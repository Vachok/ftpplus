// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.FileNames;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.*;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Callable;


/**
 @see ru.vachok.networker.info.ComputerUserResolvedStatsTest
 @since 19.05.2019 (23:13) */
class ComputerUserResolvedStats extends Stats implements Callable<String>, Runnable {
    
    private static final List<String> PC_NAMES_IN_TABLE = new ArrayList<>();
    
    private String fileName = FileNames.FILENAME_VELKOMPCUSERAUTOTXT;
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private String inetStats;
    
    private String countUni;
    
    private String sql = ConstantsFor.SQL_SELECTFROM_PCUSERAUTO;
    
    private String aboutWhat;
    
    @Override
    public void run() {
        call();
    }
    
    @Override
    public void setOption(@NotNull Object option) {
        this.aboutWhat = (String) option;
    }
    
    @Override
    public String getInfo() {
        return call();
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.aboutWhat = aboutWhat;
        return call();
    }
    
    /**
     {@code sql = select * from pcuserauto} <br>
     this.{@link #fileName} = velkom_pcuserauto.txt <br>
     Connecting to: <a href="jdbc:mysql://server202.hosting.reg.ru:3306/u0466446_velkom" target=_blank>u0466446_velkom</a>
     <p>
     
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
    
    @Override
    public String call() {
        Thread.currentThread().setName(this.getClass().getSimpleName());
        this.inetStats = countPC();
        this.countUni = makeStatFiles();
        return inetStats;
    }
    
    private @NotNull String countPC() {
        int selectFrom = selectFrom();
        String retStr = "total pc: " + selectFrom;
        messageToUser.info(getClass().getSimpleName(), "pc stats: ", retStr);
        return retStr;
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
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", ComputerUserResolvedStats.class.getSimpleName() + "[\n", "\n]")
            .add("fileName = '" + fileName + "'")
            .add("inetStats = '" + inetStats + "'")
            .add("countUni = '" + countUni + "'")
            .add("sql = '" + sql + "'")
            .add("aboutWhat = '" + aboutWhat + "'")
            .toString();
    }
}