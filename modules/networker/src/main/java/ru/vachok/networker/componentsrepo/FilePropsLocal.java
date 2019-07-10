package ru.vachok.networker.componentsrepo;


import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.ConstantsFor;

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
            System.err.println("getProps()");
            System.err.println(e.getMessage());
        }
        return retPr;
    }
    
    @Override
    public boolean setProps(Properties properties) {
        delProps();
        try (OutputStream outputStream = new FileOutputStream(propFile)) {
            properties.store(outputStream, getClass().getSimpleName());
            System.out.println(propFile.toPath().toAbsolutePath() + " = " + propFile.length() + " bytes");
            return true;
        }
        catch (IOException e) {
            System.err.println("setProps(Properties properties)");
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
            System.err.println("delProps() is " + isDel);
            System.err.println(e.getMessage());
            return isDel;
        }
        
    }
}
