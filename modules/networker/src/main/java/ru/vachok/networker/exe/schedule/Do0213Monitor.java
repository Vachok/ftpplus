// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.abstr.Pinger;
import ru.vachok.networker.exe.runnabletasks.TemporaryFullInternet;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.DBMessenger;

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
 Class ru.vachok.networker.exe.schedule.Do0213Monitor
 <p>
 
 @see ru.vachok.networker.exe.schedule.Do0213MonitorTest
 @since 07.07.2019 (9:07) */
public class Do0213Monitor implements Runnable, Pinger {
    
    
    private static final String SQL_FIRST = "INSERT INTO `u0466446_liferpg`.`worktime` (`Date`, `Timein`, `Timeout`) VALUES ('";
    
    protected static final String MIN_LEFT_OFFICIAL = " minutes left official";
    
    protected final int timeoutForPing = ConstantsFor.TIMEOUT_650 * ConstantsFor.ONE_YEAR;
    
    @SuppressWarnings("StaticVariableOfConcreteClass")
    private static Do0213Monitor do0213Monitor = new Do0213Monitor();
    
    private MessageToUser messageToUser = new DBMessenger(getClass().getSimpleName());
    
    private final Connection connection;
    
    private long timeIn = 0L;
    
    private long elapsedMillis = 0L;
    
    private DateFormat dateFormat;
    
    protected Do0213Monitor() {
        connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        //noinspection SimpleDateFormatWithoutLocale
        dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    }
    
    @SuppressWarnings("SuspiciousGetterSetter")
    @Contract(pure = true)
    public static Do0213Monitor getI() {
        return do0213Monitor;
    }
    
    @Override
    public void run() {
        if (ConstantsFor.thisPC().toLowerCase().contains("rups")) {
            messageToUser.info(launchSchedule(this::downloadLastPingFromDB));
            System.out.println("toString() = " + this);
        }
        else {
            System.err.println("(" + new Date() + ") NO PINGS = " + ConstantsFor.HOSTNAME_DO213);
        }
    }
    
    @Override
    public String getPingResultStr() {
        String fileResultsName = getClass().getSimpleName() + ".res";
        try (OutputStream outputStream = new FileOutputStream(fileResultsName, true)) {
            try (PrintStream printStream = new PrintStream(outputStream, true, "UTF-8")) {
                printStream.println(getTimeToEndStr() + " " + LocalTime.now());
            }
        }
        catch (IOException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".getPingResultStr");
        }
        return FileSystemWorker.readFile(fileResultsName);
    }
    
    @Override
    public String getTimeToEndStr() {
        long nineWorkHours = TimeUnit.HOURS.toMillis(9);
    
        return TimeUnit.MILLISECONDS.toMinutes(nineWorkHours - elapsedMillis) + MIN_LEFT_OFFICIAL;
    }
    
    @Override
    public boolean isReach(String inetAddrStr) {
        boolean retBool = false;
        try {
            byte[] inetAddressBytes = InetAddress.getByName(inetAddrStr).getAddress();
            InetAddress inetAddress = InetAddress.getByAddress(inetAddressBytes);
            retBool = inetAddress.isReachable(timeoutForPing);
        }
        catch (IOException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".isReach");
        }
        return retBool;
    }
    
    private void parseRS(@NotNull ResultSet rsFromDB) throws SQLException {
        String dateFromDB = rsFromDB.getString("Date");
        
        long timeinStamp = rsFromDB.getLong(ConstantsFor.DBFIELD_TIMEIN);
        long timeoutStamp = rsFromDB.getLong(ConstantsFor.DBFIELD_TIMEOUT);
    
        if ((timeoutStamp > 0) && (!dateCheck(dateFromDB) && isReach("10.200.213.85"))) {
            this.timeIn = ConstantsFor.getAtomicTime();
            System.out.println(new TemporaryFullInternet(ConstantsFor.HOSTNAME_DO213, 9, "add").call());
            timeInUploadToDB(sbSQLGet(timeIn, 0));
            runMonitoringPC(dateFromDB);
        }
        else {
            this.timeIn = timeinStamp;
            runMonitoringPC(dateFromDB);
        }
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
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", Do0213Monitor.class.getSimpleName() + "[\n", "\n]")
            .add("timeoutForPing = " + timeoutForPing)
            .add("timeIn = " + timeIn)
            .add("elapsedMillis = " + elapsedMillis)
            .add("dateFormat = " + dateFormat)
            .toString();
    }
    
    private void downloadLastPingFromDB() {
        final String sql = "select * from worktime ORDER BY `worktime`.`recid` DESC limit 1 ";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    if (resultSet.last()) {
                        parseRS(resultSet);
                    }
                }
            }
        }
        catch (SQLException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".downloadLastPingFromDB");
        }
    }
    
    private void runMonitoringPC(String dateFromDB) {
        while (true) {
            boolean is213Reach = isReach("10.200.213.85");
            this.elapsedMillis = System.currentTimeMillis() - timeIn;
            if (!is213Reach) {
                timeInUploadToDB(sbSQLGet(ConstantsFor.MINUTES_IN_STD_WORK_DAY - TimeUnit.MILLISECONDS.toMinutes(elapsedMillis), ConstantsFor.getAtomicTime()));
                break;
            }
            try {
                Thread.sleep(ConstantsFor.DELAY * 10);
            }
            catch (InterruptedException e) {
                initNewThread();
            }
        }
    }
    
    private void initNewThread() {
        messageToUser.infoNoTitles("Do0213Monitor.initNewThread");
        launchSchedule(new Do0213Monitor());
        Thread.currentThread().checkAccess();
        Thread.currentThread().interrupt();
    }
    
    private String launchSchedule(Runnable thisOrNew) {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleWithFixedDelay(thisOrNew, 0, timeoutForPing, TimeUnit.SECONDS);
        return service.toString();
    }
    
    private boolean dateCheck(@NonNls @NotNull String dateFromDB) {
        return dateFromDB.equals(dateFormat.format(new Date()));
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