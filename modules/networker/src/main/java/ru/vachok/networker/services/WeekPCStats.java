package ru.vachok.networker.services;


import org.slf4j.Logger;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 Сбор статы по-недельно
 <p>

 @since 08.12.2018 (0:12) */
public class WeekPCStats implements Runnable {

    /*Fields*/
    private static final Logger LOGGER = AppComponents.getLogger();

    @Override
    public void run() {
        final long stArt = System.currentTimeMillis();

        getFromDB();

        String msgTimeSp = "WeekPCStats.run method. " + ( float ) (System.currentTimeMillis() - stArt) / 1000 + " sec spend";
        LOGGER.info(msgTimeSp);
    }

    private void getFromDB() {
        final long stArt = System.currentTimeMillis();
        String sql = "select * from pcuserauto";
        File file = new File("velkom_pcuserauto.txt");
        try(Connection c = new RegRuMysql().getDefaultConnection(ConstantsFor.DB_PREFIX + "velkom");
            PreparedStatement p = c.prepareStatement(sql);
            ResultSet r = p.executeQuery();
            OutputStream outputStream = new FileOutputStream(file);
            PrintWriter printWriter = new PrintWriter(outputStream, true)){
            while(r.next()){
                printWriter.println(new StringBuilder()
                    .append(r.getString(1))
                    .append(" at ")
                    .append(r.getString(6))
                    .append(") ")
                    .append(r.getString(2))
                    .append(" ")
                    .append(r.getString(3)).toString());
            }
            String msgTimeSp = new StringBuilder()
                .append("WeekPCStats.getFromDB method. ")
                .append(( float ) (System.currentTimeMillis() - stArt) / 1000)
                .append(" sec spend\n")
                .append(file.getAbsolutePath())
                .append(" ")
                .append(( float ) file.length() / ConstantsFor.KBYTE).toString();
            LOGGER.warn(msgTimeSp);
        }
        catch(SQLException | IOException e){
            LOGGER.warn(e.getMessage());
        }
        copyFile(file);
    }

    private void copyFile(File file) {
        final long stArt = System.currentTimeMillis();
        if(ConstantsFor.thisPC().toLowerCase().contains("home")){
            try{
                Files.copy(file.toPath(), Paths.get("\\\\10.10.111.1\\Torrents-FTP\\" + file.getName()));
                String msgTimeSp = "WeekPCStats.copyFile method. " + ( float ) (System.currentTimeMillis() - stArt) / 1000 + " sec spend";
                LOGGER.info(msgTimeSp);
            }
            catch(IOException e){
                LOGGER.error(e.getMessage(), e);
            }
        }
        getInfo(file);
    }

    private void getInfo(File file) {
        final long stArt = System.currentTimeMillis();
        List<String> fileLines = new ArrayList<>();
        try(InputStream inputStream = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader)){
            while(reader.ready()){
                fileLines.add(bufferedReader.readLine());
            }
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
        String msgTimeSp = "WeekPCStats.getInfo method. " +
            ( float ) (System.currentTimeMillis() - stArt) / 1000 +
            " sec spend\n" + "\n" + new TForms().fromArray(fileLines, false) + "\n" + fileLines.size() + " size";
        LOGGER.info(msgTimeSp);
    }
}