// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.abstr.Pinger;
import ru.vachok.networker.exe.runnabletasks.TemporaryFullInternet;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 Class ru.vachok.networker.exe.schedule.Do0213Monitor
 <p>
 
 @see ru.vachok.networker.exe.schedule.Do0213MonitorTest
 @since 07.07.2019 (9:07) */
public class Do0213Monitor implements Runnable, Pinger {
    
    
    private static final String SQL_FIRST = "INSERT INTO `u0466446_liferpg`.`worktime` (`Date`, `Timein`, `Timeout`) VALUES ('";
    
    private static Do0213Monitor do0213Monitor = new Do0213Monitor();
    
    private MysqlDataSource mySqlDataSource = new RegRuMysql().getDataSource();
    
    private long timeIn;
    
    private long elapsedMillis;
    
    private DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    public static Do0213Monitor getI() {
        return do0213Monitor;
    }
    
    @Override
    public void run() {
        mySqlDataSource.setUser("u0466446_kudr");
        mySqlDataSource.setPassword("36e42yoak8");
        mySqlDataSource.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_LIFERPG);
        if (isReach(ConstantsFor.HOSTNAME_DO213)) {
            logicRun();
            System.out.println("toString() = " + this);
        }
        else {
            System.err.println("(" + new Date() + ") NO PINGS = " + ConstantsFor.HOSTNAME_DO213);
        }
    }
    
    @Override
    public String getPingResultStr() {
        String fileResultsName = getClass().getSimpleName() + ".res";
        try (OutputStream outputStream = new FileOutputStream(fileResultsName, true);
             PrintStream printStream = new PrintStream(outputStream, true, "UTF-8")
        ) {
            printStream.println(getTimeToEndStr() + " " + LocalTime.now());
        }
        catch (IOException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".getPingResultStr");
        }
        return FileSystemWorker.readFile(fileResultsName);
    }
    
    @Override
    public String getTimeToEndStr() {
        long nineWorkHours = TimeUnit.HOURS.toMillis(9);
        
        return TimeUnit.MILLISECONDS.toMinutes(nineWorkHours - elapsedMillis) + " minutes left official";
    }
    
    @Override
    public boolean isReach(String inetAddrStr) {
        boolean retBool = false;
        try {
            byte[] inetAddressBytes = InetAddress.getByName(inetAddrStr).getAddress();
            InetAddress inetAddress = InetAddress.getByAddress(inetAddressBytes);
            retBool = inetAddress.isReachable(ConstantsFor.TIMEOUT_650 * 400);
        }
        catch (IOException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".isReach");
        }
        return retBool;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Do0213Monitor{");
        sb.append("dateFormat=").append(dateFormat.format(new Date()));
        sb.append(", mySqlDataSource=").append(mySqlDataSource.getDatabaseName());
        sb.append(", elapsedMillis=").append(elapsedMillis);
        sb.append('}');
        return sb.toString();
    }
    
    private void logicRun() {
        downloadLastPingFromDB();
    }
    
    private void downloadLastPingFromDB() {
        final String sql = "select * from worktime ORDER BY `worktime`.`recid` DESC limit 1 ";
        try (Connection connection = mySqlDataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()
        ) {
            while (resultSet.next()) {
                if (resultSet.last()) {
                    parseRS(resultSet);
                }
            }
        }
        catch (SQLException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".downloadLastPingFromDB");
        }
    }
    
    private void parseRS(@NotNull ResultSet rsFromDB) throws SQLException {
        String dateFromDB = rsFromDB.getString("Date");
        
        long timeinStamp = rsFromDB.getLong(ConstantsFor.DBFIELD_TIMEIN);
        long timeoutStamp = rsFromDB.getLong(ConstantsFor.DBFIELD_TIMEOUT);
    
        if ((timeoutStamp > 0) && (!dateCheck(dateFromDB) && isReach("10.200.213.85"))) {
            this.timeIn = ConstantsFor.getAtomicTime();
            System.out.println(new TemporaryFullInternet("do0213", 9, "add").call());
            uploadTimeinDB(sbSQLGet(timeIn, 0));
            monitorDO213(dateFromDB);
        }
        else {
            this.timeIn = timeinStamp;
        }
    }
    
    private boolean dateCheck(String dateFromDB) {
        return dateFromDB.equals(dateFormat.format(new Date()));
    }
    
    private @NotNull String sbSQLGet(long timeIn, long timeOut) {
        
        StringBuilder sbSQL = new StringBuilder()
            .append(SQL_FIRST)
            .append(dateFormat.format(new Date()))
            .append("', ")
            .append(timeIn)
            .append(", ").append(timeOut).append(");");
        return sbSQL.toString();
    }
    
    private void uploadTimeinDB(final String sql) {
        try (Connection connection = mySqlDataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            int rowsUpdate = preparedStatement.executeUpdate();
            System.out.println(getClass().getSimpleName() + " rowsUpdate = " + rowsUpdate);
        }
        catch (SQLException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".uploadTimeinDB");
        }
    }
    
    private void monitorDO213(String dateFromDB) {
        while (true) {
            boolean is213Reach = isReach("10.200.213.85");
            this.elapsedMillis = System.currentTimeMillis() - timeIn;
            if (!is213Reach) {
                uploadTimeinDB(sbSQLGet(540 - TimeUnit.MILLISECONDS.toMinutes(elapsedMillis), ConstantsFor.getAtomicTime()));
                break;
            }
            try {
                Thread.sleep(ConstantsFor.DELAY * 20);
            }
            catch (InterruptedException e) {
                Thread.currentThread().checkAccess();
                Thread.currentThread().interrupt();
                AppComponents.threadConfig().getTaskScheduler().scheduleWithFixedDelay(new Do0213Monitor(), new Date(), TimeUnit.MINUTES.toMillis(1));
            }
        }
    }
}