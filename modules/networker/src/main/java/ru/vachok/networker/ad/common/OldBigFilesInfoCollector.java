// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.common;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.*;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.*;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 Сбор информации о файла, в которые не заходили более 2 лет, и которые имеют размер более 25 мб.
 <p>
 Список папок-исключений: {@link ConstantsFor#EXCLUDED_FOLDERS_FOR_CLEANER}

 @see ru.vachok.networker.ad.common.OldBigFilesInfoCollectorTest
 @since 22.11.2018 (14:53) */
public class OldBigFilesInfoCollector implements Callable<String> {


    private static final String DOS_ARCHIVE = "dos:archive";

    private String reportUser;
    
    @NotNull private String startPath = "\\\\srv-fs.eatmeat.ru\\common_new";

    private long dirsCounter;

    private long filesCounter;
    
    private long totalFilesSize;
    
    private long filesMatched;
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, OldBigFilesInfoCollector.class.getSimpleName());
    
    public OldBigFilesInfoCollector() {
        this.reportUser = "Not completed yet";
    }
    
    @NotNull
    public String getStartPath() {
        return startPath;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OldBigFilesInfoCollector{");
        sb.append("totalFilesSize=").append(totalFilesSize);
        sb.append(", startPath='").append(startPath).append('\'');
        sb.append(", filesMatched=").append(filesMatched);
        sb.append(", filesCounter=").append(filesCounter);
        sb.append(", dirsCounter=").append(dirsCounter);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public String call() {
        Thread.currentThread().setName(this.getClass().getSimpleName());
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append(Files.walkFileTree(Paths.get(startPath), new OldBigFilesInfoCollector.WalkerCommon()));
        }
        catch (IOException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(AbstractForms.fromArray(e));
        }
        return stringBuilder.toString();
    }

    private void writeToDB(@NotNull Path file, float mByteSize, String attrArray) throws SQLException {
        DataConnectTo localDCT = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);
        List<String> cleanStop = FileSystemWorker.readFileToList(new File(FileNames.CLEANSTOP_TXT).getAbsolutePath());
        for (String stop : cleanStop) {
            if (file.toString().contains(stop)) {
                file = Paths.get("blackhole");
            }
        }
        try (Connection connection = localDCT.getDefaultConnection(ConstantsFor.DB_COMMONOLDFILES)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO oldfiles (AbsolutePath, size, Attributes) VALUES (?, ?, ?)")) {
                preparedStatement.setString(1, file.toAbsolutePath().normalize().toString());
                preparedStatement.setFloat(2, mByteSize);
                preparedStatement.setString(3, attrArray);
                preparedStatement.executeUpdate();
            }
        }
        this.reportUser = reportUser();
    }
    
    @NotNull
    private String reportUser() {
        String msg = MessageFormat.format("{0} total dirs, {1} total files scanned. Matched: {2} ({3} mb)",
                dirsCounter, filesCounter, filesMatched, totalFilesSize / ConstantsFor.MBYTE);
        messageToUser.warn(msg);
        String confirm = AppComponents.getMessageSwing(this.getClass().getSimpleName()).confirm(this.getClass().getSimpleName(), "Do you want to clean?", msg);
        if (confirm.equals("ok")) {
            new Cleaner();
        }
        else {
            writeToLog();
        }
        return msg;
    }

    private void writeToLog() {
        String logName = this.getClass().getSimpleName() + ".log";
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(logName))) {
            outputStream.write(reportUser.getBytes());
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage() + " see line: 118");
        }
    }

    /**
     @param attrs {@link BasicFileAttributes}
     @return более 15 мб и старше 2х лет.
     */
    private boolean more2MBOld(@NotNull BasicFileAttributes attrs) {
        return attrs.lastAccessTime().toMillis() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(ConstantsFor.ONE_YEAR * 2) && attrs.size() > ConstantsFor.MBYTE * 15;
    }

    private class WalkerCommon extends SimpleFileVisitor<Path> {


        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            dirsCounter += 1;
            if (Arrays.stream(ConstantsFor.getExcludedFoldersForCleaner()).anyMatch(tabooDir->dir.toAbsolutePath().normalize().toString().contains(tabooDir))) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            else {
                String toString = MessageFormat.format("Dirs: {0}, files: {3}/{2}. Current dir: {1}", dirsCounter, dir.toAbsolutePath().normalize(), filesCounter, filesMatched);
                return FileVisitResult.CONTINUE;
            }
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            filesCounter += 1;

            if (more2MBOld(attrs)) {
                Files.setAttribute(file, DOS_ARCHIVE, true);
                String attrArray = new TForms().fromArray(Files.readAttributes(file, "dos:*"));
                float mByteSize = (float) attrs.size() / ConstantsFor.MBYTE;
                try {
                    writeToDB(file, mByteSize, attrArray);
                }
                catch (SQLException | RuntimeException e) {
                    return FileVisitResult.CONTINUE;
                }
                finally {
                    filesMatched += 1;
                    totalFilesSize += attrs.size();
                }
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            messageToUser.warn(exc.getMessage() + " file: " + file.toAbsolutePath().normalize());
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            return FileVisitResult.CONTINUE;
        }
    }
}

