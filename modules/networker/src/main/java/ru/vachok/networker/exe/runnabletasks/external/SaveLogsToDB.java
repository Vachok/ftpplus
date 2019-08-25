// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks.external;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.info.Stats;
import ru.vachok.networker.restapi.MessageToUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.StringJoiner;
import java.util.concurrent.Callable;


/**
 @see ru.vachok.networker.exe.runnabletasks.external.SaveLogsToDBTest
 @since 06.06.2019 (13:40) */
public class SaveLogsToDB extends Stats implements Callable<String> {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.DB, SaveLogsToDB.class.getSimpleName());
    
    private ru.vachok.stats.SaveLogsToDB logsToDB = new ru.vachok.stats.SaveLogsToDB();
    
    private int extTimeOut = 100;
    
    public int showInfo() {
        return getDBInfo() - AppComponents.getUserPref().getInt(this.getClass().getSimpleName(), 0);
    }
    
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
    
    @Override
    public String call() {
        return getInfo();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        SaveLogsToDB db = (SaveLogsToDB) o;
        
        if (extTimeOut != db.extTimeOut) {
            return false;
        }
        return logsToDB.equals(db.logsToDB);
    }
    
    @Override
    public int hashCode() {
        int result = logsToDB.hashCode();
        result = 31 * result + extTimeOut;
        return result;
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        Thread.currentThread().setName(this.getClass().getSimpleName());
        try {
            int i = Integer.parseInt(aboutWhat);
            return this.logsToDB.getInfoAbout(String.valueOf(i));
        }
        catch (NumberFormatException e) {
            return this.logsToDB.getInfoAbout("60");
        }
    }
    
    @Override
    public void setClassOption(@NotNull Object classOption) {
        this.extTimeOut = (int) classOption;
        this.logsToDB.setClassOption(classOption);
    }
    
    @Override
    public String getInfo() {
        return logsToDB.getInfoAbout(String.valueOf(extTimeOut));
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", SaveLogsToDB.class.getSimpleName() + "[\n", "\n]")
                .toString();
    }
}
