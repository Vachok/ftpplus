package ru.vachok.networker;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.services.TimeChecker;

import java.io.*;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


/**
 Сохранение {@link Properties} в базу
 <p>

 @see AppComponents#saveAppProps(Properties)
 @since 25.02.2019 (10:12) */
class SaveDBPropsCallable implements Callable<Boolean> {


    private static final MessageToUser messageToUser = new MessageLocal(SaveDBPropsCallable.class.getSimpleName());

    @SuppressWarnings("DuplicateStringLiteralInspection")
    private static final String pathToPropsName = "ConstantsFor.properties";

    private static File pFile = new File(pathToPropsName);

    private final MysqlDataSource mysqlDataSource;

    private final String classMeth;

    private final String methName;
    /**
     {@link Properties} для сохданения.
     */
    private Properties propsToSave;

    private AtomicBoolean retBool = new AtomicBoolean(false);


    SaveDBPropsCallable(MysqlDataSource mysqlDataSource, Properties propsToSave, String classMeth, String methName) {
        this.mysqlDataSource = mysqlDataSource;
        this.classMeth = classMeth;
        this.methName = methName;
        this.propsToSave = propsToSave;
    }


    @Override
    public Boolean call() {
        final String sql = "insert into ru_vachok_networker (property, valueofproperty, javaid) values (?,?,?)";

        pFile.setLastModified(ConstantsFor.DELAY);

        try (Connection c = mysqlDataSource.getConnection(); OutputStream outputStream = new FileOutputStream(mysqlDataSource.getResourceId() + "_dssrc.properties")) {
            mysqlDataSource.setRelaxAutoCommit(true);
            Savepoint delPropsPoint = c.setSavepoint();
            retBool.set(savePropsDelStatement(c));
            try (PreparedStatement preparedStatement = c.prepareStatement(sql)) {
                AtomicInteger executeUpdate = new AtomicInteger();
                propsToSave.forEach((x, y)->{
                    try {
                        preparedStatement.setString(1, x.toString());
                        preparedStatement.setString(2, y.toString());
                        preparedStatement.setString(3, ConstantsFor.class.getSimpleName());
                        executeUpdate.set(preparedStatement.executeUpdate());
                    }
                    catch (SQLException e) {
                        rollBackState(c, delPropsPoint);
                    }
                });
                retBool.set(executeUpdate.get() > 0);
            }
        }
        catch (SQLException | IOException e) {
            messageToUser.errorAlert(ConstantsFor.class.getSimpleName(), methName, e.getMessage());
            FileSystemWorker.error(classMeth, e);
            retBool.set(false);
        }
        return retBool.get();
    }


    private void rollBackState(Connection c, Savepoint delPropsPoint) {
        retBool.set(false);
        try {
            c.rollback(delPropsPoint);
        }
        catch (SQLException ignore) {
            //
        }
    }


    /**
     Выполнение удаления {@link Properties} из БД
     <p>

     @param c {@link Connection}
     @return выполнение {@link PreparedStatement}
     */
    private boolean savePropsDelStatement(Connection c) {
        String classPoint = "SaveDBPropsCallable.";
        String methNameSave = "savePropsDelStatement";
        String sql = "delete FROM `ru_vachok_networker` where `javaid` =  'ConstantsFor'";

        if (!pFile.exists()) {
            try {
                Files.createFile(pFile.toPath());
            }
            catch (IOException e) {
                FileSystemWorker.error("SaveDBPropsCallable.savePropsDelStatement", e);
            }
            pFile.setLastModified(ConstantsFor.DELAY);
        }
        long delayX3 = new TimeChecker().call().getReturnTime() - TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY * 3);
        boolean lastModAndCanWrite = pFile.lastModified() <= System.currentTimeMillis() - delayX3 && pFile.canWrite();

        if (lastModAndCanWrite) {
            @SuppressWarnings("DuplicateStringLiteralInspection") String pointProps = ".properties";
            try (OutputStream outputStream = new FileOutputStream(ConstantsFor.class.getSimpleName() + pointProps)) {
                propsToSave.store(outputStream, classMeth);
            } catch (IOException e) {
                messageToUser.error(e.getMessage());
            }

            messageToUser.warn("NO DB SAVE! " + pFile.getName() + " can write is " + true + ". Modified: " + (new TimeChecker().call().getReturnTime() - pFile.lastModified()) + " MSec ago.");
            retBool.set(false);
        }
        else {
            Savepoint delPropsPoint = null;
            try (PreparedStatement preparedStatement = c.prepareStatement(sql); InputStream inputStream = new FileInputStream(pFile)) {
                delPropsPoint = c.setSavepoint();
                propsToSave.load(inputStream);
                int update = preparedStatement.executeUpdate();
                messageToUser.info("ConstantsFor.savePropsDelStatement", "update", " = " + update);
                messageToUser.info(classPoint + methNameSave, "Modified: ", TimeUnit.MILLISECONDS.toMinutes(new TimeChecker().call().getReturnTime() - pFile.lastModified()) + " min ago");
                if (update > 0) {
                    retBool.set(true);
                }
            }
            catch (SQLException | IOException e) {
                rollBackState(c, delPropsPoint);
            }
        }

        return retBool.get();
    }
}
