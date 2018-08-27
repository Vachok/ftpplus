package ru.vachok.networker.logic;




import org.slf4j.Logger;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ApplicationConfiguration;
import ru.vachok.networker.DBMessanger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Arrays;


public class Putty extends Thread {

    private static final String SOURCE_CLASS = Putty.class.getSimpleName();

    private static final Logger LOGGER = ApplicationConfiguration.logger();
    private static final int TIMEOUT_2 = 2000;

    private static MessageToUser messageToUser = new DBMessanger();


    @SuppressWarnings({"SingleStatementInBlock" , "InjectedReferences"})
    private static void puttyStart() {
        File thisDir = new File(".");
        Path dirPath = Paths.get(thisDir.getAbsolutePath());
        File puttyExe = new File(String.valueOf(dirPath) + "\\putty.exe");
        String puttyPath = puttyExe.getAbsolutePath();
        if (puttyExe.canExecute()) {
            try {
                Process myPuttyExec = Runtime.getRuntime().exec(puttyPath);
            } catch (IOException e) {
                messageToUser.errorAlert(SOURCE_CLASS , "puttyStart ID 56" , e.getMessage());
            }
        } else {
            noPutty(puttyPath);
        }
    }


    private static void noPutty( String puttyPath ) {
        messageToUser.infoTimer(TIMEOUT_2 , SOURCE_CLASS + " noPutty ID 107 " + puttyPath);
        tryToRePack(puttyPath);
    }


    private static void tryToRePack( String puttyPath ) {
        Blob puttyB;
        File putty = new File(puttyPath);
        InputStream fileInputStream;
        String sql = "select bins from u0466446_liferpg.properties where javaid like 'putty';";
        try (Connection connection = new RegRuMysql().getDefaultConnection("u0466446_liferpg"); PreparedStatement preparedStatement = connection.prepareStatement(sql); ResultSet resultSet = preparedStatement.executeQuery()) {
            if (!resultSet.wasNull()) {
                while (resultSet.next()) {
                    puttyB = resultSet.getBlob("bins");
                    fileInputStream = puttyB.getBinaryStream();
                    FileOutputStream fileOutputStream = new FileOutputStream(putty);
                    while (fileInputStream.available() > 0) {
                        fileOutputStream.write(fileInputStream.read());
                    }
                }
            } else {
                String s = resultSet.getMetaData().toString();
                messageToUser.info(SOURCE_CLASS , "tryToRePack ID 97" , s);
            }
        } catch (SQLException | IOException e) {
            messageToUser.out("Putty_101" , (Arrays.toString(e.getStackTrace()).replaceAll(", " , "\n") + "\nPutty.tryToRePack, and ID (lineNum) is 101").getBytes());
        }
        new Putty().startPy();
    }


    /*Private methods
    =========================* */
    private void startPy() {
        puttyStart();
    }


    @Override
    public void run() {
        puttyStart();
    }


    @Override
    public State getState() {
        return super.getState();
    }
}