package ru.vachok.networker;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


/**
 Сохранение {@link Properties} в базу
 <p>
 
 @see ConstantsFor#saveAppProps(Properties)
 @since 25.02.2019 (10:12) */
class SaveDBPropsCallable implements Callable<Boolean> {
    
    
    private static final MessageToUser messageToUser = new MessageLocal(SaveDBPropsCallable.class.getSimpleName());
    
    private final MysqlDataSource mysqlDataSource;
    
    private final String classMeth;
    
    private final String methName;
    
    private static File pFile = new File("ConstantsFor.properties");
    
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
    
    /**
     Выполнение удаления {@link Properties} из БД
     <p>
     
     @param c {@link Connection}
     @param delPropsPoint {@link Savepoint}
     @return выполнение {@link PreparedStatement}
     
     @throws SQLException делает {@link Connection#rollback(Savepoint)}
     */
    private boolean savePropsDelStatement(Connection c, Savepoint delPropsPoint) throws SQLException, IOException {
        String sql = "delete FROM `ru_vachok_networker` where `javaid` =  'ConstantsFor'";
        if (!pFile.exists()) {
            Files.createFile(pFile.toPath());
        }
        if (pFile.canWrite()) {
            try (OutputStream outputStream = new FileOutputStream(ConstantsFor.class.getSimpleName() + ".properties")) {
                propsToSave.store(outputStream, classMeth);
            } catch (IOException e) {
                messageToUser.errorAlert("SaveDBPropsCallable", "savePropsDelStatement", e.getMessage());
            }
            messageToUser.warn("NO DB SAVE! " + pFile.getName() + " can write is " + pFile.canWrite());
            retBool.set(false);
        } else if (!pFile.canWrite()) {
            try (PreparedStatement preparedStatement = c.prepareStatement(sql);
                 InputStream inputStream = new FileInputStream(pFile)) {
                propsToSave.load(inputStream);
                int update = preparedStatement.executeUpdate();
                messageToUser.info("ConstantsFor.savePropsDelStatement", "update", " = " + update);
                if (update > 0) retBool.set(true);
            } catch (SQLException | IOException e) {
                messageToUser.errorAlert("ConstantsFor", "savePropsDelStatement", e.getMessage());
                c.rollback(delPropsPoint);
                retBool.set(false);
            }
        } else {
            retBool.set(false);
        }
        return retBool.get();
    }
    
    @Override
    public Boolean call() {
        String sql = "insert into ru_vachok_networker (property, valueofproperty, javaid) values (?,?,?)";
        try (Connection c = mysqlDataSource.getConnection()) {
            Savepoint delPropsPoint = c.setSavepoint("delPropsPoint" + LocalTime.now().format(DateTimeFormatter.ISO_TIME));
            retBool = new AtomicBoolean(savePropsDelStatement(c, delPropsPoint));
            if (retBool.get()) {
                try (PreparedStatement preparedStatement = c.prepareStatement(sql)) {
                    AtomicInteger executeUpdate = new AtomicInteger();
                    propsToSave.forEach((x, y)->{
                        try {
                            preparedStatement.setString(1, x.toString());
                            preparedStatement.setString(2, y.toString());
                            preparedStatement.setString(3, "ConstantsFor");
                            executeUpdate.set(preparedStatement.executeUpdate());
                        } catch (SQLException e) {
                            messageToUser.warn(getClass().getSimpleName(), e.getSQLState() + " " + e.getMessage(), new TForms().fromArray(e, false));
                            retBool.set(false);
                        }
                    });
                    if (executeUpdate.get() > 0) {
                        retBool.set(true);
                        preparedStatement.close();
                    } else {
                        c.rollback();
                        preparedStatement.close();
                        retBool.set(false);
                    }
                }
            }
        } catch (SQLException | IOException e) {
            messageToUser.errorAlert(ConstantsFor.class.getSimpleName(), methName, e.getMessage());
            FileSystemWorker.error(classMeth, e);
            retBool.set(false);
        }
        return retBool.get();
    }
}
