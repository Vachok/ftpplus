package ru.vachok.networker.data;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.sql.*;


/**
 @see MyISAMRepairTest
 @since 20.11.2019 (9:11) */
public class MyISAMRepair implements InformationFactory {
    
    
    private Object classOption;
    
    private DataConnectTo dataConnectTo = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);
    
    public String repairTable(String sql) {
        final long startRepairStamp = System.currentTimeMillis();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("*** ru.vachok.networker.data.MyISAMRepair.repairTable starting at ").append(startRepairStamp).append("\n");
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.DB_SLOWLOG)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                stringBuilder.append("Query ").append(sql).append(" ends with code: ").append(preparedStatement.executeUpdate()).append("\n");
                stringBuilder.append("Time spend: ").append(System.currentTimeMillis() - startRepairStamp).append(" milliseconds");
            }
        }
        catch (SQLException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(AbstractForms.fromArray(e));
        }
        finally {
            stringBuilder.append("ru.vachok.networker.data.MyISAMRepair.repairTable complete ***\n");
        }
        return stringBuilder.toString();
    }
    
    @Override
    public String getInfo() {
        String sql = "SHOW TABLE STATUS FROM velkom";
        if (classOption instanceof String) {
            sql = "SHOW TABLE STATUS FROM " + classOption;
        }
        
        StringBuilder stringBuilder = new StringBuilder();
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.DB_SLOWLOG)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                stringBuilder.append(preparedStatement.toString().split(": ")[1]).append("\n");
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    stringBuilder.append(parseResults(resultSet));
                }
            }
        }
        catch (SQLException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(AbstractForms.fromArray(e));
        }
        return stringBuilder.toString();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MyISAMRepair{");
        sb.append("classOption=").append(classOption);
        sb.append('}');
        return sb.toString();
    }
    
    private @NotNull String parseResults(@NotNull ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        StringBuilder stringBuilder = new StringBuilder();
        while (resultSet.next()) {
            stringBuilder.append(resultSet.getString(1)).append(": ").append(resultSet.getString(18)).append("\n");
        }
        return stringBuilder.toString();
    }
    
    @Override
    public void setClassOption(Object option) {
        if (option instanceof DataConnectTo) {
            this.dataConnectTo = (DataConnectTo) option;
        }
        this.classOption = option;
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.classOption = aboutWhat;
        return getInfo();
    }
    
    
}
