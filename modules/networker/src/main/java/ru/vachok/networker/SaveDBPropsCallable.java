package ru.vachok.networker;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 Сохранение {@link Properties} в базу
 <p>

 @see ConstantsFor#saveAppProps(Properties)
 @since 25.02.2019 (10:12) */
class SaveDBPropsCallable implements Callable<Boolean> {

    private static MessageToUser messageToUser = new MessageLocal();

    private final MysqlDataSource mysqlDataSource;

    private final Properties propsToSave;

    private final String classMeth;

    private final String methName;

    SaveDBPropsCallable(MysqlDataSource mysqlDataSource, Properties propsToSave, String classMeth, String methName) {
        this.mysqlDataSource = mysqlDataSource;
        this.propsToSave = propsToSave;
        this.classMeth = classMeth;
        this.methName = methName;
    }

    /**
     Выполнение удаления {@link Properties} из БД
     <p>

     @param c             {@link Connection}
     @param delPropsPoint {@link Savepoint}
     @throws SQLException делает {@link Connection#rollback(Savepoint)}
     @see #saveAppProps(Properties)
     */
    private static boolean savePropsDelStatement(Connection c, Savepoint delPropsPoint) throws SQLException {
        final String sql = "delete FROM `ru_vachok_networker` where `javaid` =  'ConstantsFor'";
        try (PreparedStatement preparedStatement = c.prepareStatement(sql)) {
            int update = preparedStatement.executeUpdate();
            messageToUser.info("ConstantsFor.savePropsDelStatement", "update", " = " + update);
            return true;
        } catch (SQLException e) {
            messageToUser.errorAlert("ConstantsFor", "savePropsDelStatement", e.getMessage());
            c.rollback(delPropsPoint);
            return false;
        }
    }

    @Override
    public Boolean call() throws Exception {
        String sql = "insert into ru_vachok_networker (property, valueofproperty, javaid) values (?,?,?)";
        try (Connection c = mysqlDataSource.getConnection();
             OutputStream outputStream = new FileOutputStream(ConstantsFor.class.getSimpleName() + ".properties")) {
            Savepoint delPropsPoint = c.setSavepoint("delPropsPoint" + LocalTime.now().format(DateTimeFormatter.ISO_TIME));
            boolean retBool = false;
            if (savePropsDelStatement(c, delPropsPoint)) {
                PreparedStatement preparedStatement = c.prepareStatement(sql);
                AtomicInteger executeUpdate = new AtomicInteger();
                propsToSave.forEach((x, y) -> {
                    try {
                        preparedStatement.setString(1, x.toString());
                        preparedStatement.setString(2, y.toString());
                        preparedStatement.setString(3, "ConstantsFor");
                        executeUpdate.set(preparedStatement.executeUpdate());
                    } catch (SQLException e) {
                        FileSystemWorker.error("ConstantsFor.saveAppProps", e);
                    }
                });
                if (executeUpdate.get() > 0) {
                    retBool = true;
                    preparedStatement.close();
                } else {
                    c.rollback();
                }
                propsToSave.store(outputStream, classMeth);
            }
            return retBool;
        } catch (SQLException e) {
            messageToUser.errorAlert(ConstantsFor.class.getSimpleName(), methName, e.getMessage());
            FileSystemWorker.error(classMeth, e);
            return false;
        }
    }
}
