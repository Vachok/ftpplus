package ru.vachok.networker.componentsrepo.services;


import org.jetbrains.annotations.Contract;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.StringJoiner;


/**
 @since 16.11.2019 (20:46) */
public class LocalDBLibsUploader implements Runnable {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, LocalDBLibsUploader.class.getSimpleName());
    
    private DataConnectTo dataConnectTo;
    
    private String libName;
    
    private String libVersion;
    
    private String description;
    
    private Path pathTo;
    
    public LocalDBLibsUploader(String libName, String libVersion, String description, Path pathTo) {
        this.libName = libName;
        this.libVersion = libVersion;
        this.description = description;
        this.pathTo = pathTo;
        this.dataConnectTo = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);
    }
    
    @Contract(pure = true)
    public LocalDBLibsUploader(String libName, String libVersion, String description, Path pathTo, DataConnectTo dataConnectTo) {
        this.libName = libName;
        this.libVersion = libVersion;
        this.description = description;
        this.pathTo = pathTo;
        this.dataConnectTo = dataConnectTo;
    }
    
    @Override
    public void run() {
        final String sql = "INSERT INTO `velkom`.`library` (`LibName`, `LibVersion`,`Description`, `Bin`) VALUES (?, ?, ?, ?)";
        System.out.println("connectToDB() = " + connectToDB(sql));
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", LocalDBLibsUploader.class.getSimpleName() + "[\n", "\n]")
            .add("dataConnectTo = " + dataConnectTo)
            .toString();
    }
    
    private int connectToDB(String sql) {
        int retInt;
        try (InputStream inputStream = new FileInputStream(String.valueOf(pathTo.normalize().toAbsolutePath()))) {
            try (Connection connection = dataConnectTo.getDefaultConnection("velkom.library")) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, libName);
                    preparedStatement.setString(2, libVersion);
                    preparedStatement.setString(3, description);
                    preparedStatement.setBinaryStream(4, inputStream);
                    retInt = preparedStatement.executeUpdate();
                }
            }
        }
        catch (SQLException | IOException e) {
            messageToUser.error("LocalDBLibsUploader.run", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
            retInt = -666;
            if (e.getMessage().contains(ConstantsFor.ERROR_DUPLICATEENTRY)) {
                sql = sql.replace("INSERT", "REPLACE");
                retInt = connectToDB(sql);
            }
        }
        return retInt;
    }
}