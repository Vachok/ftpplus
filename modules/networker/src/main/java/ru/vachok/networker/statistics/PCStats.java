// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.statistics;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.abstr.DataBaseRegSQL;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.sql.*;
import java.util.*;


/**
 Class ru.vachok.networker.statistics.PCStats
 <p>
 
 @since 19.05.2019 (23:13) */
public class PCStats implements DataBaseRegSQL {
    
    
    private static final List<String> PC_NAMES_IN_TABLE = new ArrayList<>();
    
    private StatsOfNetAndUsers statsOfNetAndUsers = new WeekStats();
    
    private String fileName;
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private String sql;
    
    public static List<String> getPcNamesInTable() {
        return PC_NAMES_IN_TABLE;
    }
    
    public String getPCStats() {
        System.out.println(statsOfNetAndUsers.getPCStats());
        return countStat();
    }
    
    @Override public void setSavepoint(Connection connection) {
    
    }
    
    @Override public MysqlDataSource getDataSource() {
        return null;
    }
    
    @Override public Savepoint getSavepoint(Connection connection) {
        return null;
    }
    
    @Override public int selectFrom() {
        final long stArt = System.currentTimeMillis();
        int retInt = 0;
        File file = new File(fileName);
        try (Connection c = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
            try (PreparedStatement p = c.prepareStatement(sql)) {
                try (ResultSet r = p.executeQuery()) {
                    try (OutputStream outputStream = new FileOutputStream(file)) {
                        try (PrintStream printStream = new PrintStream(outputStream, true)) {
                            while (r.next()) {
                                if (sql.equals(ConstantsFor.SQL_SELECTFROM_PCUSERAUTO)) {
                                    pcUserAutoSelect(r, printStream);
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (SQLException | IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".selectFrom", e));
        }
        String toCopy = "\\\\10.10.111.1\\Torrents-FTP\\" + file.getName();
        if (!ConstantsFor.thisPC().toLowerCase().contains("home")) {
            toCopy = file.getName() + "_cp";
        }
        FileSystemWorker.copyOrDelFile(file, toCopy, false);
        file.deleteOnExit();
        return PC_NAMES_IN_TABLE.size();
    }
    
    @Override public int insertTo() {
        return 0;
    }
    
    @Override public int deleteFrom() {
        return 0;
    }
    
    @Override public int updateTable() {
        return 0;
    }
    
    private void pcUserAutoSelect(ResultSet r, PrintStream prStream) throws SQLException {
        String toPrint = r.getString(2) + " " + r.getString(3);
        PC_NAMES_IN_TABLE.add(toPrint);
        prStream.println(toPrint);
    }
    
    private String countStat() {
        List<String> readFileAsList = FileSystemWorker.readFileToList(ConstantsFor.FILENAME_VELKOMPCUSERAUTOTXT);
        FileSystemWorker.writeFile(ConstantsFor.FILENAME_PCAUTODISTXT, readFileAsList.parallelStream().distinct());
        if (ConstantsFor.thisPC().toLowerCase().contains("home")) {
            String toCopy = "\\\\10.10.111.1\\Torrents-FTP\\" + ConstantsFor.FILENAME_PCAUTODISTXT;
            FileSystemWorker.copyOrDelFile(new File(ConstantsFor.FILENAME_PCAUTODISTXT), toCopy, false);
        }
        return countFreqOfUsers();
    }
    
    private String countFreqOfUsers() {
        List<String> pcAutoThisList = FileSystemWorker.readFileToList(new File(ConstantsFor.FILENAME_PCAUTODISTXT).getAbsolutePath());
        Collections.sort(pcAutoThisList);
        Collection<String> stringCollect = new LinkedList<>();
        for (String pcUser : pcAutoThisList) {
            stringCollect.add(Collections.frequency(pcAutoThisList, pcUser) + "times = " + pcUser);
        }
        String absolutePath = new File("possible_users.txt").getAbsolutePath();
        boolean fileWritten = FileSystemWorker.writeFile(absolutePath, stringCollect.stream());
        if (fileWritten) {
            return absolutePath;
        }
        else {
            return "Error. File not written!\n\n\n\n" + absolutePath;
        }
    }
    
}