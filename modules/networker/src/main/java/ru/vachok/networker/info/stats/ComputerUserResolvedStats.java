// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info.stats;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.io.*;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Callable;


/**
 @see ComputerUserResolvedStatsTest
 @since 19.05.2019 (23:13) */
class ComputerUserResolvedStats implements Callable<String>, Runnable, Stats {


    /**
     Файл уникальных записей из БД velkom-pcuserauto
     */
    public static final String PCUSERAUTO_UNIQ = "pcusersauto.uniq";

    private static final List<String> PC_NAMES_IN_TABLE = new ArrayList<>();

    private final String fileName = FileNames.VELKOM_PCUSERAUTO_TXT;

    private final List<String> pcAndUser = new ArrayList<>();

    private final MessageToUser messageToUser = ru.vachok.networker.restapi.message.MessageToUser
        .getInstance(ru.vachok.networker.restapi.message.MessageToUser.TRAY, getClass().getSimpleName());

    private String countPCs;

    private String countUni;

    private String sql = ConstantsFor.SQL_SELECTFROM_PCUSERAUTO;

    private String aboutWhat;

    @Override
    public void run() {
        call();
    }

    @Override
    public String call() {
        Thread.currentThread().setName(this.getClass().getSimpleName());
        try {
            this.countPCs = countPC();
            this.countUni = makeStatFiles();
        }
        catch (InvokeIllegalException e) {
            messageToUser.error("ComputerUserResolvedStats.call", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
        messageToUser.info("PC Stats", countPCs, countUni);
        return countPCs;
    }

    @Override
    public String getInfo() {
        return call();
    }

    @Override
    public void setClassOption(@NotNull Object option) {
        this.aboutWhat = (String) option;
    }

    @NotNull
    private String countPC() throws InvokeIllegalException {
        int selectFrom = selectFrom();
        String retStr = "total pc: " + selectFrom;
        messageToUser.info(getClass().getSimpleName(), "pc stats: ", retStr);
        return retStr;
    }

    /**
     Writes file: {@link ComputerUserResolvedStats#PCUSERAUTO_UNIQ} from {@link FileNames#VELKOM_PCUSERAUTO_TXT}
     <p>

     @return {@link #countFreqOfUsers()}
     */
    @NotNull
    private String makeStatFiles() throws InvokeIllegalException {
        List<String> readFileAsList = FileSystemWorker.readFileToList(FileNames.VELKOM_PCUSERAUTO_TXT);
        FileSystemWorker.writeFile(PCUSERAUTO_UNIQ, readFileAsList.parallelStream().distinct());
        if (UsefulUtilities.thisPC().toLowerCase().contains("home")) {
            String toCopy = "\\\\10.10.111.1\\Torrents-FTP\\" + PCUSERAUTO_UNIQ;
            FileSystemWorker.copyOrDelFile(new File(PCUSERAUTO_UNIQ), Paths.get(toCopy).toAbsolutePath().normalize(), false);
        }
        return countFreqOfUsers();
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
     {@code sql = select * from pcuserauto} <br>
     this.{@link #fileName} = velkom_pcuserauto.txt <br>
     Connecting to: <a href="jdbc:mysql://server202.hosting.reg.ru:3306/u0466446_velkom" target=_blank>u0466446_velkom</a>
     <p>

     @return кол-во срязок ПК-Пользователь в таблице <b>pcuserauto</b>
     */
    protected int selectFrom() throws InvokeIllegalException {
        this.sql = ConstantsFor.SQL_SELECTFROM_PCUSERAUTO;
        File file = new File(fileName);
        try (Connection c = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_PCUSERAUTO_FULL)) {
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
    public String getInfoAbout(String aboutWhat) {
        this.aboutWhat = aboutWhat;
        return call();
    }

    /**
     {@code PcUser like: } a115.eatmeat.ru \e.v.drozhzhina\ntuser.dat.LOG1 <br>

     @return кол-во уникальных записей в файле <b>possible_users.txt</b>
     */
    @NotNull
    private String countFreqOfUsers() {
        List<String> pcAutoThisList = FileSystemWorker.readFileToList(new File(PCUSERAUTO_UNIQ).getAbsolutePath());

        Collections.sort(pcAutoThisList);

        for (String pcUser : pcAutoThisList) {
            try {
                String userPC = pcUser.toLowerCase().split(" ")[0];
                pcAndUser.add(pcUser.toLowerCase().split(" ")[1] + ":" + userPC.replace(ConstantsFor.DOMAIN_EATMEATRU, ""));
            }
            catch (IndexOutOfBoundsException ignore) {
                //20.06.2019 (11:09)
            }

        }
        return MessageFormat.format("File {0} is {1}", FileNames.USER_LOGIN_COUNTER_TXT, String.valueOf(writeMapWithCount()));
    }

    private boolean writeMapWithCount() {
        Map<String, Integer> mapWithCount = new TreeMap<>();
        File countFile = new File(FileNames.USER_LOGIN_COUNTER_TXT);
        boolean fileWritten = countFile.delete();
        if (fileWritten) {
            for (String pcUser : pcAndUser) {
                int freq = Collections.frequency(pcAndUser, pcUser);
                mapWithCount.put(pcUser, freq);
            }
        }
        String strToSend = AbstractForms.fromArray(mapWithCount);
        FileSystemWorker.writeFile(FileNames.USER_LOGIN_COUNTER_TXT, strToSend);
        ru.vachok.networker.restapi.message.MessageToUser.getInstance(ru.vachok.networker.restapi.message.MessageToUser.EMAIL, this.getClass().getSimpleName())
            .info(strToSend);
        return countFile.exists();
    }

    @Override
    public String toString() {
        return new StringJoiner(",\n", ComputerUserResolvedStats.class.getSimpleName() + "[\n", "\n]")
            .add("fileName = '" + fileName + "'")
            .add("inetStats = '" + countPCs + "'")
            .add("countUni = '" + countUni + "'")
            .add("sql = '" + sql + "'")
            .add("aboutWhat = '" + aboutWhat + "'")
            .toString();
    }
}