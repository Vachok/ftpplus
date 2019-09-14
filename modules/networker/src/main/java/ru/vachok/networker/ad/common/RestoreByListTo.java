package ru.vachok.networker.ad.common;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
            stringBuilder.append(cpFiles(filesForRestore.removeFirst()));
        }
        return stringBuilder.toString();
    }
    
    private @NotNull Deque<String> getFilesList() {
        DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
        Deque<String> filesForRestore = new ConcurrentLinkedDeque<>();
        final String sql = "select * from velkom.restore";
        try (Connection connection = dataConnectTo.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String upstring = resultSet.getString(ConstantsFor.DBCOL_UPSTRING);
                if (!upstring.isEmpty()) {
                    filesForRestore.add(upstring);
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
        Path fileForCopy;
        try {
            fileForCopy = Paths.get(first);
        }
        catch (InvalidPathException e) {
            return e.getMessage() + " " + e.getInput();
        }
        String parent = fileForCopy.getParent().getFileName().toString();
        parent = pathToCopyRestored + ConstantsFor.FILESYSTEM_SEPARATOR + parent + ConstantsFor.FILESYSTEM_SEPARATOR + fileForCopy.getFileName().toString();
        boolean isCopyFile = FileSystemWorker.copyOrDelFile(fileForCopy.toFile(), Paths.get(parent), false);
        return "File " + fileForCopy + " is copied to: " + parent + ". " + isCopyFile;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RestoreByListTo{");
        sb.append("pathToCopyRestored=").append(pathToCopyRestored);
        sb.append('}');
        return sb.toString();
    }
}
