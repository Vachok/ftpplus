// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info.stats;


import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.services.MyCalen;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToTray;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;


/**
 @see ru.vachok.networker.info.stats.WeeklyInternetStatsTest
 @since 20.05.2019 (9:36) */
final class WeeklyInternetStats implements Runnable, Stats {


    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, WeeklyInternetStats.class.getSimpleName());

    private static final File IPS_WITH_INET = new File(FileNames.INETSTATSIP_CSV);

    private long totalBytes = 0;

    private String fileName;

    private String sql;

    private InformationFactory informationFactory = InformationFactory.getInstance(INET_USAGE);

    protected String getFileName() {
        return fileName;
    }

    @Override
    public String getInfoAbout(String aboutWhat) {
        return Stats.isSunday() ? getInfo() : MessageFormat
            .format("I will NOT start before: {0}. {1}", MyCalen.getNextDayofWeek(0, 0, DayOfWeek.SUNDAY), this.getClass().getSimpleName());
    }

    @Override
    public String getInfo() {
        if (Stats.isSunday()) {
            run();
        }
        return toString();
    }

    @Override
    public void setClassOption(@NotNull Object option) {
        if (option instanceof InformationFactory) {
            this.informationFactory = (InformationFactory) option;
        }
        else {
            throw new IllegalArgumentException(WeeklyInternetStats.class.getSimpleName());
        }
    }

    @Override
    public void run() {

        long iPsWithInet;
        try {
            iPsWithInet = readIPsWithInet(false);
            messageToUser.info(getClass().getSimpleName(), String.valueOf(iPsWithInet), " iPsWithInet");
        }
        catch (RuntimeException e) {
            messageToUser.error("WeeklyInternetStats.run", e.getMessage(), AbstractForms.networkerTrace(e));
        }
        try {
            execDo();
        }
        catch (InvokeIllegalException e) {
            messageToUser.warn(WeeklyInternetStats.class.getSimpleName(), e.getMessage(), " see line: 118 ***");
        }
    }

    /**
     @return inetstatsIP.csv length in bytes.

     @see WeeklyInternetStatsTest#testReadIPsWithInet
     */
    long readIPsWithInet(boolean isNoSquidNeedRead) {
        Thread.currentThread().setName("readIPsWithInet");
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_VELKOMINETSTATS)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(ConstantsFor.SQL_SELECTINETSTATS)) {
                try (ResultSet r = preparedStatement.executeQuery()) {
                    synchronized(IPS_WITH_INET) {
                        makeIPFile(r, isNoSquidNeedRead);
                    }
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 112 ***");
        }
        return new File(FileNames.INETSTATSIP_CSV).length();
    }

    @Override
    public String toString() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(PropertiesNames.CLASS, getClass().getSimpleName());
        jsonObject.add(PropertiesNames.HASH, this.hashCode());
        jsonObject.add(PropertiesNames.TIMESTAMP, System.currentTimeMillis());
        jsonObject.add("IPS_WITH_INET", IPS_WITH_INET.getAbsolutePath());
        jsonObject.add("totalBytes", totalBytes);
        jsonObject.add("fileName", fileName);
        jsonObject.add("sql", sql);
        jsonObject.add("informationFactory", informationFactory.toString());
        return jsonObject.toString();
    }

    private void makeIPFile(@NotNull ResultSet resultSet, boolean isNoSquidNeedRead) throws SQLException {
        try (OutputStream outputStream = new FileOutputStream(IPS_WITH_INET)) {
            try (PrintStream printStream = new PrintStream(outputStream, true)) {
                while (resultSet.next()) {
                    String ip = resultSet.getString("ip");
                    printStream.println(ip);
                }
            }
        }
        catch (IOException e) {
            messageToUser.warn(WeeklyInternetStats.class.getSimpleName(), e.getMessage(), " see line: 220 ***");
        }
        finally {
            if (isNoSquidNeedRead) {
                readNoSquidIPs();
            }
        }
    }

    private void readNoSquidIPs() {
        Set<String> ipsList = new LinkedHashSet<>();
        SSHFactory build = new SSHFactory.Builder(new AppComponents().sshActs().whatSrvNeed(),
            "sudo cat /etc/pf/vipnet;sudo cat /etc/pf/24hrs;exit", this.getClass().getSimpleName()).build();
        String[] vipNetIPs;
        try {
            vipNetIPs = build.call().split("\n");
        }
        catch (RuntimeException e) {
            vipNetIPs = new String[]{""};
        }
        for (String netIP : vipNetIPs) {
            String ip = getIP(netIP);
            if (!ip.isEmpty()) {
                ipsList.add(ip);
            }
        }
        ipsList.forEach(ip->FileSystemWorker.appendObjectToFile(IPS_WITH_INET, ip));
    }

    private String getIP(String ip) {
        try {
            ip = ip.split("#")[0];
        }
        catch (IndexOutOfBoundsException e) {
            ip = ip.replace("<br>", "");
        }
        finally {
            ip = ip.replace("<br>", "");
        }
        return ip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WeeklyInternetStats)) {
            return false;
        }

        WeeklyInternetStats stats = (WeeklyInternetStats) o;

        if (totalBytes != stats.totalBytes) {
            return false;
        }
        if (fileName != null ? !fileName.equals(stats.fileName) : stats.fileName != null) {
            return false;
        }
        if (sql != null ? !sql.equals(stats.sql) : stats.sql != null) {
            return false;
        }
        return informationFactory.equals(stats.informationFactory);
    }

    protected long deleteFrom(String ip, String rowsLimit) {
        this.sql = new StringBuilder().append("DELETE FROM `inetstats` WHERE `ip` LIKE '").append(ip).append(ConstantsFor.LIMIT).append(rowsLimit).toString();
        try (Connection connection = DataConnectTo.getDefaultI().getDefaultConnection(String.format("%s.%s", ConstantsFor.STR_VELKOM, FileNames.DIR_INETSTATS))) {
            try (PreparedStatement p = connection.prepareStatement(sql)) {
                return p.executeLargeUpdate();
            }
        }
        catch (SQLException e) {
            messageToUser.warn(WeeklyInternetStats.class.getSimpleName(), e.getMessage(), " see line: 281 ***");
            Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(new WeeklyInternetStats());
        }
        return -1;
    }

    @Override
    public int hashCode() {
        int result = (int) (totalBytes ^ (totalBytes >>> 32));
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + (sql != null ? sql.hashCode() : 0);
        result = 31 * result + informationFactory.hashCode();
        return result;
    }

    private void execDo() throws InvokeIllegalException {
        if (ConstantsFor.argNORUNExist(ConstantsFor.REGRUHOSTING_PC) || ConstantsFor.argNORUNExist()) {
            throw new InvokeIllegalException(UsefulUtilities.thisPC());
        }
        if (!new File(FileNames.WEEKLY_LCK).exists() && Stats.isSunday()) {
            readStatsToCSVAndDeleteFromDB();
            AppConfigurationLocal.getInstance().execute(new InetStatSorter());
        }
        else {
            messageToUser.warn(MessageFormat.format("Not approved: \n{0} weekly.lck\n{1} isSunday", new File(FileNames.WEEKLY_LCK).exists(), Stats.isSunday()));
        }
    }

    private void readStatsToCSVAndDeleteFromDB() {
        List<String> chkIps = FileSystemWorker.readFileToList(new File(FileNames.INETSTATSIP_CSV).getPath());
        for (String ip : chkIps) {
            messageToUser.info(writeObj(ip, "300000"));
        }
        new MessageToTray(this.getClass().getSimpleName()).info("ALL STATS SAVED\n", totalBytes / ConstantsFor.KBYTE + " Kb", fileName);
    }

    private String downloadConcreteIPStatistics() {
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I)
            .getDefaultConnection(String.format("%s.%s", ConstantsFor.STR_VELKOM, FileNames.DIR_INETSTATS))) {
            try (PreparedStatement p = connection.prepareStatement(sql)) {
                try (ResultSet r = p.executeQuery()) {
                    try (OutputStream outputStream = new FileOutputStream(fileName)) {
                        try (PrintStream printStream = new PrintStream(outputStream, true)) {
                            printConcreteIPToFile(r, printStream);
                        }
                    }
                }
            }
            return fileName;
        }
        catch (SQLException | IOException | OutOfMemoryError e) {
            Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(new WeeklyInternetStats());
            messageToUser.warn(WeeklyInternetStats.class.getSimpleName(), e.getMessage(), " see line: 268 ***");
            return e.getMessage();
        }
    }

    @Override
    public String writeObj(String ip, Object rowsLimit) {
        this.fileName = ip + "_" + LocalTime.now().toSecondOfDay() + ".csv";
        this.sql = new StringBuilder().append("SELECT * FROM `inetstats` WHERE `ip` LIKE '").append(ip).append(ConstantsFor.LIMIT).append(rowsLimit).toString();
        String retStr = downloadConcreteIPStatistics();
        File file = new File(fileName);
        this.totalBytes += file.length();

        retStr = MessageFormat.format("{0} file is {1}. Total kb: {2}", retStr, file.length() / ConstantsFor.KBYTE, totalBytes / ConstantsFor.KBYTE);

        if (Stats.isSunday() & file.length() > 10) {
            retStr = MessageFormat.format("{0} ||| {1} rows deleted.", retStr, deleteFrom(ip, (String) rowsLimit));
        }
        return retStr;
    }

    private void printConcreteIPToFile(@NotNull ResultSet r, PrintStream printStream) throws SQLException {
        while (r.next()) {
            printStream.print(new java.util.Date(Long.parseLong(r.getString("Date"))));
            printStream.print(",");
            printStream.print(r.getString(ConstantsFor.DBCOL_RESPONSE));
            printStream.print(",");
            printStream.print(r.getString(ConstantsFor.DBCOL_BYTES));
            printStream.print(",");
            printStream.print(r.getString(ConstantsFor.DBFIELD_METHOD));
            printStream.print(",");
            printStream.print(r.getString("site"));
            printStream.println();
        }
    }

    protected void setSql() {
        this.sql = ConstantsFor.SQL_SELECTINETSTATS;
    }

}
