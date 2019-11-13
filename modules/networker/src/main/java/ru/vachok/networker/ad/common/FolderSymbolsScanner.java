package ru.vachok.networker.ad.common;


import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.*;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;


/**
 @see FolderSymbolsScannerTest
 @since 10.10.2019 (12:44) */
public class FolderSymbolsScanner extends SimpleFileVisitor<Path> implements Callable<String> {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, FolderSymbolsScanner.class.getSimpleName());
    
    private Map<String, Integer> manySymbolsPaths = new ConcurrentHashMap<>();
    
    private Path scanDir;
    
    public FolderSymbolsScanner(Path scanDir) {
        this.scanDir = scanDir;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FolderSymbolsScanner{");
        sb.append("scanDir=").append(scanDir);
        sb.append(", manySymbolsPaths=").append(manySymbolsPaths);
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public String call() throws Exception {
        Files.walkFileTree(scanDir, this);
        return AbstractForms.fromArray(manySymbolsPaths);
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String stringPath = file.toAbsolutePath().normalize().toString();
        if (stringPath.length() > 254) {
            manySymbolsPaths.put(stringPath, stringPath.length());
            addToDatabase(stringPath);
        }
        return FileVisitResult.CONTINUE;
    }
    
    private void addToDatabase(String problemPath) {
        DataConnectTo dataConnectTo = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);
        String sql = "insert into common.muchsymbols (tstamp, path) values (?, ?);" ;
        
        try (Connection connection = dataConnectTo.getDefaultConnection("common.muchsymbols")) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                preparedStatement.setString(2, problemPath);
                int update = preparedStatement.executeUpdate();
                messageToUser.info(this.getClass().getSimpleName(), dataConnectTo.toString(), update + " updated");
            }
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("FolderSymbolsScanner.addToDatabase", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace())));
        }
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
    
}
