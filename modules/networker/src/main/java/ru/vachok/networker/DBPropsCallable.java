package ru.vachok.networker;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 Сохранение {@link Properties} в базу
 <p>

 @see AppComponents#saveAppPropsForce()
 @since 25.02.2019 (10:12) */
@SuppressWarnings("DuplicateStringLiteralInspection") public class DBPropsCallable implements Callable<Properties> {


    private static final MessageToUser messageToUser = new MessageLocal(DBPropsCallable.class.getSimpleName());

    @SuppressWarnings("DuplicateStringLiteralInspection")
    private static final String pathToPropsName = "ConstantsFor.properties";

    private static File pFile = new File(pathToPropsName);

    private MysqlDataSource mysqlDataSource = null;

    /**
     {@link Properties} для сохданения.
     */
    private Properties propsToSave = new Properties();

    /**
     * Запишем .mini
     */
    private final Collection<String> miniLogger = new PriorityQueue<>();

    private boolean isForced = false;

    private AtomicBoolean retBool = new AtomicBoolean(false);

    DBPropsCallable(MysqlDataSource mysqlDataSource, Properties propsToSave) {
        this.mysqlDataSource = mysqlDataSource;
        this.propsToSave = propsToSave;
    }

    DBPropsCallable(MysqlDataSource mysqlDataSource, Properties propsToSave, boolean isForced) {
        this.mysqlDataSource = mysqlDataSource;
        this.propsToSave = propsToSave;
        this.isForced = isForced;
    }


    private DBPropsCallable() {

    }


    @Override public Properties call() {
        if (this.propsToSave.size() > 3) { return propsToSave; } else if (isForced && upProps()) { return propsToSave; } else {
            throw new IllegalComponentStateException(propsToSave.toString());
        }
    }


    @SuppressWarnings("DuplicateStringLiteralInspection") public boolean upProps() {
        String methName = ".upProps";
        String sql = "insert into ru_vachok_networker (property, valueofproperty, javaid) values (?,?,?)";
        miniLogger.add("2. " + sql);
        FileSystemWorker.writeFile(getClass().getSimpleName() + ".mini" , miniLogger.stream());
        try (Connection c = mysqlDataSource.getConnection();
             OutputStream outputStream = new FileOutputStream(ConstantsFor.class.getSimpleName() + ConstantsFor.FILEEXT_PROPERTIES)
        )
        {
            Objects.requireNonNull(propsToSave).store(outputStream , getClass().getSimpleName() + " " + LocalTime.now());
            int executeUpdateInt = 0;
            try (PreparedStatement preparedStatement = c.prepareStatement(sql)) {
                for (Map.Entry<Object, Object> entry : propsToSave.entrySet()) {
                    Object x = entry.getKey();
                    Object y = entry.getValue();
                    preparedStatement.setString(1 , x.toString());
                    preparedStatement.setString(2 , y.toString());
                    preparedStatement.setString(3 , ConstantsFor.class.getSimpleName());
                    executeUpdateInt = executeUpdateInt + preparedStatement.executeUpdate();
                }
                return executeUpdateInt > 0;
            }
        } catch (IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".upProps" , e));
        } catch (SQLException e) {
            messageToUser.error(e.getMessage());
        }
        return false;
    }

    /**
     Выполнение удаления {@link Properties} из БД
     <p>

     @param c {@link Connection}
     */
    private void savePropsDelStatement( Connection c) {
        String classPoint = "DBPropsCallable.";
        String methNameSave = "savePropsDelStatement";
        long delayX3 = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY * 3);
        if (!isForced) {
            if (delFromDataBase(c)) retBool.set(upProps());
        } else {
            this.propsToSave = AppComponents.getOrSetProps();
        }
    }


    private boolean delFromDataBase( Connection c ) {
        String sql = "delete FROM `ru_vachok_networker` where `javaid` =  'ConstantsFor'";
        miniLogger.add("4. Starting DELETE: " + sql);
        try (PreparedStatement preparedStatement = c.prepareStatement(sql);
             InputStream inputStream = new FileInputStream(pFile)
        ) {
            int pDeleted = preparedStatement.executeUpdate();
            this.propsToSave.load(inputStream);
            miniLogger.add("ConstantsFor.savePropsDelStatement " + "pDeleted " + " = " + pDeleted);
            miniLogger.add(new TForms().fromArray(propsToSave , false));
            Files.deleteIfExists(pFile.toPath());
            retBool.set(upProps());
        } catch (IOException | SQLException e) {
            retBool.set(false);
            miniLogger.add(e.getMessage());
            FileSystemWorker.writeFile(getClass().getSimpleName() + ".mini" , miniLogger.stream());
        }
        return retBool.get();
    }
}
