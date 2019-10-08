package ru.vachok.networker.restapi.props;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.*;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;


/**
 @see MemoryPropertiesTest
 @since 08.10.2019 (16:26) */
public class MemoryProperties extends DBPropsCallable {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.DB, MemoryProperties.class.getSimpleName());
    
    @Override
    public Properties getProps() {
        Properties retProps = fromMemoryTable();
        if (retProps.size() < 17) {
            retProps.putAll(super.getProps());
        }
        else {
            InitProperties.getInstance(InitProperties.FILE).setProps(retProps);
        }
        return retProps;
    }
    
    private @NotNull Properties fromMemoryTable() {
        Properties properties = new Properties();
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_MEMPROPERTIES);
             PreparedStatement preparedStatement = connection.prepareStatement("select * from mem.properties");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                properties.put(resultSet.getString(2), resultSet.getString(3));
            }
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("MemoryProperties.fromMemoryTable", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace())));
        }
        return properties;
    }
    
    @Override
    public boolean setProps(@NotNull Properties properties) {
        final String sql = "INSERT INTO `properties` (`property`, `valueofproperty`) VALUES (?, ?);";
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_MEMPROPERTIES)) {
            int update = 0;
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    preparedStatement.setString(1, entry.getKey().toString());
                    preparedStatement.setString(2, entry.getValue().toString());
                    update += preparedStatement.executeUpdate();
                }
            }
            AppComponents.threadConfig().execByThreadConfig(()->super.setProps(properties));
            return update > 0;
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("MemoryProperties.setProps", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace())));
            return updateTable(properties) > 0;
        }
    }
    
    @Contract(pure = true)
    private int updateTable(@NotNull Properties properties) {
        final String sql = "UPDATE properties SET property=?, valueofproperty=? WHERE property=?";
        int update = 0;
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_MEMPROPERTIES)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    preparedStatement.setString(1, entry.getKey().toString());
                    preparedStatement.setString(2, entry.getValue().toString());
                    preparedStatement.setString(3, entry.getKey().toString());
                    update += preparedStatement.executeUpdate();
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("MemoryProperties.updateTable", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace())));
            update -= 1;
        }
        return update;
    }
    
    @Override
    public boolean delProps() {
        throw new TODOException("ru.vachok.networker.restapi.props.MemoryProperties.delProps created 08.10.2019 (15:59)");
    }
}
