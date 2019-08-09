// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.statistics;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.*;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;


/**
 @see ru.vachok.networker.statistics.PCStatsTest
 @since 19.05.2019 (23:13) */
public class PCStats implements Callable<String> {
    
    private static final List<String> PC_NAMES_IN_TABLE = new ArrayList<>();
    
    private String fileName = FileNames.FILENAME_VELKOMPCUSERAUTOTXT;
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private String sql;
    
    private String inetStats;
    
    private String countUni;
    
    /**
     {@link ru.vachok.networker.statistics.PCStatsTest#testCall()}
     <p>
     
     @return {@link #toString()}
     */
    @Override public String call() {
        this.inetStats = getPCStats();
        this.countUni = makeStatFiles();
        return getPCStats();
    }
    
    /**
     {@code sql = select * from pcuserauto} <br>
     this.{@link #fileName} = velkom_pcuserauto.txt <br>
     Connecting to: <a href="jdbc:mysql://server202.hosting.reg.ru:3306/u0466446_velkom" target=_blank>u0466446_velkom</a>
     <p>
     
     @see ru.vachok.networker.statistics.PCStatsTest#testSelectFrom()
     @return кол-во срязок ПК-Пользователь в таблице <b>pcuserauto</b>
     */
    protected int selectFrom() {
        final long stArt = System.currentTimeMillis();
        int retInt = 0;
        this.sql = ConstantsFor.SQL_SELECTFROM_PCUSERAUTO;
        File file = new File(fileName);
        try (Connection c = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
            try (PreparedStatement p = c.prepareStatement(sql)) {
                try (ResultSet r = p.executeQuery()) {
                    printResultsToFile(file, r);
                }
            }
        }
        catch (SQLException | IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".selectFrom", e));
        }
        String toCopy = "\\\\10.10.111.1\\Torrents-FTP\\" + file.getName();
        if (!UsefulUtilities.thisPC().toLowerCase().contains("home")) {
            toCopy = file.getName() + "_cp";
        }
        FileSystemWorker.copyOrDelFile(file, Paths.get(toCopy).toAbsolutePath().normalize(), false);
        return PC_NAMES_IN_TABLE.size();
    }
    
    private @NotNull String getPCStats() {
        int selectFrom = selectFrom();
        String retStr = "total pc: " + selectFrom;
        messageToUser.info(getClass().getSimpleName(), "pc stats: ", retStr);
        return retStr;
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("PCStats{");
        sb.append("countUni='").append(countUni).append('\'');
        sb.append(", inetStats='").append(inetStats).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    private void printResultsToFile(File file, @NotNull ResultSet r) throws IOException, SQLException {
        try (OutputStream outputStream = new FileOutputStream(file)) {
            try (PrintStream printStream = new PrintStream(outputStream, true)) {
                while (r.next()) {
                    if (sql.equals(ConstantsFor.SQL_SELECTFROM_PCUSERAUTO)) {
                        String toPrint = r.getString(2) + " " + r.getString(3);
                        PC_NAMES_IN_TABLE.add(toPrint);
                        printStream.println(toPrint);
                    }
                }
            }
        }
    }
    
    /**
     Writes file: {@link FileNames#FILENAME_PCAUTOUSERSUNIQ} from {@link FileNames#FILENAME_VELKOMPCUSERAUTOTXT}
     <p>
     
     @return {@link #countFreqOfUsers()}
     */
    private @NotNull String makeStatFiles() {
        List<String> readFileAsList = FileSystemWorker.readFileToList(FileNames.FILENAME_VELKOMPCUSERAUTOTXT);
        FileSystemWorker.writeFile(FileNames.FILENAME_PCAUTOUSERSUNIQ, readFileAsList.parallelStream().distinct());
        if (UsefulUtilities.thisPC().toLowerCase().contains("home")) {
            String toCopy = "\\\\10.10.111.1\\Torrents-FTP\\" + FileNames.FILENAME_PCAUTOUSERSUNIQ;
            FileSystemWorker.copyOrDelFile(new File(FileNames.FILENAME_PCAUTOUSERSUNIQ), Paths.get(toCopy).toAbsolutePath().normalize(), false);
        }
        return countFreqOfUsers();
    }
    
    /**
     {@code PcUser like: } a115.eatmeat.ru \e.v.drozhzhina\ntuser.dat.LOG1 <br>
     
     @return кол-во уникальных записей в файле <b>possible_users.txt</b>
     */
    private @NotNull String countFreqOfUsers() {
        List<String> pcAutoThisList = FileSystemWorker.readFileToList(new File(FileNames.FILENAME_PCAUTOUSERSUNIQ).getAbsolutePath());
        Collections.sort(pcAutoThisList);
        List<String> stringCollect = new ArrayList<>();
        Map<String, String> countFreqMap = new TreeMap<>();
        for (String pcUser : pcAutoThisList) {
            try {
                String userPC = pcUser.toLowerCase().split("\\Q \\\\E")[0];
                pcUser = pcUser.toLowerCase().split("\\Q \\\\E")[1].split("\\Q\\\\E")[0];
                String addToList = pcUser + ":" + userPC;
                stringCollect.add(addToList);
                int frequency = Collections.frequency(stringCollect, addToList);
                countFreqMap.put(addToList, "      times = " + frequency);
            }
            catch (IndexOutOfBoundsException ignore) {
                //20.06.2019 (11:09)
            }
        }
        String possibleUsers = "user_login_counter.txt";
        String absolutePath = new File(possibleUsers).getAbsolutePath();
        boolean fileWritten = FileSystemWorker.writeFile(absolutePath, countFreqMap);
        if (fileWritten) {
            return stringCollect.size() + " unique records.";
        }
        else {
            return "Error. File not written!\n\n\n\n" + absolutePath;
        }
    }
}