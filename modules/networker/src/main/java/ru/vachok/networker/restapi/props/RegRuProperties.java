package ru.vachok.networker.restapi.props;


import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import static ru.vachok.networker.restapi.database.DataConnectTo.REGRUCONNECTION;


/**
 Class ru.vachok.networker.restapi.props.RegRuProperties
 <p>

 @since 06.05.2020 (2:11) */
class RegRuProperties implements InitProperties {


    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.SWING, RegRuProperties.class.getSimpleName());

    @Override
    public Properties getProps() {
        Properties properties = new Properties();
        String sql = "SELECT * FROM `u0466446_properties`.`dev`";
        try (Connection connection = DataConnectTo.getInstance(REGRUCONNECTION).getDefaultConnection(ConstantsFor.DB_REGRU_JSON_PROPS_TABLE)) {
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setQueryTimeout(5);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        if (resultSet.getString(ConstantsFor.DBCOL_VALUEOFPROPERTY).equals("json")) {
                            properties.setProperty(resultSet.getString(ConstantsFor.DBCOL_PROPERTY), resultSet.getString("jsonobject"));
                        }
                    }
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error("RegRuProperties.getProps", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
        return properties;
    }

    @Override
    public boolean setProps(Properties properties) {
        String sql = "INSERT INTO `u0466446_properties`.`dev` (`property`, `valueofproperty`, `jsonobject`) VALUES (?, ?, ?)";
        boolean retBool = false;
        try (Connection connection = DataConnectTo.getInstance(REGRUCONNECTION).getDefaultConnection(ConstantsFor.DB_REGRU_JSON_PROPS_TABLE)) {
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setQueryTimeout(5);
                for (Object o : properties.keySet()) {
                    preparedStatement.setString(1, o.toString());
                    preparedStatement.setString(2, "json");
                    preparedStatement.setString(3, properties.get(o).toString());
                    preparedStatement.addBatch(sql);
                }
                retBool = preparedStatement.executeUpdate() > 0;
            }
        }
        catch (SQLException e) {
            messageToUser.error("RegRuProperties.setProps", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
        return retBool;
    }

    @Override
    public boolean delProps() {
        throw new UnsupportedOperationException("Only by hands! 06.05.2020 (2:39)");
    }
}