// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.restapi.InitProperties;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.StringJoiner;


/**
 Class ru.vachok.networker.restapi.props.PropsFromFile
 <p>
 
 @since 14.07.2019 (17:35) */
public class PropsFromFile implements InitProperties {
    
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private Path filePath;
    
    private Properties properties;
    
    @Contract(pure = true)
    public PropsFromFile(Path filePath, Properties properties) {
        this.filePath = filePath;
        this.properties = properties;
    }
    
    @Override
    public MysqlDataSource getRegSourceForProperties() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Properties getProps() {
        try {
            this.properties.load(new FileInputStream(filePath.toAbsolutePath().normalize().toString()));
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("PropsFromFile.getProps threw away: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        return this.properties;
    }
    
    @Override
    public boolean setProps(Properties properties) {
        this.properties = properties;
        
        try {
            properties.store(new FileOutputStream(filePath.toAbsolutePath().normalize().toString()), toString());
            
        }
        catch (IOException e) {
            messageToUser
                .error(MessageFormat.format("PropsFromFile.setProps says: {0}. Parameters: \n[properties({2})]: {1}", e.getMessage(), properties, properties.size()));
        }
        return filePath.toFile().exists();
    }
    
    @Override
    public boolean delProps() {
        try {
            Files.deleteIfExists(filePath);
        }
        catch (IOException e) {
            filePath.toFile().delete();
            
        }
        return filePath.toFile().exists();
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", PropsFromFile.class.getSimpleName() + "[\n", "\n]")
            .add("filePath = " + filePath)
            .add(ConstantsFor.TOSTRING_PROPERTIES + properties.size())
            .toString();
    }
}