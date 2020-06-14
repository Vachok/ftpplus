package ru.vachok.networker.restapi.props;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;


/**
 @see MemoryPropertiesTest
 @since 08.10.2019 (16:26) */
public class MemoryProperties extends DBPropsCallable {


    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, MemoryProperties.class.getSimpleName());

    @Override
    public boolean setProps(@NotNull Properties properties) {
        final String sql = "INSERT INTO mem.properties (property, valueofproperty, setter) VALUES (?, ?, ?);";
        int update = 0;
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_MEMPROPERTIES)) {
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    preparedStatement.setString(1, entry.getKey().toString());
                    preparedStatement.setString(2, entry.getValue().toString());
                    preparedStatement.setString(3, UsefulUtilities.thisPC());
                    update += preparedStatement.executeUpdate();
                    connection.commit();
                }
            }
            catch (SQLException e) {
                messageToUser.warn(MemoryProperties.class.getSimpleName(), e.getMessage(), " see line: 46 ***");
                return updateTable(properties);
            }
        }
        catch (SQLException e) {
            if (!e.getMessage().contains(ConstantsFor.ERROR_DUPLICATEENTRY)) {
                messageToUser.error("MemoryProperties.setProps", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
            }
        }
        return update > 0;
    }

    @Contract(pure = true)
    private boolean updateTable(@NotNull Properties properties) {
        boolean result = false;
        boolean finished = false;
        final String sql = "UPDATE properties SET property=?, valueofproperty=? WHERE property=?";
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_MEMPROPERTIES)) {
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            connection.setSavepoint();
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    preparedStatement.setString(1, entry.getKey().toString());
                    preparedStatement.setString(2, entry.getValue().toString());
                    preparedStatement.setString(3, entry.getKey().toString());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
            }
            catch (SQLException e) {
                connection.rollback();
                messageToUser.warn(MemoryProperties.class.getSimpleName(), e.getMessage(), " see line: 123 ***");
                finished = true;
            }
            if (!finished) {
                result = true;
            }
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("MemoryProperties.updateTable", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace())));
        }
        return result;
    }
}
