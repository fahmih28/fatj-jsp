package io.github.fahmih28.configuration;

import io.github.fahmih28.configuration.properties.WebResourcesProperties;
import io.github.fahmih28.configuration.tomcat.JspIndexFilter;
import io.github.fahmih28.configuration.tomcat.ResourceLoaderResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.FileNotFoundException;
import java.util.*;

@Configuration
@ConditionalOnBean(TomcatServletWebServerFactory.class)
@EnableConfigurationProperties(WebResourcesProperties.class)
public class JspConfiguration {

    private static final Set<String> JSP_EXTENSIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(".jsp",".jspx")));

    private static final String JAR_EXTENSION = ".jar";

    private static final String WEB_INF_PREFIX = "/WEB-INF";

    private static final String BOOT_INF_PREFIX = "/BOOT-INF";

    private static final String PROTOCOL = "classpath:";

    private static final String JAR_MARKED_PROTOCOL = ".jar!";

    private static final Set<String> FILTER_INDEX_JSPX = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("/index.jsp","/index.jspx")));

    @Bean
    public TomcatContextCustomizer tomcatContextCustomizer(WebResourcesProperties resourceProperties,
                                                           ResourceLoader resourceLoader,
                                                           ResourcePatternResolver resourcePatternResolver) {
        return context -> {
            try {

                StandardRoot standardRoot = new StandardRoot(context);
                context.setResources(standardRoot);

                ResourceLoaderResourceSet jspResourceSet = ResourceLoaderResourceSet.builder()
                        .context(standardRoot)
                        .filter(path -> JSP_EXTENSIONS
                                .stream()
                                .anyMatch(extension -> path.endsWith(extension))
                        )
                        .resourceLoader(resourceLoader)
                        .resourcePatternResolver(resourcePatternResolver)
                        .pathTranslator(path->PROTOCOL+resourceProperties.getJsp().getLocation()+path)
                        .serveList(false)
                        .serveWebappPath(false)
                        .build();

                standardRoot.addPreResources(jspResourceSet);

                ResourceLoaderResourceSet jarResourceSet = ResourceLoaderResourceSet.builder()
                        .context(standardRoot)
                        .filter(path -> path.endsWith(JAR_EXTENSION))
                        .resourceLoader(resourceLoader)
                        .resourcePatternResolver(resourcePatternResolver)
                        .serveList(true)
                        .serveWebappPath(true)
                        .pathTranslator(path->{
                            if(path.startsWith(WEB_INF_PREFIX)){
                                return PROTOCOL+BOOT_INF_PREFIX+path.substring(WEB_INF_PREFIX.length());
                            }
                            return path;
                        })
                        .build();

                standardRoot.addPreResources(jarResourceSet);


            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };
    }


    @Bean
    @ConditionalOnProperty(prefix = "web-resources.jsp",name = "map-index",havingValue = "true")
    public FilterRegistrationBean<JspIndexFilter> jspIndexFilter(ResourcePatternResolver resourcePatternResolver, WebResourcesProperties webResourcesProperties) throws Exception{
        FilterRegistrationBean<JspIndexFilter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
        filterFilterRegistrationBean.setFilter(new JspIndexFilter());
        Map<String,String> pathMapping = new HashMap<>();
        try {
            Resource[] resources = resourcePatternResolver.getResources(PROTOCOL+webResourcesProperties.getJsp().getLocation() + "/**");
            for (Resource resource : resources) {
                String url = resource.getURL().toString();
                int isJar = url.indexOf(JAR_MARKED_PROTOCOL);
                if(isJar != -1){
                    url = url.substring(isJar+ JAR_MARKED_PROTOCOL.length());
                }
                else{
                    url = resource.getFile().getAbsolutePath();
                    int relativeTo = resourcePatternResolver.getResource(PROTOCOL).getFile().getAbsolutePath().length();
                    url = url.substring(relativeTo);
                }

                url = url.substring(webResourcesProperties.getJsp().getLocation().length());

                for(String indexJsp:FILTER_INDEX_JSPX){
                    if(url.endsWith(indexJsp)){
                        String pathPattern = url.substring(0,url.length()-indexJsp.length());
                        if(pathPattern.equals("")){
                            pathPattern= "/";
                        }
                        pathMapping.put(pathPattern,url);
                        break;
                    }
                }
            }
        }
        catch (FileNotFoundException e) {
        }

        filterFilterRegistrationBean.setInitParameters(pathMapping);
        filterFilterRegistrationBean.setOrder(2);
        filterFilterRegistrationBean.setUrlPatterns(pathMapping.keySet());

        return filterFilterRegistrationBean;
    }
}
