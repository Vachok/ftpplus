package ru.vachok.ostpst.api;


import java.awt.*;
import java.util.Properties;


public class FileProperties implements InitProperties {
    
    
    private String fileName;
    
    public FileProperties(String fileName) {
        this.fileName = fileName;
    }
    
    @Override public Properties getProps() {
        throw new IllegalComponentStateException("19.06.2019 (12:35)");
    }
    
    @Override public boolean delProps() {
        throw new IllegalComponentStateException("19.06.2019 (12:35)");
    }
    
    @Override public boolean setProps(Properties properties) {
        throw new IllegalComponentStateException("19.06.2019 (12:35)");
    }
}
