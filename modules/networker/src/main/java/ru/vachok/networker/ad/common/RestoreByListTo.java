package ru.vachok.networker.ad.common;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 @since 12.09.2019 (10:40) */
public class RestoreByListTo {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, RestoreByListTo.class.getSimpleName());
    
    private Path pathToCopyRestored;
    
    @Contract(pure = true)
    public RestoreByListTo(Path pathToCopyRestored) {
        this.pathToCopyRestored = pathToCopyRestored;
    }
    
    public String restoreList() {
        StringBuilder stringBuilder = new StringBuilder();
        
        List<String> filesForRestore = getFilesList();
        
        return stringBuilder.toString();
    }
    
    private @NotNull List<String> getFilesList() {
        DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
        List<String> filesForRestore = new ArrayList<>();
        final String sql = "select * from restore";
        try (Connection connection = dataConnectTo.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                filesForRestore.add(resultSet.getString("upstring"));
            }
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 39");
        }
        return filesForRestore;
    }
}
