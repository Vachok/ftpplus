// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks.external;


import org.jetbrains.annotations.Contract;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppInfoOnLoad;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.stats.data.DataConnectTo;

import java.sql.*;
import java.text.MessageFormat;
import java.util.StringJoiner;
import java.util.concurrent.Callable;


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
        int result = -1;
        try (Connection connection = ru.vachok.networker.restapi.database.DataConnectTo.getDefaultI().getDefaultConnection(ConstantsFor.DB_VELKOMINETSTATS);
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT idrec FROM inetstats ORDER BY idrec DESC LIMIT 1;");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            //noinspection LoopStatementThatDoesntLoop
            while (resultSet.next()) {
                result = resultSet.getInt(1);
                break;
            }
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("SaveLogsToDB.getLastRecordID", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace())));
            result = 0 - e.getErrorCode();
        }
        return result;
    }
    
    public String saveAccessLogToDatabaseWithTimeOut(String timeOut) {
        this.extTimeOut = Integer.parseInt(timeOut);
        ru.vachok.stats.data.DataConnectTo option = DataConnectTo.getInstance("SQLSrvInetstat");
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
