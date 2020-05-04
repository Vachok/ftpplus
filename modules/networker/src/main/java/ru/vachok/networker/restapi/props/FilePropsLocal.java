// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.*;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Properties;


/**
 @see ru.vachok.networker.restapi.props.FilePropsLocalTest */
public class FilePropsLocal implements InitProperties {


    private final String propertiesName;

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, FilePropsLocal.class.getSimpleName());

    private File propFile;

    void setPropFile(File propFile) {
        this.propFile = propFile;
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

    public void reloadPropsFromDB() {
        this.propFile = new File(FileNames.CONSTANTSFOR_PROPERTIES);
        InitProperties instance = InitProperties.getInstance(InitProperties.DB_MEMTABLE);
        Properties props = instance.getProps();
        if (!propFile.exists() || propFile.canWrite()) {
            messageToUser.info(getClass().getSimpleName(), "Properties reloaded from mem.properties", String.valueOf(setProps(props)));
        }
        else if (!propFile.canWrite() && propFile.canRead()) {
            props = getProps();
            messageToUser
                .info(getClass().getSimpleName(),
                    MessageFormat.format("Properties loaded to mem.properties. Setting file writable: {0}", propFile.setWritable(true)),
                    MessageFormat.format("{0} file can write. Props set to memtable {1}", propFile.canWrite(), String.valueOf(instance.setProps(props))));
        }
        else {
            messageToUser.warn(getClass().getSimpleName(),
                MessageFormat.format("{0} is unreadable", propFile.getAbsolutePath()),
                MessageFormat.format("Delete: {0}", propFile.delete()));
        }
    }

    @Override
    public boolean setProps(@NotNull Properties properties) {
        try (OutputStream outputStream = new FileOutputStream(propFile)) {
            properties.store(outputStream, getClass().getSimpleName());
            return propFile.setLastModified(System.currentTimeMillis());
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
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
