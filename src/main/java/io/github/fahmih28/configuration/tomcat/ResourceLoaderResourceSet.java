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
public class ResourceLoaderResourceSet extends AbstractResourceSet {

    private static final String JAR_SUFFIX = ".jar!";

    private final Function<String,String> pathTranslator;
    private final Predicate<String> filter;
    private final ResourceLoader resourceLoader;
    private final ResourcePatternResolver resourcePatternResolver;
    private final WebResourceRoot context;
    private final boolean serveList;
    private final boolean serveWebappPath;
    private final long timestamp;

    @Override
    public WebResource getResource(String path) {

        path = pathTranslator != null? pathTranslator.apply(path):path;

        if (filter != null && !filter.test(path)) {
                return new EmptyResource(context, path);
        }

        Resource resource = resourceLoader.getResource(path);
        if (!resource.exists()) {
            return new EmptyResource(context, path);
        }

        return new ResourceWebResource(resource, timestamp, path, context);
    }

    @Override
    public String[] list(String path) {
        if(!serveList){
            return new String[0];
        }
        try {
            path = pathTranslator != null? pathTranslator.apply(path):path;

            Resource[] resources = resourcePatternResolver.getResources( path + "/*");
            if (resources == null || resources.length == 0) {
                return new String[0];
            }

            String[] result = new String[resources.length];
            for (int i = 0;i < resources.length;i++) {
                result[i] = "/"+resources[i].getFilename();
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
    public Set<String> listWebAppPaths(String path) {
        if(!serveWebappPath){
            return Collections.emptySet();
        }

        try {
            path = pathTranslator != null? pathTranslator.apply(path):path;
            Resource[] resources = resourcePatternResolver.getResources(path + "*");
            if (resources == null || resources.length == 0) {
                return Collections.emptySet();
            }

            Set<String> paths = new HashSet<>();
            for (Resource resource : resources) {
                String suffix = !resource.isReadable()?"/":"";
                String name = "/"+resource.getFilename()+suffix;

                if (resource.isFile() && filter != null && !filter.test(name)) {
                    continue;
                }

                paths.add(name);
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
