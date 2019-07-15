// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo;


import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.restapi.InitProperties;

import java.io.*;
import java.nio.file.Files;
import java.util.Properties;


public class FilePropsLocal implements InitProperties {
    
    
    private String propertiesName;
    
    private File propFile;
    
    public FilePropsLocal(String propertiesName) {
        if (!propertiesName.contains(ConstantsFor.PATTERN_POINT)) {
            this.propertiesName = propertiesName + ".properties";
        }
        else {
            this.propertiesName = propertiesName;
        }
        this.propFile = new File(this.propertiesName);
    }
    
    @Override
    public Properties getProps() {
        Properties retPr = new Properties();
        try {
            retPr.load(new FileInputStream(propFile));
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return retPr;
    }
    
    @Override
    public boolean setProps(Properties properties) {
        try (OutputStream outputStream = new FileOutputStream(propFile)) {
            properties.store(outputStream, getClass().getSimpleName());
            return true;
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean delProps() {
        try {
            return Files.deleteIfExists(propFile.toPath());
        }
        catch (IOException e) {
            boolean isDel = propFile.delete();
            propFile.deleteOnExit();
            return isDel;
        }
        
    }
}
