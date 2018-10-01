package ru.vachok.networker.componentsrepo;


import org.springframework.stereotype.Component;

/**
 @since 01.10.2018 (13:37) */
@Component
public class ResoCache {

    private static ResoCache resoCache = new ResoCache();

    private byte[] bytes;

    private String filePath;

    private ResoCache() {
    }

    public static ResoCache getResoCache() {
        return resoCache;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
