// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks.external;


import org.jetbrains.annotations.Contract;
import ru.vachok.networker.*;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.*;
import java.text.MessageFormat;
import java.util.StringJoiner;
import java.util.concurrent.*;


/**
 @see ru.vachok.networker.exe.runnabletasks.external.SaveLogsToDBTest
 @since 06.06.2019 (13:40) */
public class SaveLogsToDB implements Runnable, ru.vachok.stats.InformationFactory, InformationFactory, Callable<String> {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.DB, SaveLogsToDB.class.getSimpleName());
    
    private static final int START_ID = new SaveLogsToDB().getLastRecordID();
    
    private ru.vachok.stats.SaveLogsToDB logsToDB = new ru.vachok.stats.SaveLogsToDB();
    
    private int extTimeOut = 100;
    
    public int getIDDifferenceWhileAppRunning() {
        int difference = getLastRecordID() - START_ID;
        UsefulUtilities.setPreference(AppInfoOnLoad.class.getSimpleName(), String.valueOf(difference));
        return difference;
    }
    
    public static int getLastRecordID() {
        int retInt = START_ID;
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
    
    public String saveAccessLogToDatabaseWithTimeOut(String timeOut) {
        this.extTimeOut = Integer.parseInt(timeOut);
        ExecutorService option = Executors.newSingleThreadExecutor();
        this.logsToDB.setClassOption(option);
        Thread.currentThread().setName(this.getClass().getSimpleName());
        try {
            long i = Long.parseLong(timeOut);
            return this.logsToDB.getInfoAbout(String.valueOf(i));
        }
        catch (NumberFormatException e) {
            return this.logsToDB.getInfoAbout("60");
        }
    }
    
    @Override
    public String call() {
        return saveAccessLogToDatabase();
    }
    
    @Override
    public void run() {
        saveAccessLogToDatabase();
    }
    
    public String saveAccessLogToDatabase() {
        return logsToDB.getInfoAbout(String.valueOf(extTimeOut));
    }
    
    @Override
    public int hashCode() {
        int result = logsToDB.hashCode();
        result = 31 * result + extTimeOut;
        return result;
    }
    
    @Contract(value = "null -> false", pure = true)
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
    public String toString() {
        return new StringJoiner(",\n", SaveLogsToDB.class.getTypeName() + "[\n", "\n]")
                .toString();
    }
    
    @Override
    public String getInfoAbout(String s) {
        throw new TODOException("ru.vachok.networker.exe.runnabletasks.external.SaveLogsToDB.getInfoAbout created 28.08.2019 (17:26)");
    }
    
    @Override
    public void setClassOption(Object option) {
        if (option instanceof Integer) {
            this.extTimeOut = (int) option;
        }
        else {
            throw new InvokeIllegalException("Must be Integer");
        }
    }
    
    @Override
    public String getInfo() {
        return saveAccessLogToDatabase();
    }
    
}
