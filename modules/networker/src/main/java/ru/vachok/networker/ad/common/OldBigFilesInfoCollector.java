// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.common;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.*;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 Сбор информации о файла, в которые не заходили более 2 лет, и которые имеют размер более 25 мб.
 <p>
 Список папок-исключений: {@link ConstantsFor#EXCLUDED_FOLDERS_FOR_CLEANER}
 
 @see ru.vachok.networker.ad.common.OldBigFilesInfoCollectorTest
 @since 22.11.2018 (14:53) */
@Service
public class OldBigFilesInfoCollector implements Callable<String> {
    
    
    private static final String DOS_ARCHIVE = "dos:archive";
    
    private String reportUser;
    
    private @NotNull String startPath = "\\\\srv-fs.eatmeat.ru\\common_new";
    
    private long dirsCounter;
    
    private long filesCounter;
    
    private long totalFilesSize;
    
    private long filesMatched;
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
    private PreparedStatement preparedStatement;
    
    public OldBigFilesInfoCollector() {
        this.reportUser="Not comleted yet";
    }
    
    public @NotNull String getStartPath() {
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
        return startSearch();
    }
    
    private @NotNull String startSearch() {
        MysqlDataSource source = DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT).getDataSource();
        try (Connection connection = source.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `velkom`.`oldfiles` (`AbsolutePath`, `size`, `Attributes`) VALUES (?, ?, ?)")) {
                this.preparedStatement = preparedStatement;
                
                Files.walkFileTree(Paths.get(startPath), new WalkerCommon());
            }
        }
        catch (IOException | RuntimeException | SQLException e) {
            messageToUser.error(MessageFormat
                    .format("OldBigFilesInfoCollector.startSearch {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
    
        this.reportUser = reportUser();
        return reportUser;
    }
    
    private @NotNull String reportUser() {
        String msg = MessageFormat.format("{0} total dirs, {1} total files scanned. Matched: {2} ({3} mb)",
                dirsCounter, filesCounter, filesMatched, totalFilesSize / ConstantsFor.MBYTE);
        messageToUser.warn(msg);
        String confirm = AppComponents.getMessageSwing(this.getClass().getSimpleName()).confirm(this.getClass().getSimpleName(), "Do you want to clean?", msg);
        if (confirm.equals("ok")) {
            new Cleaner();
        }else{
            writeToLog();
        }
        return msg;
    }
    
    private void writeToLog() {
        String logName = this.getClass().getSimpleName() + ".log";
        try(OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(logName))){
            outputStream.write(reportUser.getBytes());
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage() + " see line: 118");
        }
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
                
                sendToDB(file, mByteSize, attrArray);
                
                filesMatched += 1;
                totalFilesSize += attrs.size();
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
    
    private void sendToDB(@NotNull Path file, float mByteSize, String attrArray) {
        try {
            preparedStatement.setString(1, file.toAbsolutePath().toString());
            preparedStatement.setFloat(2, mByteSize);
            preparedStatement.setString(3, attrArray);
            messageToUser.info(preparedStatement.executeUpdate() + " executeUpdate");
        }
        catch (SQLException ignore) {
            //
        }
    }
    
    private boolean more2MBOld(@NotNull BasicFileAttributes attrs) {
        return attrs.lastAccessTime().toMillis() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(ConstantsFor.ONE_YEAR * 2) && attrs.size() > ConstantsFor.MBYTE * 25;
    }
}

