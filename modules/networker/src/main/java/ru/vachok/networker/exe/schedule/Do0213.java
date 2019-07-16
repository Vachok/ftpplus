// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.abstr.monitors.NetFactory;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.exe.runnabletasks.TemporaryFullInternet;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.message.DBMessenger;

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
import java.util.StringJoiner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 Class ru.vachok.networker.exe.schedule.Do0213
 <p>
 
 @see ru.vachok.networker.exe.schedule.Do0213MonitorTest
 @deprecated since 15.07.2019 (14:58)
 @since 07.07.2019 (9:07) */
public class Do0213 extends NetFactory {
    
    
    private static final String SQL_FIRST = "INSERT INTO `u0466446_liferpg`.`worktime` (`Date`, `Timein`, `Timeout`) VALUES ('";
    
    private static final String STR_MONITORING = " is not configured for monitoring.";
    
    protected static final String MIN_LEFT_OFFICIAL = " minutes left official";
    
    private int timeoutForPingSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(ConstantsFor.TIMEOUT_650 * ConstantsFor.ONE_YEAR);
    
    @Override
    public void setLaunchTimeOut(int launchTimeOut) {
        this.timeoutForPingSeconds = launchTimeOut;
    }
    
    private final Connection connection;
    
    private final DateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    
    private MessageToUser messageToUser = new DBMessenger(this.getClass().getSimpleName());
    
    private long timeIn;
    
    private long elapsedMillis;
    
    private @NotNull String hostName;
    
    public Do0213(String hostName) {
        this.hostName = hostName;
        this.connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
    }
    
    protected Do0213() {
        this.connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        this.hostName = "10.200.214.80";
    }
    
    public String getHostName() {
        return hostName;
    }
    
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
    
    @Override
    public Runnable getMonitoringRunnable() {
        if (!ConstantsFor.thisPC().toLowerCase().contains("rups")) {
        
        }
        else {
            throw new InvokeIllegalException(ConstantsFor.thisPC() + STR_MONITORING);
        }
    
        throw new InvokeIllegalException("Not ready");
    }
    
    @Override
    public String getStatistics() {
        throw new InvokeEmptyMethodException(getClass().getTypeName());
    }
    
    public boolean isReach(String inetAddrStr) {
        boolean retBool;
        try {
            byte[] inetAddressBytes = InetAddress.getByName(inetAddrStr).getAddress();
            InetAddress inetAddress = InetAddress.getByAddress(inetAddressBytes);
            System.out.println("PINGING = " + inetAddress);
            System.out.println("timeout seconds = " + timeoutForPingSeconds);
            retBool = inetAddress.isReachable(Math.toIntExact(TimeUnit.SECONDS.toMillis(timeoutForPingSeconds)));
        }
        catch (IOException e) {
            throw new InvokeIllegalException(ConstantsFor.thisPC() + STR_MONITORING);
        }
        return retBool;
    }
    
    @Override
    public String writeLogToFile() {
        throw new InvokeEmptyMethodException("12.07.2019 (16:36)");
    }
    
    @Override
    public void run() {
        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    }
    
    public String getPingResultStr() {
        String fileResultsName = getClass().getSimpleName() + ".res";
        try (OutputStream outputStream = new FileOutputStream(fileResultsName, true)) {
            try (PrintStream printStream = new PrintStream(outputStream, true, "UTF-8")) {
                printStream.println(getExecution() + " " + LocalTime.now());
            }
        }
        catch (IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".getPingResultStr", e));
        }
        return FileSystemWorker.readFile(fileResultsName);
    }
    
    public String getExecution() {
        long nineWorkHours = TimeUnit.HOURS.toMillis(9);
    
        return TimeUnit.MILLISECONDS.toMinutes(nineWorkHours - elapsedMillis) + MIN_LEFT_OFFICIAL;
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", Do0213.class.getSimpleName() + "[\n", "\n]")
            .add("timeout launcher sec = " + timeoutForPingSeconds)
            .add("timeIn = " + timeIn)
            .add("elapsedMillis = " + elapsedMillis)
            .add("simpleDateFormat = " + simpleDateFormat)
            .add("current inst = " + this.hashCode())
            .toString();
    }
    
    protected int getTimeoutForPingSeconds() {
        return timeoutForPingSeconds;
    }
    
    private void parseRS(@NotNull ResultSet rsFromDB) throws SQLException {
        String dateFromDB = rsFromDB.getString("Date");
        
        long timeinStamp = rsFromDB.getLong(ConstantsFor.DBFIELD_TIMEIN);
        long timeoutStamp = rsFromDB.getLong(ConstantsFor.DBFIELD_TIMEOUT);
    
        if ((timeoutStamp > 0) && (!dateCheck(dateFromDB) && isReach(hostName))) {
            timeIn = ConstantsFor.getAtomicTime();
            System.out.println(new TemporaryFullInternet(ConstantsFor.HOSTNAME_DO213, 9, "add").call());
            timeInUploadToDB(sbSQLGet(timeIn, 0));
            runMonitoringPC(dateFromDB);
        }
        else {
            timeIn = timeinStamp;
            runMonitoringPC(dateFromDB);
        }
    }
    
    private @NotNull
    String sbSQLGet(long timeIn, long timeOut) {
        StringBuilder sbSQL = new StringBuilder()
            .append(SQL_FIRST)
            .append(simpleDateFormat.format(new Date()))
            .append("', ")
            .append(timeIn)
            .append(", ").append(timeOut).append(");");
        return sbSQL.toString();
    }
    
    private void downloadLastPingFromDB() {
        final String sql = "select * from worktime ORDER BY `worktime`.`recid` DESC limit 1 ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            String clientInfo = connection.getMetaData().getURL();
            System.out.println(clientInfo);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    if (resultSet.last()) {
                        parseRS(resultSet);
                    }
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".downloadLastPingFromDB", e));
        }
    }
    
    private void runMonitoringPC(String dateFromDB) {
        if (isReach(hostName)) {
            do {
                try {
                    synchronized(this) {
                        elapsedMillis = System.currentTimeMillis() - timeIn;
                        wait(timeoutForPingSeconds + 5000);
                        if (!isReach(hostName)) {
                            notifyAll();
                        }
                    }
                }
                catch (InterruptedException | NullPointerException e) {
                    Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(getMonitoringRunnable());
                    
                    Thread.currentThread().checkAccess();
                    Thread.currentThread().interrupt();
                }
            } while (isReach(hostName));
        }
    }
    
    private boolean dateCheck(@NonNls @NotNull String dateFromDB) {
        return dateFromDB.equals(simpleDateFormat.format(new Date()));
    }
    
    private void timeInUploadToDB(final String sql) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            int rowsUpdate = preparedStatement.executeUpdate();
            System.out.println(getClass().getSimpleName() + " rowsUpdate = " + rowsUpdate);
        }
        catch (SQLException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".timeInUploadToDB");
        }
    
    }
}