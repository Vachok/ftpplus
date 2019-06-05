package ru.vachok.networker.net.ftp;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.mysqlandprops.RegRuMysql;

import java.awt.*;
import java.io.File;
import java.net.ConnectException;
import java.nio.file.AccessDeniedException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;


/**
 @since 05.06.2019 (11:36) */
public class RegRuDBLibs implements LibsHelp {
    
    
    @Override public String uploadLibs() throws AccessDeniedException, ConnectException {
        MysqlDataSource regDataSrc = new RegRuMysql().getDataSourceSchema("u0466446_properties-libs");
        StringBuilder stringBuilder = new StringBuilder();
        regDataSrc.setRelaxAutoCommit(true);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String formatedDate = dateFormat.format(new Date());
        
        final String sql = "UPDATE `u0466446_properties`.`libs` SET `libversion` = ?, `libdate` = ?, `libbin` = ?";
        try (Connection connection = regDataSrc.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            File[] libFiles = new RegRuFTPLibsUploader().getLibFiles();
            for (File file : libFiles) {
                preparedStatement.setString(1, file.getName());
                preparedStatement.setString(2, getVersion());
            }
            
        }
        catch (SQLException e) {
            stringBuilder.append(e.getMessage()).append(" ").append(getClass().getSimpleName()).append("\n");
        }
        return stringBuilder.toString();
    }
    
    @Override public Queue<String> getContentsQueue() {
        throw new IllegalComponentStateException("05.06.2019 (12:46)");
    }
}
