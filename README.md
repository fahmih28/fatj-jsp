# fatj-jsp
springboot's autoconfiguration library for enabling a fat/uber jar app to serve jsp

## Usage
- add this lib in your dependency 
- its required to include, the dependency below in your pom.xml, this library uses spring boot parent version 2.7.18.
  ```xml
     <dependencies>
        ....
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>jstl</artifactId>
        </dependency>
     </dependencies>
  ```
    
- set up several configuration on the project's application properties/yml
  ### Properties
    for serving jsp file you need set the location/path of jsp files inside fatjar, use the properties as below, always use '/' prefix in the path  
    ```yaml
        web-resource:
          jsp:
            location: /META-INF/webapp
    ```
    for allowing redirect the dir name to index.jsp/jspx resides inside it, set this properties below
    ```yaml
        web-resource:
          jsp:
            map-index: true
    ```

