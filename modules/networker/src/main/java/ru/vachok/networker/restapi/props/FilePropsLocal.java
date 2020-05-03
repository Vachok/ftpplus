// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;

import java.io.*;
import java.nio.file.Files;
import java.util.Properties;


/**
 @see ru.vachok.networker.restapi.props.FilePropsLocalTest */
public class FilePropsLocal implements InitProperties {


    private final String propertiesName;

    private File propFile;

    public void setPropFile(File propFile) {
        this.propFile = propFile;
    }

    private @NotNull Properties getFromStream() {
        Properties retProps = new Properties();
        try (InputStream inputStream = getClass().getResourceAsStream(ConstantsFor.STREAMJAR_PROPERTIES)) {
            retProps.load(inputStream);
            return retProps;
        }
        catch (IOException e) {
            retProps.setProperty(e.getMessage(), new TForms().fromArray(e));
            return retProps;
        }
    }

    @Override
    public Properties getProps() {
        Properties retPr = new Properties();
        try {
            retPr.load(new FileInputStream(propFile));
            return retPr;
        }
        catch (IOException e) {
            return getFromStream();
        }
    }

    public FilePropsLocal(@NotNull String propertiesName) {
        if (!propertiesName.contains(ConstantsFor.PATTERN_POINT)) {
            this.propertiesName = propertiesName + FileNames.EXT_PROPERTIES;
        }
        else {
            this.propertiesName = propertiesName;
        }
        this.propFile = new File(this.propertiesName);
    }

    @Override
    public boolean setProps(@NotNull Properties properties) {
        try (OutputStream outputStream = new FileOutputStream(propFile)) {
            properties.store(outputStream, getClass().getSimpleName());
            propFile.setLastModified(System.currentTimeMillis());
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FilePropsLocal{");
        sb.append("propFile=").append(propFile);
        sb.append(", propertiesName='").append(propertiesName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
