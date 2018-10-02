package ru.vachok.networker.componentsrepo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.networker.ConstantsFor;

import javax.annotation.Resource;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 @since 01.10.2018 (13:37) */
@Resource
public class ResoCache implements org.springframework.core.io.Resource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResoCache.class.getSimpleName());

    private static ResoCache resoCache = new ResoCache();

    private byte[] bytes;

    private String filePath;

    private String descr;

    private List<ResoCache> resources = new ArrayList<>();

    private String fileName = ConstantsFor.APP_NAME + new SecureRandom().nextInt(Year.now().getValue()) + ".res";

    private File file = new File(fileName);

    private long lastModif;

    private ResoCache() {

    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public List<ResoCache> getResources() {
        return resources;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setLastModif(long lastModif) {
        this.lastModif = lastModif;
    }

    public void setBytes(byte[] bytes) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(this.file)) {
            resources.add(this);
            fileOutputStream.write(bytes);
            resources.add(this);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static ResoCache getResoCache() {
        return resoCache;
    }

    @Override
    public boolean exists() {
        return !resources.isEmpty() && 0 < bytes.length && file.exists();
    }

    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public URL getURL() throws IOException {
        return getURI().toURL();
    }

    @Override
    public URI getURI() {
        return file.toURI();
    }

    @Override
    public File getFile() {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(bytes);
        } catch (IOException e) {

        }
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public long contentLength() {
        return bytes.length;
    }

    @Override
    public long lastModified() {
        return lastModif;
    }

    @Override
    public org.springframework.core.io.Resource createRelative(String relativePath) {
        throw new UnsupportedOperationException("Not Implemented yet");
    }

    @Override
    public String getFilename() {
        return fileName;
    }

    @Override
    public String getDescription() {
        return descr;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            return inputStream;
        }
    }
}
