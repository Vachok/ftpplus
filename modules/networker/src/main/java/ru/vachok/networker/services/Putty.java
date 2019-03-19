package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;


/**
 putty.exe . Если нет на ПК - берёт из БД
 */
public class Putty extends Thread {

    private static final String SOURCE_CLASS = Putty.class.getSimpleName();

    private static final Logger LOGGER = LoggerFactory.getLogger(Putty.class.getSimpleName());

    private static MessageToUser messageToUser = new MessageCons();

    @SuppressWarnings({"SingleStatementInBlock"})
    private static void puttyStart() {
        File thisDir = new File(".");
        Path dirPath = Paths.get(thisDir.getAbsolutePath());
        File puttyExe = new File(dirPath + "\\putty.exe");
        String puttyPath = puttyExe.getAbsolutePath();
        if (puttyExe.canExecute()) {
            try {
                Process myPuttyExec = Runtime.getRuntime().exec(puttyPath);
                String msg = myPuttyExec.isAlive() + " my putty alive";
                LOGGER.info(msg);
            } catch (IOException e) {
                messageToUser.errorAlert(SOURCE_CLASS, "puttyStart ID 54", e.getMessage());
            }
        } else {
            noPutty(puttyPath);
        }
    }

    private static void noPutty(String puttyPath) {
        tryToRePack(puttyPath);
    }

    private static void tryToRePack(String puttyPath) {
        File putty = new File(puttyPath);
        String sql = "select bins from u0466446_liferpg.properties where javaid like 'putty';";
        String classMeth = "Putty.tryToRePack";

        try (Connection connection = new AppComponents().connection(ConstantsFor.DBPREFIX + ConstantsFor.STR_VELKOM);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = preparedStatement.executeQuery();
                 FileOutputStream fileOutputStream = new FileOutputStream(putty)) {
                if (!resultSet.wasNull()) {
                    while (resultSet.next()) {
                        Blob puttyB = resultSet.getBlob("bins");
                        try (InputStream fileInputStream = puttyB.getBinaryStream()) {
                            while (fileInputStream.available() > 0) {
                                fileOutputStream.write(fileInputStream.read());
                            }
                        } catch (SQLException e) {
                            messageToUser.errorAlert("Putty", "tryToRePack", e.getMessage());
                            FileSystemWorker.error(classMeth, e);
                        }
                    }
                } else {
                    String rsMetaData = resultSet.getMetaData().toString();
                    messageToUser.info(classMeth, "rsMetaData", " = " + rsMetaData);
                }
            }
        } catch (SQLException | IOException e) {
            messageToUser.errorAlert("Putty", "tryToRePack", e.getMessage());
            FileSystemWorker.error(classMeth, e);
        }
        new Putty().startPy();
    }

    private void startPy() {
        puttyStart();
    }

    @Override
    public void run() {
        puttyStart();
    }

    @Override
    public Thread.State getState() {
        String msg = Thread.currentThread().getName() + " STATE";
        LOGGER.info(msg);
        return super.getState();
    }
}