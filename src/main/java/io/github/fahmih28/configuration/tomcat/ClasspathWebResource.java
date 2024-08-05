package io.github.fahmih28.configuration.tomcat;

import io.github.fahmih28.util.IOUtils;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.jar.Manifest;

public class ClasspathWebResource implements WebResource {

    private final Resource resource;
    private final long timestamp;
    private final String path;
    private final WebResourceRoot root;
    private String mimeType;

    public ClasspathWebResource(Resource resource, long timestamp, String path, WebResourceRoot root) {
        this.resource = resource;
        this.timestamp = timestamp;
        this.path = path;
        this.root = root;
    }

    @Override
    public long getLastModified() {
        return timestamp;
    }

    @Override
    public String getLastModifiedHttp() {
        return "";
    }

    @Override
    public boolean exists() {
        return resource.exists();
    }

    @Override
    public boolean isVirtual() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return !resource.isReadable();
    }

    @Override
    public boolean isFile() {
        return resource.isReadable();
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public String getName() {
        return resource.getFilename();
    }

    @Override
    public long getContentLength() {
        try {
            return resource.contentLength();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getCanonicalPath() {
        return path;
    }

    @Override
    public boolean canRead() {
        return true;
    }

    @Override
    public String getWebappPath() {
        return path;
    }

    @Override
    public String getETag() {
        return "";
    }

    @Override
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public InputStream getInputStream() {
        try {
            return resource.getInputStream();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] getContent() {
        try {
            return IOUtils.readFully(getInputStream(),(int)getContentLength());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public long getCreation() {
        return timestamp;
    }

    @Override
    public URL getURL() {
        try {
            return resource.getURL();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public URL getCodeBase() {
        return null;
    }

    @Override
    public WebResourceRoot getWebResourceRoot() {
        return root;
    }

    @Override
    public Certificate[] getCertificates() {
        return new Certificate[0];
    }

    @Override
    public Manifest getManifest() {
        return null;
    }
}
