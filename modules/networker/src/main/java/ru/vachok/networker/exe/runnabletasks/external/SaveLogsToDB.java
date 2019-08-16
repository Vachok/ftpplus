// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks.external;


import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.DBMessenger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.StringJoiner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;


/**
 @since 06.06.2019 (13:40) */
public class SaveLogsToDB implements InformationFactory, Callable<String> {
    
    
    private static final ru.vachok.stats.InformationFactory LOGS_TO_DB_EXT = new ru.vachok.stats.SaveLogsToDB();
    
    private static final MessageToUser messageToUser = DBMessenger.getInstance(SaveLogsToDB.class.getSimpleName());
    
    public int getDBInfo() {
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
        return retInt;
    }
    
    public int showInfo() {
        return getDBInfo() - AppComponents.getUserPref().getInt(this.getClass().getSimpleName(), 0);
    }
    
    @Override
    public String call() {
        return LOGS_TO_DB_EXT.getInfoAbout("100");
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        try {
            int i = Integer.parseInt(aboutWhat);
            return LOGS_TO_DB_EXT.getInfoAbout(String.valueOf(i));
        }
        catch (NumberFormatException e) {
            return LOGS_TO_DB_EXT.getInfoAbout("60");
        }
    }
    
    @Override
    public void setClassOption(Object classOption) {
        ExecutorService executorService = (ExecutorService) classOption;
        LOGS_TO_DB_EXT.setClassOption(executorService);
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", SaveLogsToDB.class.getSimpleName() + "[\n", "\n]")
            .toString();
    }
}
