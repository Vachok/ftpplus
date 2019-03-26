package ru.vachok.networker;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.services.TimeChecker;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


/**
 Сохранение {@link Properties} в базу
 <p>

 @see AppComponents#saveAppProps(Properties)
 @since 25.02.2019 (10:12) */
class DBPropsCallable implements Callable<Boolean> {


    private static final MessageToUser messageToUser = new MessageLocal(DBPropsCallable.class.getSimpleName());

    @SuppressWarnings("DuplicateStringLiteralInspection")
    private static final String pathToPropsName = "ConstantsFor.properties";

    private static final Properties PROPS = new Properties();

    private static File pFile = new File(pathToPropsName);

    private final MysqlDataSource mysqlDataSource;

    private final String classMeth;

    private final String methName;
    /**
     {@link Properties} для сохданения.
     */
    private Properties propsToSave;

    /**
     * Запишем .mini
     */
    private final Collection<String> miniLogger = new PriorityQueue<>();

    private boolean isForced = false;

    private AtomicBoolean retBool = new AtomicBoolean(false);


    DBPropsCallable(MysqlDataSource mysqlDataSource, Properties propsToSave, String classMeth, String methName) {
        this.mysqlDataSource = mysqlDataSource;
        this.classMeth = classMeth;
        this.methName = methName;
        this.propsToSave = propsToSave;
    }

    DBPropsCallable(MysqlDataSource mysqlDataSource, Properties propsToSave, String classMeth, String methName, boolean isForced) {
        this.mysqlDataSource = mysqlDataSource;
        this.classMeth = classMeth;
        this.methName = methName;
        this.propsToSave = propsToSave;
        this.isForced = isForced;
    }

    /**
     * Тащит {@link #PROPS} из БД или файла
     * <p>
     * {@link ThreadConfig#thrNameSet(String)} <br>
     */
    static Properties takePr() {
        AppComponents
            .threadConfig()
            .thrNameSet("Props");

        InitProperties initProperties = new DBRegProperties(ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName());

        Properties retPr = new Properties(); File prFile = new File(ConstantsFor.class.getSimpleName() + ConstantsFor.FILEEXT_PROPERTIES); StringBuilder stringBuilder = new StringBuilder();
        String classMeth = "ConstantsFor.takePr";

        try (InputStream inputStream = new FileInputStream(prFile)) {
            retPr.load(inputStream);

            stringBuilder
                .append("fromFile: ")
                .append(true)
                .append("\n"); stringBuilder
                .append("File \"ff\": ")
                .append(new File("ff").exists())
                .append("\n");

        } catch (IOException e) {
            stringBuilder
                .append(e.getMessage())
                .append("\n")
                .append(new TForms().fromArray(e, false))
                .append("\n"); FileSystemWorker.error(classMeth, e);
        } PROPS.clear(); PROPS.putAll(retPr); stringBuilder
            .append(PROPS.size())
            .append(" is PROPS size, PROPS equals retPr: ")
            .append(PROPS.equals(retPr));

        messageToUser.warn(classMeth, "results", " = " + stringBuilder);

        return retPr;
    }

    @Override
    public Boolean call() {
        miniLogger.add("1. Starting " + getClass().getSimpleName() + " at: " + new Date(System.currentTimeMillis()));
        if (!pFile.exists()) {
            try {
                Path fileCreate = Files.createFile(pFile.toPath());
                miniLogger.add(fileCreate.toString());
            } catch (IOException e) {
                FileSystemWorker.error("DBPropsCallable.savePropsDelStatement", e);
            }
        }
        return upProps();
    }

    private boolean upProps() {
        String sql = "insert into ru_vachok_networker (property, valueofproperty, javaid) values (?,?,?)";
        miniLogger.add("2. " + sql);
        try (Connection c = mysqlDataSource.getConnection()) {
            try (PreparedStatement preparedStatement = c.prepareStatement(sql)) {
                AtomicInteger executeUpdate = new AtomicInteger();
                for (Map.Entry<Object, Object> entry : propsToSave.entrySet()) {
                    Object x = entry.getKey();
                    Object y = entry.getValue();
                    try {
                        preparedStatement.setString(1, x.toString());
                        preparedStatement.setString(2, y.toString());
                        preparedStatement.setString(3, ConstantsFor.class.getSimpleName());
                        executeUpdate.set(preparedStatement.executeUpdate());
                    } catch (SQLException e) {
                        FileSystemWorker.error(getClass().getSimpleName() + ".upProps", e);
                    }
                }
                retBool.set(executeUpdate.get() > 0);
            }
        } catch (SQLException e) {
            messageToUser.errorAlert(ConstantsFor.class.getSimpleName(), methName, e.getMessage());
            FileSystemWorker.error(classMeth, e);
            retBool.set(false);
        }
        FileSystemWorker.writeFile(getClass().getSimpleName() + ".mini", miniLogger.stream());
        return retBool.get();
    }

    /**
     Выполнение удаления {@link Properties} из БД
     <p>

     @param c {@link Connection}
     */
    private void savePropsDelStatement(Connection c) {
        String classPoint = "DBPropsCallable.";
        String methNameSave = "savePropsDelStatement";

        long delayX3 = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY * 3);
        boolean lastModAndCanWrite = pFile.lastModified() <= System.currentTimeMillis() - delayX3 && pFile.canWrite();
        if (lastModAndCanWrite) {
            writeToFile();
            retBool.set(false);
        } else if (!isForced) {
            delFromDataBase(c);
            retBool.set(true);
        } else retBool.set(false);
    }

    private void delFromDataBase(Connection c) {
        String sql = "delete FROM `ru_vachok_networker` where `javaid` =  'ConstantsFor'";
        miniLogger.add("4. Starting DELETE: " + sql);
        try (PreparedStatement preparedStatement = c.prepareStatement(sql);
             InputStream inputStream = new FileInputStream(pFile)
        ) {
            propsToSave.load(inputStream);
            int update = preparedStatement.executeUpdate();
            miniLogger.add("ConstantsFor.savePropsDelStatement " + "update " + " = " + update);
            if (update > 0) {
                retBool.set(true);
            }
        } catch (IOException | SQLException e) {
            retBool.set(false);
            miniLogger.add(e.getMessage());
        }
    }

    private void writeToFile() {
        miniLogger.add("Now: " + LocalDateTime.now() + " file: " + LocalDateTime.ofEpochSecond(pFile.lastModified() / 1000, 0, ZoneOffset.ofHours(3))); miniLogger.add("lastModAndCanWrite");
        String pointProps = ConstantsFor.FILEEXT_PROPERTIES; File toSavePrLoc = new File(ConstantsFor.class.getSimpleName() + pointProps);

        try (OutputStream outputStream = new FileOutputStream(toSavePrLoc);) {
            propsToSave.store(outputStream, classMeth); miniLogger.add("File: " + toSavePrLoc.getName() + " modified: " + new Date(toSavePrLoc.lastModified()));
        } catch (IOException e) {
            FileSystemWorker.error(getClass().getSimpleName() + ".writeToFile", e);
        } retBool.set(false); messageToUser.warn("NO DB SAVE! " + pFile.getName() + " can write is " + true + ". Modified: " + (new TimeChecker().call().getReturnTime() - pFile.lastModified()) + " MSec ago.");
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("DBPropsCallable{"); sb
            .append("propsToSave=")
            .append(propsToSave); sb.append('}'); return sb.toString();
    }
}
