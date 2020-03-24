// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.common;


import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 @see CleanerTest
 @since 25.06.2019 (11:37) */
@Service("Cleaner")
public class Cleaner extends SimpleFileVisitor<Path> implements Runnable {


    private static final MessageToUser messageToUser = ru.vachok.networker.restapi.message.MessageToUser
        .getInstance(ru.vachok.networker.restapi.message.MessageToUser.DB, Cleaner.class.getSimpleName());

    private Map<Integer, Path> indexPath = new ConcurrentHashMap<>();

    private long lastModifiedLog = 1;

    private List<String> remainFiles = new ArrayList<>();

    private int deleteTodayLimit = 0;

    public void setLastModifiedLog(long lastModifiedLog) {
        this.lastModifiedLog = lastModifiedLog;
    }

    public Cleaner() {
    }

    public Cleaner(long lastModifiedLog) {
        this.lastModifiedLog = lastModifiedLog;
    }

    /**
     @see CleanerTest
     */
    @Override
    public void run() {
        FirebaseApp firebaseApp = AppComponents.getFirebaseApp();
        messageToUser.info(getClass().getSimpleName(), firebaseApp.getName(), "INITIALIZED");
        makeDeletions();
    }

    private void makeDeletions() {
        fillPaths();
        for (int i = 0; i < indexPath.size(); i++) {
            Random random = new Random();
            int index = random.nextInt(indexPath.size());
            Path sourceDel = genSrcDel(index);
            Path copyPath = sourceDel;
            try {
                Path replacedPath = Paths.get(sourceDel.normalize().toAbsolutePath().toString().toLowerCase()
                    .replace("\\\\srv-fs.eatmeat.ru\\common_new\\", "\\\\192.168.14.10\\IT-Backup\\Srv-Fs\\Archives\\".toLowerCase()));
                if (!replacedPath.toFile().exists()) {
                    copyPath = Files.move(sourceDel, replacedPath, StandardCopyOption.REPLACE_EXISTING);
                }
                if (copyPath.toFile().exists()) {
                    messageToUser.info(getClass().getSimpleName(), sourceDel.toAbsolutePath().toString(), "Moved " + copyPath.toAbsolutePath()
                        .toString() + ", db removed: " + removeFromDB(index));
                }
                else {
                    messageToUser
                        .warning(getClass().getSimpleName(), "NOT MOVED!", copyPath.toAbsolutePath().toString() + " old exists: " + sourceDel.toFile().exists());
                }
            }
            catch (NoSuchFileException e) {
                removeFromDB(index);
            }
            catch (IOException | RuntimeException e) {
                messageToUser.warn(Cleaner.class.getSimpleName(), "makeDeletions", AbstractForms.fromArray(e));
            }
            finally {
                if (sourceDel != null) {
                    String mesg = MessageFormat.format("{0}) {1}", index, sourceDel.toString());
                    FirebaseDatabase.getInstance().getReference(getClass().getSimpleName())
                        .setValue(mesg, (error, ref)->messageToUser.warn(Cleaner.class.getSimpleName(), error.getMessage(), " see line: 104 ***"));
                }
            }
        }
    }

    private void fillPaths() {
        try (Connection connection = DataConnectTo.getDefaultI().getDefaultConnection(ConstantsFor.DB_COMMONOLDFILES);
             PreparedStatement preparedStatement = connection.prepareStatement("select * from common.oldfiles");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                if (resultSet.getInt("moved") == 0) {
                    indexPath.put(resultSet.getInt(1), Paths.get(resultSet.getString(2)));
                }
                else {
                    messageToUser.info(getClass().getSimpleName(), "already moved: ", resultSet.getString(2));
                }
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

    private Path genSrcDel(int i) {
        Path path = indexPath.get(indexPath.keySet().stream().iterator().next());
        if (indexPath.containsKey(i)) {
            path = indexPath.get(i);
        }
        try {
            indexPath.remove(i);
        }
        catch (RuntimeException e) {
            System.out.println(MessageFormat.format("i-1 = {0}", i));
        }
        return path;
    }

    private int removeFromDB(int idRec) {
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_COMMONOLDFILES);
             PreparedStatement preparedStatement = connection
                 .prepareStatement(String.format("UPDATE `common`.`oldfiles` SET `moved`='1' WHERE  `idrec`=%d", idRec))) {
            return preparedStatement.executeUpdate();

        }
        catch (SQLException e) {
            messageToUser.warn(Cleaner.class.getSimpleName(), "removeFromDB", e.getMessage() + Thread.currentThread().getState().name());
            return -666;
        }
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
}
