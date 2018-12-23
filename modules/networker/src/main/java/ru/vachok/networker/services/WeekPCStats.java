package ru.vachok.networker.services;


import org.slf4j.Logger;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 Сбор статы по-недельно
 <p>

 @since 08.12.2018 (0:12) */
public class WeekPCStats implements Runnable {

    /*Fields*/
    private static final Logger LOGGER = AppComponents.getLogger();

    /**
     Лист только с именами ПК
     */
    private static final List<String> PC_NAMES_IN_TABLE = new ArrayList<>();

    /**
     Более 3х совпадений в строках
     */
    private static final String STR_SEC_SPEND = " sec spend";

    @Override
    public void run() {
        final long stArt = System.currentTimeMillis();
        getFromDB();
        String msgTimeSp = "WeekPCStats.run method. " + (float) (System.currentTimeMillis() - stArt) / 1000 + STR_SEC_SPEND;
        LOGGER.info(msgTimeSp);
    }

    /**
     Инфо из БД
     <p>
     Читает БД pcuserauto <br>
     Записывает файл {@code velkom_pcuserauto.txt} <br>
     Usages: {@link #run()}
     Uses: {@link FileSystemWorker#copyFile(File, String)}
     */
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
                PC_NAMES_IN_TABLE.add(r.getString(2) + " " + r.getString(3));
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
        String toCopy = "\\\\10.10.111.1\\Torrents-FTP\\" + file.getName();
        if(!ConstantsFor.thisPC().toLowerCase().contains("home")){
            toCopy = file.getName() + "_cp";
        }
        FileSystemWorker.copyFile(file, toCopy);
    }

    /**
     Копир
     <p>
     Если {@link ConstantsFor#thisPC()} - home, копирует файл в роутер. <br>
     Usages: {@link #getFromDB()} <br>
     Uses: {@link ConstantsFor#thisPC()}, {@link #getInfoList(File)} ()}

     @param file {@code velkom_pcuserauto.txt}
     */
    private void sendToEmail(File file) {
        MessageToUser eSender = new ESender("143500@gmail.com");
        eSender.info(this.getClass().getSimpleName(),
            ConstantsFor.thisPC() + " " + ConstantsFor.getUpTime(),
            new TForms().fromArray(getInfoList(file), false));
        getInfoList(file);
    }

    /**
     Чтение файла
     <p>
     Usages: {@link #sendToEmail(File)} <br>
     Uses: -
     <p>

     @param file {@code velkom_pcuserauto.txt}
     @return {@link List} строк из файла
     */
    private List<String> getInfoList(File file) {
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
            fileLines.add(e.getMessage());
            return fileLines;
        }
        String msgTimeSp = "WeekPCStats.getInfoList method. " +
            ( double ) (System.currentTimeMillis() - stArt) / 1000 +
            " sec spend\n" + fileLines.size() + " strings in file\n" +
            PC_NAMES_IN_TABLE.size() + " PC_NAMES_IN_TABLE.size()\n" +
            PC_NAMES_IN_TABLE.get(0) + " " + PC_NAMES_IN_TABLE.get(1) + "...\n";
        LOGGER.info(msgTimeSp);
        String infoS = getInfo();
        try(OutputStream outputStream = new FileOutputStream("infoS.txt");
            PrintWriter printWriter = new PrintWriter(outputStream, true)){
            printWriter.print(infoS);
        }
        catch(IOException e){
            LOGGER.warn(e.getMessage(), e);
        }
        MessageToUser messageToUser = new ESender("143500@gmail.com");
        new Thread(() -> messageToUser.info(ConstantsFor.getUpTime(), msgTimeSp, infoS)).start();
        return fileLines;
    }

    /**
     Usages: {@link #getInfoList(File)} <br> Uses: {@link TForms#fromArray(java.util.concurrent.ConcurrentMap, boolean)}

     @return информация для записи в файл
     */
    private String getInfo() {
        final long stArt = System.currentTimeMillis();
        Collections.sort(PC_NAMES_IN_TABLE);
        Object[] objects = PC_NAMES_IN_TABLE.stream().distinct().toArray();
        List<String> obList = new ArrayList<>();
        for(Object object : objects){
            obList.add(object.toString());
        }
        Collections.sort(obList);
        ConcurrentMap<String, Integer> integerStringConcurrentMap = new ConcurrentHashMap<>();
        PC_NAMES_IN_TABLE.parallelStream().forEach(x -> {
            int integer = 0;
            for(String o : obList){
                boolean contains = PC_NAMES_IN_TABLE.contains(o);
                if(contains){
                    integer = integer + 1;
                }
                integerStringConcurrentMap.put(o, integer);
            }
        });
        String msgTimeSp = "WeekPCStats.getInfo method. " + (float) (System.currentTimeMillis() - stArt) / 1000 + STR_SEC_SPEND;
        LOGGER.info(msgTimeSp);
        return new TForms().fromArray(integerStringConcurrentMap, false);
    }
}