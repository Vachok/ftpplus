// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.common;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.componentsrepo.services.MyCalen;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.synchronizer.SyncData;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;


/**
 @see CleanerTest
 @since 25.06.2019 (11:37) */
public class Cleaner extends SimpleFileVisitor<Path> implements Callable<String> {
    
    
    private static final MessageToUser messageToUser = ru.vachok.networker.restapi.message.MessageToUser
            .getInstance(ru.vachok.networker.restapi.message.MessageToUser.LOCAL_CONSOLE, Cleaner.class.getSimpleName());
    
    private Map<Integer, Path> indexPath = new ConcurrentHashMap<>();
    
    private long lastModifiedLog = MyCalen.getLongFromDate(3, 12, 2019, 15, 15);
    
    private List<String> remainFiles = new ArrayList<>();
    
    private int deleteTodayLimit = 0;
    
    private int getTotalFiles() {
        return SyncData.getLastRecId(DataConnectTo.getDefaultI(), ConstantsFor.DB_COMMONOLDFILES);
    }
    
    /**
     @return имя файла-лога, с информацией об удалениях.
     */
    @Override
    public String call() {
        return makeDeletions();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Cleaner{");
        sb.append("remainFiles=").append(remainFiles);
        sb.append(", lastModifiedLog=").append(lastModifiedLog);
        sb.append(", indexPath=").append(indexPath);
        sb.append(", deleteTodayLimit=").append(deleteTodayLimit);
        sb.append('}');
        return sb.toString();
    }
    
    private void fillPaths() {
        try (Connection connection = DataConnectTo.getDefaultI().getDefaultConnection(ConstantsFor.DB_COMMONOLDFILES);
             PreparedStatement preparedStatement = connection.prepareStatement("select * from common.oldfiles");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                indexPath.put(resultSet.getInt(1), Paths.get(resultSet.getString(2)));
            }
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 70");
        }
        finally {
            this.deleteTodayLimit = limitOfDeleteFiles(indexPath.size());
            messageToUser.warn(this.getClass().getSimpleName(), "Today LIMIT is ", String.valueOf(deleteTodayLimit));
        }
        
    }
    
    @NotNull
    private String makeDeletions() {
        fillPaths();
        for (int i = 0; i < limitOfDeleteFiles(indexPath.size()); i++) {
            Random random = new Random();
            int index = random.nextInt(indexPath.size());
            Path sourceDel = indexPath.get(index);
            Path copyPath = Paths.get("null");
            try {
                
                copyPath = Files.copy(sourceDel, Paths.get(sourceDel.normalize().toAbsolutePath().toString()
                        .replace("\\\\srv-fs.eatmeat.ru\\common_new\\", "\\\\192.168.14.10\\IT-Backup\\Srv-Fs\\Archives\\")), StandardCopyOption.REPLACE_EXISTING);
                if (copyPath.toFile().exists()) {
                    Files.deleteIfExists(sourceDel);
                }
            }
            catch (IOException e) {
                messageToUser.warn(Cleaner.class.getSimpleName(), "makeDeletions", e.getMessage() + Thread.currentThread().getState().name());
            }
            finally {
                messageToUser.warning(this.getClass().getSimpleName(), "makeDeletions", MessageFormat
                        .format("{0} {1}:{2} remain {3}", sourceDel.normalize().toAbsolutePath().toString(), String.valueOf(sourceDel.toFile().exists()), String
                                .valueOf(copyPath.toFile().exists()), limitOfDeleteFiles(indexPath.size()) - i));
            }
        }
        return "";
    }
    
    private int limitOfDeleteFiles(int stringsInLogFile) {
        
        if (System.currentTimeMillis() < lastModifiedLog + TimeUnit.SECONDS.toMillis(1)) {
            stringsInLogFile = (stringsInLogFile / 100) * 10;
        }
        else if (System.currentTimeMillis() < lastModifiedLog + TimeUnit.DAYS.toMillis(2)) {
            stringsInLogFile = (stringsInLogFile / 100) * 25;
        }
        else if (System.currentTimeMillis() < lastModifiedLog + TimeUnit.DAYS.toMillis(3)) {
            stringsInLogFile = (stringsInLogFile / 100) * 75;
        }
        this.deleteTodayLimit = stringsInLogFile;
        return stringsInLogFile;
    }
}
