package io.github.fahmih28.configuration;

import io.github.fahmih28.configuration.properties.WebResourcesProperties;
import io.github.fahmih28.configuration.tomcat.ClasspathResourceSet;
import io.github.fahmih28.configuration.tomcat.JspIndexFilter;
import io.github.fahmih28.configuration.tomcat.JspRestrictionFilter;
import org.apache.catalina.webresources.StandardRoot;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.servlet.DispatcherType;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.function.Function;

import static io.github.fahmih28.configuration.tomcat.ClasspathResourceSet.PROTOCOL;


@Configuration
@EnableConfigurationProperties(WebResourcesProperties.class)
public class JspConfiguration {

    private static final Set<String> JSP_EXTENSIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(".jsp", ".jspx")));

    private static final String JAR_EXTENSION = ".jar";

    private static final String WEB_INF_PREFIX = "/WEB-INF";

    private static final String BOOT_INF_PREFIX = "/BOOT-INF";

    private static final Set<String> FILTER_INDEX_JSPX = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("/index.jsp", "/index.jspx")));

    @Bean
    public TomcatContextCustomizer tomcatContextCustomizer(WebResourcesProperties resourceProperties,
                                                           ResourceLoader resourceLoader,
                                                           ResourcePatternResolver resourcePatternResolver) {
        return context -> {
            try {

                StandardRoot standardRoot = new StandardRoot(context);
                context.setResources(standardRoot);

                ClasspathResourceSet jspResourceSet = ClasspathResourceSet.builder()
                        .context(standardRoot)
                        .resourceFilter(path -> JSP_EXTENSIONS
                                .stream()
                                .anyMatch(extension -> path.endsWith(extension))
                        )
                        .resourceLoader(resourceLoader)
                        .serveListWebAppPath(false)
                        .serveList(false)
                        .resourcePatternResolver(resourcePatternResolver)
                        .resourceTranslator(path -> resourceProperties.getJsp().getLocation() + path)
                        .build();

                standardRoot.addPreResources(jspResourceSet);

                Function<String, String> translator = (path) -> path.startsWith(WEB_INF_PREFIX) ? BOOT_INF_PREFIX + path.substring(WEB_INF_PREFIX.length()) : path;
                ClasspathResourceSet extResourceSet = ClasspathResourceSet.builder()
                        .context(standardRoot)
                        .resourceLoader(resourceLoader)
                        .resourcePatternResolver(resourcePatternResolver)
                        .serveList(true)
                        .serveListWebAppPath(true)
                        .resourceTranslator(translator)
                        .listTranslator(translator)
                        .listWebAppPathTranslator(translator)
                        .build();

                standardRoot.addPreResources(extResourceSet);


            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };
    }


    @Bean
    @ConditionalOnProperty(prefix = "web-resources.jsp", name = "map-index", havingValue = "true")
    public FilterRegistrationBean<JspIndexFilter> jspIndexFilter(ResourcePatternResolver resourcePatternResolver, WebResourcesProperties webResourcesProperties) throws Exception {
        FilterRegistrationBean<JspIndexFilter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
        filterFilterRegistrationBean.setFilter(new JspIndexFilter());
        Map<String, String> pathMapping = new HashMap<>();
        try {
            Resource[] resources = resourcePatternResolver.getResources(PROTOCOL + webResourcesProperties.getJsp().getLocation() + "/**");
            for (Resource resource : resources) {
                String url = resource.getURL().toString();
                int isJar = url.indexOf(JAR_EXTENSION);
                if (isJar != -1) {
                    url = url.substring(isJar + JAR_EXTENSION.length());
                } else {
                    url = resource.getFile().getAbsolutePath();
                    int relativeTo = resourcePatternResolver.getResource(PROTOCOL).getFile().getAbsolutePath().length();
                    url = url.substring(relativeTo);
                }

                url = url.substring(webResourcesProperties.getJsp().getLocation().length());

                for (String indexJsp : FILTER_INDEX_JSPX) {
                    if (url.endsWith(indexJsp)) {
                        String pathPattern = url.substring(0, url.length() - indexJsp.length());
                        if (pathPattern.equals("")) {
                            pathPattern = "/";
                        }
                        pathMapping.put(pathPattern, url);
                        break;
                    }
                }
            }
        } catch (FileNotFoundException e) {
        }

        filterFilterRegistrationBean.setInitParameters(pathMapping);
        filterFilterRegistrationBean.setOrder(2);
        filterFilterRegistrationBean.setUrlPatterns(pathMapping.keySet());

        return filterFilterRegistrationBean;
    }

    @Bean
    @ConditionalOnProperty(prefix = "web-resources.jsp", name = "restrict-direct-access", havingValue = "true")
    public FilterRegistrationBean<JspRestrictionFilter> jspSecurityFilter() {
        FilterRegistrationBean<JspRestrictionFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new JspRestrictionFilter());
        filterRegistrationBean.setUrlPatterns(Collections.singleton("*.jsp"));
        filterRegistrationBean.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
        return filterRegistrationBean;
    }
}
