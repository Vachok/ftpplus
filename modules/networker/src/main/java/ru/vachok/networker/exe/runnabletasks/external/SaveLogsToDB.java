// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks.external;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.DBMessenger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;


/**
 @since 06.06.2019 (13:40) */
public class SaveLogsToDB implements Runnable {
    
    protected static final String CLEANED = "Cleaned: ";
    
    private static final ru.vachok.stats.SaveLogsToDB LOGS_TO_DB_EXT = new ru.vachok.stats.SaveLogsToDB();
    
    private static final MessageToUser messageToUser = DBMessenger.getInstance(SaveLogsToDB.class.getSimpleName());
    
    private static final int DB_LAST_ID = new SaveLogsToDB().showInfo();
    
    @Contract(pure = true)
    public static ru.vachok.stats.SaveLogsToDB getI() {
        return LOGS_TO_DB_EXT;
    }
    
    public static void startScheduled() {
        messageToUser.info(LOGS_TO_DB_EXT.startScheduled());
    }
    
    public @NotNull int showInfo() {
        int retInt = 0;
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `inetstats` ORDER BY `inetstats`.`idrec` DESC LIMIT 1");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                retInt = resultSet.getInt("idrec");
            }
        }
        catch (SQLException e) {
            messageToUser
                .error(MessageFormat.format("SaveLogsToDB.showInfo {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
        return retInt - DB_LAST_ID;
    }
    
    @Override
    public void run() {
        LOGS_TO_DB_EXT.startScheduled();
    }
}
