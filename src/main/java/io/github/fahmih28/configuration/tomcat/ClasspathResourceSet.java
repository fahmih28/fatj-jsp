package io.github.fahmih28.configuration.tomcat;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.webresources.AbstractResourceSet;
import org.apache.catalina.webresources.EmptyResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClasspathResourceSet extends AbstractResourceSet {

    public static final String JAR_SUFFIX = ".jar!";

    public static final String PROTOCOL = "classpath:";

    private final Function<String,String> resourceTranslator;
    private final Predicate<String> resourceFilter;
    private final Function<String,String> listTranslator;
    private final Function<String,String> listWebAppPathTranslator;
    private final boolean serveList;
    private final boolean serveListWebAppPath;
    private final ResourceLoader resourceLoader;
    private final ResourcePatternResolver resourcePatternResolver;
    private final WebResourceRoot context;
    private final long timestamp;

    @Override
    public WebResource getResource(String reqPath) {
        String path = resourceTranslator != null? resourceTranslator.apply(reqPath):reqPath;
        if (resourceFilter != null && !resourceFilter.test(path)) {
            return new EmptyResource(context, path);
        }
        Resource resource = resourceLoader.getResource(PROTOCOL+path);
        if (!resource.exists()) {
            return new EmptyResource(context, path);
        }

        return new ClasspathWebResource(resource, timestamp, path, context);
    }

    @Override
    public String[] list(String reqPath) {
        if(!serveList){
            return new String[0];
        }
        try {
            String path = listTranslator != null? listTranslator.apply(reqPath):reqPath;
            String separator = path.charAt(path.length()-1) == '/'?"":"/";
            path = path+separator;
            Resource[] subPathResources = resourcePatternResolver.getResources( PROTOCOL+path +"**");
            if (subPathResources == null || subPathResources.length == 0) {
                return new String[0];
            }


            Set<String> subPaths = new HashSet<>();
            for (Resource subPathResoruce:subPathResources) {
                String pathWithJar = subPathResoruce.getURL().toString();
                String pathInsideJar = pathWithJar.substring(pathWithJar.indexOf(JAR_SUFFIX)+JAR_SUFFIX.length());
                int oneLevelPosition = pathInsideJar.indexOf('/',path.length());
                String actualPath = (oneLevelPosition != -1 )? pathInsideJar.substring(path.length(),oneLevelPosition):pathInsideJar.substring(path.length());
                if(actualPath.isEmpty()){
                    continue;
                }
                subPaths.add(actualPath);
            }

            String[] result = new String[subPaths.size()];
            int i = 0;
            for(String subPath:subPaths){
                result[i++] = subPath;
            }

            return result;

        } catch (FileNotFoundException e) {
            return new String[0];
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }

    }

    @Override
    public Set<String> listWebAppPaths(String reqPath) {
        if(!serveListWebAppPath){
            return Collections.emptySet();
        }

        try {
            String path = listWebAppPathTranslator != null? listWebAppPathTranslator.apply(reqPath):reqPath;
            String separator = path.charAt(path.length()-1) == '/'?"":"/";
            path = path+separator;
            Resource[] subPathResources = resourcePatternResolver.getResources(PROTOCOL+path + "**");
            if (subPathResources == null || subPathResources.length == 0) {
                return Collections.emptySet();
            }

            Set<String> paths = new HashSet<>();
            for (Resource subPathResource : subPathResources) {
                String pathWithJar = subPathResource.getURL().toString();
                String pathInsideJar = pathWithJar.substring(pathWithJar.indexOf(JAR_SUFFIX)+JAR_SUFFIX.length());
                int oneLevelPosition = pathInsideJar.indexOf('/',path.length());

                String actualPath = (oneLevelPosition != -1 )? pathInsideJar.substring(path.length(),oneLevelPosition):pathInsideJar.substring(path.length());
                if(actualPath.isEmpty()){
                    continue;
                }

                Resource testIfDir = resourceLoader.getResource(PROTOCOL+path+actualPath);
                String suffix = !testIfDir.isReadable()?"/":"";
                paths.add(path+actualPath+suffix);
            }

            return paths;
        } catch (FileNotFoundException e) {
            return Collections.emptySet();
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean mkdir(String path) {
        return false;
    }

    @Override
    public boolean write(String path, InputStream is, boolean overwrite) {
        return false;
    }

    @Override
    public URL getBaseUrl() {
        return null;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void gc() {
    }

    @Override
    public void initInternal() {
    }
}
