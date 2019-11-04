package ru.vachok.networker.sysinfo;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;


/**
 * @since 16.10.2019 (16:35)
 */
public class DatabaseInfo implements DataConnectTo, InformationFactory {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, DatabaseInfo.class.getSimpleName());
    
    private Object option;
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.option = aboutWhat;
        if (option.toString().contains("slow")) {
            return slowLog();
        }
        else {
            return databaseSchemaObjects(option);
        }
    }
    
    @Override
    public void setClassOption(Object option) {
        this.option = option;
    }
    
    @Override
    public String getInfo() {
        throw new TODOException("16.10.2019 (16:34)");
    }
    
    private @NotNull String slowLog() {
        DataConnectTo dataConnectTo = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);
        StringBuilder stringBuilder = new StringBuilder();
        
        try (Connection connection = dataConnectTo.getDefaultConnection(option.toString())) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("SELECT * FROM %s ORDER BY `query_time` DESC LIMIT 100;", option.toString()))) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    stringBuilder.append("Showing SLOW.LOG from ").append(dataConnectTo.toString()).append("\n");
                    while (resultSet.next()) {
                        stringBuilder.append("Start time: ").append(resultSet.getString(1)).append(", ");
                        stringBuilder.append("host: ").append(resultSet.getString(2)).append(", ");
                        stringBuilder.append(ConstantsFor.TIME).append(resultSet.getString(3)).append(", ");
                        stringBuilder.append("locked time: ").append(resultSet.getString(4)).append(", ");
                        stringBuilder.append("rows sent: ").append(resultSet.getString(5)).append(", ");
                        stringBuilder.append(ConstantsFor.EXAMINED).append(resultSet.getString(6)).append(", ");
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
        FileSystemWorker.writeFile("slow.log", stringBuilder.toString());
        return stringBuilder.toString();
    }
    
    private String databaseSchemaObjects(Object option) {
        final String sql = String.format("SHOW TABLE STATUS FROM `%s`;", option);
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            StringBuilder stringBuilder = new StringBuilder();
            while (resultSet.next()) {
                stringBuilder.append(ConstantsFor.TABLE).append(resultSet.getString(1)).append(", ");
                stringBuilder.append("engine: ").append(resultSet.getString(2)).append(", ");
                stringBuilder.append("rows: ").append(resultSet.getLong(5)).append(", ");
                stringBuilder.append("data: ").append(resultSet.getLong(7) / ConstantsFor.KBYTE).append(" kilobytes, ");
                stringBuilder.append("comment: ").append(resultSet.getString(18)).append(".\n");
            }
            return stringBuilder.toString();
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("DatabaseInfo.databaseSchemaObjects", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace())));
            return e.getMessage();
        }
    }
    
    @Override
    public int uploadCollection(Collection stringsCollection, String tableName) {
        throw new TODOException("16.10.2019 (16:34)");
    }
    
    @Override
    public boolean dropTable(String dbPointTable) {
        throw new TODOException("16.10.2019 (16:34)");
    }
    
    @Override
    public int createTable(String dbPointTable, List<String> additionalColumns) {
        throw new TODOException("ru.vachok.networker.sysinfo.DatabaseInfo.createTable( int ) at 04.11.2019 - (13:51)");
    }
    
    @Override
    public MysqlDataSource getDataSource() {
        MysqlDataSource dataSource = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDataSource();
        dataSource.setDatabaseName("information_schema");
        return dataSource;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DatabaseInfo{");
        sb.append("option=").append(option);
        sb.append('}');
        return sb.toString();
    }
}
