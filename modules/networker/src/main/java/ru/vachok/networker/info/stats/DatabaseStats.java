package ru.vachok.networker.info.stats;


import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.*;


/**
 @since 10.10.2019 (17:16) */
public class DatabaseStats implements Stats {
    
    
    private String dbName = "mysql.general_log" ;
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, DatabaseStats.class.getSimpleName());
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        throw new TODOException("10.10.2019 (17:16)");
    }
    
    @Override
    public String getInfo() {
        return slowLog();
    }
    
    private String slowLog() {
        DataConnectTo dataConnectTo = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);
        StringBuilder stringBuilder = new StringBuilder();
        
        try (Connection connection = dataConnectTo.getDefaultConnection("mysql.slow_log")) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `mysql`.`slow_log` ORDER BY `query_time` DESC LIMIT 100;")) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    stringBuilder.append("Showing SLOW.LOG from ").append(dataConnectTo.toString()).append("\n");
                    while (resultSet.next()) {
                        stringBuilder.append("Start time: ").append(resultSet.getString(1)).append(", ");
                        stringBuilder.append("host: ").append(resultSet.getString(2)).append(", ");
                        stringBuilder.append("q_time: ").append(resultSet.getString(3)).append(", ");
                        stringBuilder.append("locked time: ").append(resultSet.getString(4)).append(", ");
                        stringBuilder.append("rows sent: ").append(resultSet.getString(5)).append(", ");
                        stringBuilder.append("rows examined: ").append(resultSet.getString(6)).append(", ");
                        stringBuilder.append("db: ").append(resultSet.getString(7)).append(", ");
                        stringBuilder.append("last insert id: ").append(resultSet.getString(8)).append(", ");
                        stringBuilder.append("insert id: ").append(resultSet.getString(9)).append(", ");
                        stringBuilder.append("sql: ").append(resultSet.getString(11)).append("\n\n");
                    }
                }
            }
        }
        catch (SQLException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(AbstractForms.fromArray(e));
        }
        return stringBuilder.toString();
    }
    
    @Override
    public void setClassOption(Object option) {
        if (option instanceof String) {
            this.dbName = (String) option;
        }
        else {
            throw new IllegalArgumentException(option.toString());
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DatabaseStats{");
        sb.append("messageToUser=").append(messageToUser);
        sb.append(", dbName='").append(dbName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
