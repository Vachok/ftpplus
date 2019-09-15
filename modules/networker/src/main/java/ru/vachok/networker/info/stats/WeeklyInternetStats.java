// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info.stats;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.services.FilesZipPacker;
import ru.vachok.networker.componentsrepo.services.MyCalen;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.data.synchronizer.SyncData;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToTray;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.info.stats.WeeklyInternetStatsTest
 @since 20.05.2019 (9:36) */
class WeeklyInternetStats implements Runnable, Stats {
    
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());

    private long totalBytes = 0;

    private String fileName;

    private String sql;

    private InformationFactory informationFactory = InformationFactory.getInstance(INET_USAGE);
    
    @Override
    public void setClassOption(@NotNull Object option) {
        this.messageToUser = (MessageToUser) option;
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        return MessageFormat.format("I will NOT start before: {0}. {1}", MyCalen.getNextDayofWeek(0, 0, DayOfWeek.SUNDAY), this.getClass().getSimpleName());
    }
    
    @Override
    public String getInfo() {
        if (Stats.isSunday()) {
            run();
        }
        return toString();
    }
    
    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getSimpleName());
        
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.REGULAR_LOGS_SAVER);
        messageToUser.info(informationFactory.getInfo());
        long iPsWithInet = 0;
        try {
            iPsWithInet = readIPsWithInet();
        }
        catch (RuntimeException e) {
            messageToUser
                .error(MessageFormat.format("InternetStats.run {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
        
        String headerMsg = MessageFormat.format("{0} in kb. ", getClass().getSimpleName());
        String titleMsg = new File(FileNames.FILENAME_INETSTATSIPCSV).getAbsolutePath();
        String bodyMsg = " = " + iPsWithInet + " size in kb";
        
        messageToUser.info(headerMsg, titleMsg, bodyMsg);
        
        if (Stats.isSunday()) {
            readStatsToCSVAndDeleteFromDB();
            Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(new WeeklyInternetStats.InetStatSorter());
        }
        else {
            throw new InvokeIllegalException(LocalDate.now().getDayOfWeek().name() + " not best day for stats...");
        }
        
    }
    
    private long readIPsWithInet() {
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(ConstantsFor.SQL_SELECTINETSTATS)) {
                try (ResultSet r = preparedStatement.executeQuery()) {
                    makeIPFile(r);
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat
                .format("InternetStats.readIPsWithInet {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
        return new File(FileNames.FILENAME_INETSTATSIPCSV).length() / ConstantsFor.KBYTE;
    }
    
    @Override
    public String toString() {
        StringJoiner stringJoiner = new StringJoiner(",\n", WeeklyInternetStats.class.getSimpleName() + "[\n", "\n]");
        stringJoiner.add("totalBytes = " + totalBytes);
        if (!Stats.isSunday()) {
            stringJoiner.add(LocalDate.now().getDayOfWeek().name());
            stringJoiner.add(daySunCounter());
        }
        return stringJoiner.toString();
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
        
        WeeklyInternetStats stats = (WeeklyInternetStats) o;
        
        if (totalBytes != stats.totalBytes) {
            return false;
        }
        if (!messageToUser.equals(stats.messageToUser)) {
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
    
    private void readStatsToCSVAndDeleteFromDB() {
        List<String> chkIps = FileSystemWorker.readFileToList(new File(FileNames.FILENAME_INETSTATSIPCSV).getPath());
        for (String ip : chkIps) {
            messageToUser.info(writeObj(ip, "300000"));
        }
        new MessageToTray(this.getClass().getSimpleName()).info("ALL STATS SAVED\n", totalBytes / ConstantsFor.KBYTE + " Kb", fileName);
    }
    
    @Override
    public String writeObj(String ip, Object rowsLimit) {
        this.fileName = ip + "_" + LocalTime.now().toSecondOfDay() + ".csv";
        this.sql = new StringBuilder().append("SELECT * FROM `inetstats` WHERE `ip` LIKE '").append(ip).append("' LIMIT ").append(rowsLimit).toString();
        String retStr = downloadConcreteIPStatistics();
        File file = new File(fileName);
        this.totalBytes += file.length();
        
        retStr = MessageFormat.format("{0} file is {1}. Total kb: {2}", retStr, file.length() / ConstantsFor.KBYTE, totalBytes / ConstantsFor.KBYTE);
        
        if (Stats.isSunday() & file.length() > 10) {
            retStr = MessageFormat.format("{0} ||| {1} rows deleted.", retStr, deleteFrom(ip, (String) rowsLimit));
        }
        return retStr;
    }
    
    private void makeIPFile(@NotNull ResultSet r) throws SQLException {
        try (OutputStream outputStream = new FileOutputStream(FileNames.FILENAME_INETSTATSIPCSV)) {
            try (PrintStream printStream = new PrintStream(outputStream, true)) {
                while (r.next()) {
                    printStream.println(r.getString("ip"));
                }
            }
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        
    }

    private String downloadConcreteIPStatistics() {
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
            try (PreparedStatement p = connection.prepareStatement(sql)) {
                try (ResultSet r = p.executeQuery()) {
                    try (OutputStream outputStream = new FileOutputStream(fileName)) {
                        try (PrintStream printStream = new PrintStream(outputStream, true)) {
                            printToFile(r, printStream);
                        }
                    }
                }
            }
            return fileName;
        }
        catch (SQLException | IOException | OutOfMemoryError e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
            Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(new WeeklyInternetStats());
            return e.getMessage();
        }
    }
    
    protected long deleteFrom(String ip, String rowsLimit) {
        this.sql = new StringBuilder().append("DELETE FROM `inetstats` WHERE `ip` LIKE '").append(ip).append("' LIMIT ").append(rowsLimit).toString();
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
            try (PreparedStatement p = connection.prepareStatement(sql)) {
                return p.executeLargeUpdate();
            }
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage());
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
            Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(new WeeklyInternetStats());
        }
        return -1;
    }
    
    private void printToFile(@NotNull ResultSet r, PrintStream printStream) throws SQLException {
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
    
    private @NotNull String daySunCounter() {
        Date daySun = MyCalen.getNextDayofWeek(0, 0, DayOfWeek.SUNDAY);
        long sundayDiff = daySun.getTime() - System.currentTimeMillis();
        return MessageFormat.format("{0} ({1} hours left)", daySun.toString(), TimeUnit.MILLISECONDS.toHours(sundayDiff));
    }
    
    @Override
    public int hashCode() {
        int result = messageToUser.hashCode();
        result = 31 * result + (int) (totalBytes ^ (totalBytes >>> 32));
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + (sql != null ? sql.hashCode() : 0);
        result = 31 * result + informationFactory.hashCode();
        return result;
    }
    
    protected String getFileName() {
        return fileName;
    }
    
    protected void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    protected void setSql() {
        this.sql = ConstantsFor.SQL_SELECTINETSTATS;
    }
    
    private static class InetStatSorter implements Runnable {
        
        
        private ru.vachok.messenger.MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, getClass().getSimpleName());
        
        @Override
        public void run() {
            sortFiles();
            Future<String> submit = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(new FilesZipPacker());
            try {
                System.out.println(submit.get());
                trunkDB();
            }
            catch (InterruptedException | ExecutionException e) {
                messageToUser.error(e.getMessage());
            }
        }
        
        private void sortFiles() {
            File[] rootFiles = new File(".").listFiles();
            Map<File, String> mapFileStringIP = new TreeMap<>();
            
            for (File fileFromRoot : Objects.requireNonNull(rootFiles)) {
                if (fileFromRoot.getName().toLowerCase().contains(".csv")) {
                    try {
                        String[] nameSplit = fileFromRoot.getName().split("_");
                        mapFileStringIP.put(fileFromRoot, nameSplit[0].replace(".csv", ""));
                    }
                    catch (ArrayIndexOutOfBoundsException ignore) {
                        //
                    }
                }
            }
            if (mapFileStringIP.size() == 0) {
                FileSystemWorker.writeFile("no.csv", new Date().toString());
            }
            else {
                Set<String> ipsSet = new TreeSet<>(mapFileStringIP.values());
                ipsSet.forEach(ip->{
                    Collection<File> csvTMPFilesQueue = new LinkedList<>();
                    for (File file : mapFileStringIP.keySet()) {
                        if (file.getName().contains("_") & file.getName().contains(ip)) {
                            csvTMPFilesQueue.add(file);
                        }
                    }
                    makeCSV(ip, csvTMPFilesQueue);
                });
                FileSystemWorker.writeFile("inetips.set", ipsSet.stream());
            }
        }
        
        private void makeCSV(String ip, @NotNull Collection<File> queueCSVFilesFromRoot) {
            String fileSeparator = System.getProperty(PropertiesNames.PRSYS_SEPARATOR);
            String pathInetStats = Paths.get(".").toAbsolutePath().normalize() + fileSeparator + ConstantsFor.STR_INETSTATS + fileSeparator;
            File finalFile = new File(pathInetStats + ip + ".csv");
            checkDirExists(pathInetStats);
            Set<String> toWriteStatsSet = new HashSet<>();
            
            if (finalFile.exists() & queueCSVFilesFromRoot.size() > 0) {
                toWriteStatsSet.addAll(FileSystemWorker.readFileToSet(finalFile.toPath()));
            }
            if (queueCSVFilesFromRoot.size() > 0) {
                System.out.println("Adding statistics to: " + finalFile.getAbsolutePath());
                boolean isDelete = false;
                for (File nextFile : queueCSVFilesFromRoot) {
                    toWriteStatsSet.addAll(FileSystemWorker.readFileToSet(nextFile.toPath()));
                    isDelete = nextFile.delete();
                    if (!isDelete) {
                        nextFile.deleteOnExit();
                    }
                }
                boolean isWrite = FileSystemWorker.writeFile(finalFile.getAbsolutePath(), toWriteStatsSet.stream());
                System.out.println(isWrite + " write: " + finalFile.getAbsolutePath());
                System.out.println(isDelete + " deleted temp csv.");
                AppComponents.threadConfig().execByThreadConfig(()->{
                    saveToLocalDatabase(finalFile.getName().replace(".csv", ""));
                });
            }
            else {
                System.out.println(finalFile.getAbsolutePath() + " is NOT modified.");
            }
        }
        
        private void checkDirExists(String directoryName) {
            File inetStatsDirectory = new File(directoryName);
            if (!inetStatsDirectory.exists() || !inetStatsDirectory.isDirectory()) {
                try {
                    Files.createDirectories(inetStatsDirectory.toPath());
                }
                catch (IOException e) {
                    messageToUser.error(e.getMessage());
                }
            }
        }
        
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("InetStatSorter{");
            sb.append(LocalDate.now().getDayOfWeek());
            sb.append('}');
            return sb.toString();
        }
        
        private void trunkDB() {
            DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.DBUSER_NETWORK);
            try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
                 PreparedStatement preparedStatement = connection.prepareStatement("truncate table inetstats")) {
                preparedStatement.executeUpdate();
            }
            catch (SQLException e) {
                messageToUser.error(e.getMessage() + " see line: 368 ***");
            }
        }
    
        private void saveToLocalDatabase(String ip) {
            SyncData syncDB = SyncData.getInstance(Stats.DBUPLOAD);
            syncDB.setOption(ip);
            syncDB.syncData();
        }
    }
}
