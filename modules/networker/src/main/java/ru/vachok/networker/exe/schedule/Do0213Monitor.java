// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.abstr.Pinger;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 Class ru.vachok.networker.exe.schedule.Do0213Monitor
 <p>
 
 @see ru.vachok.networker.exe.schedule.Do0213MonitorTest
 @since 07.07.2019 (9:07) */
public class Do0213Monitor implements Runnable, Pinger {
    
    
    private MysqlDataSource mySqlDataSource = new RegRuMysql().getDataSource();
    
    private long timeInCounting;
    
    public long getTimeInCounting() {
        return timeInCounting;
    }
    
    @Override public void run() {
        mySqlDataSource.setUser("u0466446_kudr");
        mySqlDataSource.setPassword("36e42yoak8");
        mySqlDataSource.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_LIFERPG);
        logicRun();
    }
    
    @Override public String getPingResultStr() {
        String fileResultsName = getClass().getSimpleName() + ".res";
        try (OutputStream outputStream = new FileOutputStream(fileResultsName, true);
             PrintStream printStream = new PrintStream(outputStream, true, "UTF-8")) {
            printStream.println(getTimeToEndStr() + " " + LocalTime.now());
        }
        catch (IOException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".getPingResultStr");
        }
        return FileSystemWorker.readFile(fileResultsName);
    }
    
    @Override public String getTimeToEndStr() {
        return TimeUnit.MILLISECONDS.toMinutes(timeInCounting) + " minutes left official";
    }
    
    @Override public boolean isReach(String inetAddrStr) {
        boolean retBool = false;
        try {
            InetAddress inetAddress = InetAddress.getByName(inetAddrStr);
            retBool = inetAddress.isReachable(ConstantsFor.TIMEOUT_650);
        }
        catch (IOException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".isReach");
        }
        return retBool;
    }
    
    private void logicRun() {
        downloadLastPingFromDB();
    }
    
    private void downloadLastPingFromDB() {
        final String sql = "select * from worktime";
        try (Connection connection = mySqlDataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                parseRS(resultSet);
            }
        }
        catch (SQLException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".downloadLastPingFromDB");
        }
    }
    
    private void parseRS(ResultSet rsFromDB) throws SQLException {
        String dateFromDB = rsFromDB.getString("Date");
        long timeinStamp = rsFromDB.getLong("Timein");
        long timeoutStamp = rsFromDB.getLong("Timeout");
        if (timeoutStamp > 0) {
            uploadTimeinDB(new StringBuilder().append("INSERT INTO `u0466446_liferpg`.`worktime` (`Date`, `Timein`, `Timeout`) VALUES ('").append(new Date().toString()).append("', ")
                .append(ConstantsFor.getAtomicTime()).append(", ").append(0).append(");").toString());
        }
        else {
            monitorDO213(timeinStamp);
        }
    }
    
    private void monitorDO213(final long timeinStamp) {
        while (true) {
            boolean is213Reach = isReach(ConstantsFor.HOSTNAME_DO213);
            if (!is213Reach) {
                uploadTimeinDB(new StringBuilder().append("INSERT INTO `u0466446_liferpg`.`worktime` (`Date`, `Timein`, `Timeout`) VALUES ('").append(new Date().toString()).append("', ")
                    .append(0).append(", ").append(ConstantsFor.getAtomicTime()).append(");").toString());
            }
            this.timeInCounting = System.currentTimeMillis() - timeinStamp;
            try {
                Thread.sleep(500);
            }
            catch (InterruptedException e) {
                Thread.currentThread().checkAccess();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    private void uploadTimeinDB(final String sql) {
        try (Connection connection = mySqlDataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int rowsUpdate = preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".uploadTimeinDB");
        }
    }
}