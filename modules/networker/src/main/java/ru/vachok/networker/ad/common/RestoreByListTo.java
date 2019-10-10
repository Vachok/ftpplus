package ru.vachok.networker.ad.common;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.text.MessageFormat;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 @see RestoreByListToTest
 @since 12.09.2019 (10:40) */
public class RestoreByListTo implements Callable<String> {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, RestoreByListTo.class.getSimpleName());
    
    private Path pathToCopyRestored;
    
    @Contract(pure = true)
    public RestoreByListTo(Path pathToCopyRestored) {
        this.pathToCopyRestored = pathToCopyRestored;
    }
    
    @Override
    public String call() {
        return restoreList();
    }
    
    private @NotNull String restoreList() {
        StringBuilder stringBuilder = new StringBuilder();
        Deque<String> filesForRestore = getFilesList();
        if (filesForRestore.size() == 0) {
            throw new IllegalStateException("No file restore.deq , or table velkom.restore in srv-inetstat is empty!");
        }
        while (!filesForRestore.isEmpty()) {
            String filePath = filesForRestore.removeFirst();
            String copiedFile = cpFiles(filePath);
            stringBuilder.append(copiedFile);
        }
        return stringBuilder.toString();
    }
    
    private @NotNull Deque<String> getFilesList() {
        DataConnectTo dataConnectTo = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);
    
        Deque<String> filesForRestore = new ConcurrentLinkedDeque<>();
        final String sql = "select * from common.restore";
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.DB_COMMONRESTORE)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String upstring = resultSet.getString(ConstantsFor.DBCOL_UPSTRING);
                        if (!upstring.isEmpty()) {
                            filesForRestore.add(upstring);
                        }
                    
                    }
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 39");
        }
        if (filesForRestore.size() > 0) {
            return filesForRestore;
        }
        else {
            Queue<String> filesFromFile = FileSystemWorker.readFileEncodedToQueue(Paths.get("restore.deq"), "utf-8");
            filesForRestore.addAll(filesFromFile);
            return filesForRestore;
        }
    }
    
    private @NotNull String cpFiles(String first) {
        Path fileForCopyPath;
        boolean isCopyFile;
        try {
            fileForCopyPath = Paths.get(first);
        }
        catch (RuntimeException e) {
            messageToUser.error("RestoreByListTo", "cpFiles", e.getMessage() + " see line: 92");
            return MessageFormat.format("{0} {1} is {2}.", e.getMessage(), first, delRecordFromDatabase(first));
        }
        String parent = fileForCopyPath.getParent().getFileName().toString();
    
        parent = pathToCopyRestored + ConstantsFor.FILESYSTEM_SEPARATOR + parent + ConstantsFor.FILESYSTEM_SEPARATOR + fileForCopyPath.getFileName().toString();
    
        File fileForCopyAsFile = new File("null");
        try {
            fileForCopyAsFile = fileForCopyPath.toFile();
        }
        catch (RuntimeException e) {
            messageToUser.error("RestoreByListTo", "cpFiles", e.getMessage() + " see line: 103");
        }
    
        isCopyFile = FileSystemWorker.copyOrDelFile(fileForCopyAsFile, Paths.get(parent), false);
    
        return "File " + fileForCopyAsFile + " is copied to: " + parent + ". " + isCopyFile;
    }
    
    private boolean delRecordFromDatabase(String parent) {
        DataConnectTo dataConnectTo = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);
        final String sql = "delete from common.restore WHERE upstring LIKE ?" ;
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.DB_COMMONRESTORE)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, parent);
                return preparedStatement.executeUpdate() > 0;
            }
        }
        catch (SQLException | RuntimeException e) {
            messageToUser.error("RestoreByListTo", "delRecordFromDatabase", e.getMessage() + " see line: 112");
            return false;
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RestoreByListTo{");
        sb.append("pathToCopyRestored=").append(pathToCopyRestored);
        sb.append('}');
        return sb.toString();
    }
}
