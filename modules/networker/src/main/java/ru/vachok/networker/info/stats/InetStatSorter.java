package ru.vachok.networker.info.stats;


import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.services.FilesZipPacker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.data.synchronizer.SyncData;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 Class ru.vachok.networker.info.stats.InetStatSorter
 <p>

 @since 28.06.2020 (2:04) */
class InetStatSorter implements Runnable {


    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, InetStatSorter.class.getSimpleName());

    private Map<File, String> mapFileStringIP = new TreeMap<>();

    private Collection<File> csvTMPFilesQueue = new LinkedList<>();

    @Override
    public void run() {
        sortFiles();
        Future<String> submit = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(new FilesZipPacker());
        try {
            messageToUser.info(this.getClass().getSimpleName(), "running", submit.get());
            SyncData syncData = SyncData.getInstance("10.200.202.55");
            if (!new File(FileNames.WEEKLY_LCK).exists()) {
                AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().execute(syncData::superRun);
            }
            trunkDB();
        }
        catch (InterruptedException | ExecutionException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".run", e));
        }
    }

    private void sortFiles() {
        File[] rootFiles = new File(".").listFiles();
        this.mapFileStringIP = new TreeMap<>();
        for (File fileFromRoot : rootFiles) {
            if (fileFromRoot.getName().toLowerCase().contains(".csv")) {
                try {
                    String[] nameSplit = fileFromRoot.getName().split("_");
                    mapFileStringIP.put(fileFromRoot, nameSplit[0].replace(".csv", ""));
                }
                catch (RuntimeException e) {
                    messageToUser.warn(InetStatSorter.class.getSimpleName(), e.getMessage(), " see line: 363 ***");
                }
            }
        }
        if (mapFileStringIP.size() == 0) {
            FileSystemWorker.writeFile("no.csv", new Date().toString());
        }
        else {
            Set<String> ipsSet = new TreeSet<>(mapFileStringIP.values());
            ipsSet.forEach(this::makeFile);
            FileSystemWorker.writeFile(FileNames.INETIPS_SET, ipsSet.stream());
        }
    }

    private void trunkDB() {
        DataConnectTo dataConnectTo = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.DB_VELKOMINETSTATS);
             PreparedStatement preparedStatement = connection.prepareStatement("truncate table inetstats")) {
            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            messageToUser.warn(InetStatSorter.class.getSimpleName(), e.getMessage(), " see line: 384 ***");
        }
    }

    private void makeFile(String ip) {
        this.csvTMPFilesQueue = new LinkedList<>();
        for (File file : mapFileStringIP.keySet()) {
            if (file.getName().contains("_") & file.getName().contains(ip)) {
                csvTMPFilesQueue.add(file);
            }
        }
        makeCSV(ip);
    }

    private void makeCSV(String ip) {
        String fileSeparator = System.getProperty(PropertiesNames.SYS_SEPARATOR);
        String pathInetStats = Paths.get(".").toAbsolutePath().normalize() + fileSeparator + FileNames.DIR_INETSTATS + fileSeparator;
        File finalFile = new File(pathInetStats + ip + ".csv");
        checkDirExists(pathInetStats);
        Set<String> toWriteStatsSet = new TreeSet<>();
        if (finalFile.exists() & csvTMPFilesQueue.size() > 0) {
            toWriteStatsSet.addAll(FileSystemWorker.readFileToSet(finalFile.toPath()));
        }
        if (csvTMPFilesQueue.size() > 0) {
            messageToUser.info(this.getClass().getSimpleName(), "Adding statistics to: ", finalFile.getAbsolutePath());
            boolean isDelete = false;
            for (File nextFile : csvTMPFilesQueue) {
                if (nextFile.length() >= 2) {
                    toWriteStatsSet.addAll(FileSystemWorker.readFileToSet(nextFile.toPath()));
                }
                isDelete = nextFile.delete();
                if (!isDelete) {
                    nextFile.deleteOnExit();
                }
            }
            boolean isWrite = FileSystemWorker.writeFile(finalFile.getAbsolutePath(), toWriteStatsSet.stream().sorted());
            messageToUser.info(String.valueOf(isWrite), " write: ", finalFile.getAbsolutePath());
            messageToUser.info(this.getClass().getSimpleName(), String.valueOf(isDelete), " deleted temp csv.");
        }
        else {
            messageToUser.warn(this.getClass().getSimpleName(), finalFile.getAbsolutePath(), " is NOT modified.");
        }
    }

    private void checkDirExists(String directoryName) {
        File inetStatsDirectory = new File(directoryName);
        if (!inetStatsDirectory.exists() || !inetStatsDirectory.isDirectory()) {
            try {
                Files.createDirectories(inetStatsDirectory.toPath());
            }
            catch (IOException e) {
                messageToUser.warn(InetStatSorter.class.getSimpleName(), e.getMessage(), " see line: 440 ***");
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
}