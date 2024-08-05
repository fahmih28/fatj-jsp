package io.github.fahmih28.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "web-resources")
public class WebResourcesProperties {

    @Data
    public static class Jsp{
        private String location;
        private boolean mapIndex;
        private boolean restrictDirectAccess;
    }

    private Jsp jsp;

}
