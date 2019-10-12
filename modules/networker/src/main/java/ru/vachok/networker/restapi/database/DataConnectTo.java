// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.data.enums.ConstantsFor;

import java.sql.*;
import java.util.Collection;
import java.util.List;


/**
 @see DataConnectToTest
 @since 14.07.2019 (12:15) */
public interface DataConnectTo extends ru.vachok.mysqlandprops.DataConnectTo {
    
    
    String DBNAME_VELKOM_POINT = "velkom.";
    
    String DEFAULT_I = "loc";
    
    String TESTING = "testing";
    
    @Contract(value = " -> new", pure = true)
    static @NotNull DataConnectTo getRemoteReg() {
        return new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_VELKOM);
    }
    
    @Contract(" -> new")
    static @NotNull ru.vachok.mysqlandprops.DataConnectTo getExtI() {
        return new RegRuMysql();
    }
    
    @Override
    default void setSavepoint(Connection connection) {
        throw new UnsupportedOperationException("14.07.2019 (15:44)");
    }
    
    @Contract(value = " -> new", pure = true)
    static @NotNull DataConnectTo getDefaultI() {
        return new MySqlLocalSRVInetStat();
    }
    
    int uploadCollection(Collection stringsCollection, String tableName);
    
    boolean dropTable(String dbPointTable);
    
    /**
     @param dbPointTable dbname.table
     @param additionalColumns unstandart column names <b>with type</b>
     @return executeQuery
     */
    default int createTable(String dbPointTable, List<String> additionalColumns) {
        return new MySqlLocalSRVInetStat().createTable(dbPointTable, additionalColumns);
    }
    
    @Override
    default Savepoint getSavepoint(Connection connection) {
        throw new UnsupportedOperationException("14.07.2019 (15:45)");
    }
    
    @Override
    MysqlDataSource getDataSource();
    
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    static @NotNull ru.vachok.networker.restapi.database.DataConnectTo getInstance(@NotNull String type) {
        switch (type) {
            case DEFAULT_I:
                return getDefaultI();
            case ConstantsFor.DBBASENAME_U0466446_PROPERTIES:
                return new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_PROPERTIES);
            case TESTING:
                return new TesterDB65SQL(ConstantsFor.STR_VELKOM);
            default:
                return new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        }
    }
    
    static @NotNull String getTableInfo(@NotNull ResultSet resultSet) throws SQLException {
        StringBuilder infoBuilder = new StringBuilder();
        infoBuilder.append("Name : ").append(resultSet.getString(1)).append("\n");
        infoBuilder.append("Eng : ").append(resultSet.getString(2)).append("\n");
        infoBuilder.append("Row_format : ").append(resultSet.getString(3)).append("\n");
        infoBuilder.append("Rows : ").append(resultSet.getString(5)).append("\n");
        infoBuilder.append("Avg_row_length : ").append(resultSet.getString(6)).append("\n");
        infoBuilder.append("Data_length : ").append(resultSet.getString(7)).append("\n");
        infoBuilder.append("Max_data_length : ").append(resultSet.getString(8)).append("\n");
        infoBuilder.append("Auto_increment : ").append(resultSet.getString(11)).append("\n");
        infoBuilder.append("Create_time : ").append(resultSet.getString(12)).append("\n");
        infoBuilder.append("Update_time : ").append(resultSet.getString(13)).append("\n");
        infoBuilder.append("Create_options : ").append(resultSet.getString(17)).append("\n");
        infoBuilder.append("Comment : ").append(resultSet.getString(18)).append("\n");
        
        return infoBuilder.toString();
    }
}
