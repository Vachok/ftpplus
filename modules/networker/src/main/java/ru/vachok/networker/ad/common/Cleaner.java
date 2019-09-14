// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.common;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;


/**
 @see ru.vachok.networker.ad.common.CleanerTest
 @since 25.06.2019 (11:37) */
public class Cleaner extends SimpleFileVisitor<Path> implements Callable<String> {
    
    
    private Map<Path, String> pathAttrMap = new ConcurrentHashMap<>();
    
    private MessageToUser messageToUser = ru.vachok.networker.restapi.message.MessageToUser.getInstance(ru.vachok.networker.restapi.message.MessageToUser.LOCAL_CONSOLE, getClass().getSimpleName());
    
    private long lastModifiedLog;
    
    private List<String> remainFiles = new ArrayList<>();
    
    /**
     @return имя файла-лога, с информацией об удалениях.
     */
    @Override
    public String call() {
        if (pathAttrMap.size() == 0) {
            fillMapFromDB();
        }
        else makeDeletions();
        
        return MessageFormat.format("{0} complete: {1}", this.getClass().getSimpleName(), makeDeletions());
    }
    
    private void fillMapFromDB() {
        Random random = new Random();
        int deleteTodayLimit = limitOfDeleteFiles();
        
        for (int i = 0; i < deleteTodayLimit; i++) {
            int index = random.nextInt(remainFiles.size());
            String deleteFileAsString = remainFiles.get(index);
            try {
                String[] pathAndAttrs = deleteFileAsString.split(", ,");
                pathAttrMap.putIfAbsent(Paths.get(pathAndAttrs[0]), pathAndAttrs[1]);
                remainFiles.remove(index);
            }
            catch (IndexOutOfBoundsException | NullPointerException e) {
                messageToUser.error(e.getMessage());
            }
        }
    }
    
    private void getDBInformation() {
        try(Connection connection = DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT).getDefaultConnection(ConstantsFor.STR_VELKOM);
            PreparedStatement preparedStatement = connection.prepareStatement("select * from oldfiles");
            ResultSet resultSet= preparedStatement.getResultSet();){
            resultSet.setFetchSize(3);
            while (resultSet.next()){
                remainFiles.add(resultSet.getString(2));
            }
        }catch (SQLException e){
            messageToUser.error(e.getMessage() + " see line: 70");
        }
    }
    
    private boolean makeDeletions() {
        boolean retBool = false;
        long releasedSpace = 0;
        for (Map.Entry<Path, String> pathStringEntry : pathAttrMap.entrySet()) {
            try (OutputStream outputStream = new FileOutputStream(getClass().getSimpleName() + ".log", true);
                 PrintStream printStream = new PrintStream(outputStream, true, "UTF-8")
            ) {
                Path keyPathToDelete = pathStringEntry.getKey();
                System.out.println("Trying remove: " + keyPathToDelete + " (" + keyPathToDelete.toFile()
                        .length() / ConstantsFor.MBYTE + " megabytes, attributes: " + pathStringEntry
                        .getValue() + ")");
                
                if (Files.deleteIfExists(keyPathToDelete)) {
                    releasedSpace += keyPathToDelete.toFile().length();
                    releasedSpace /= ConstantsFor.GBYTE;
                    printStream.println(keyPathToDelete + " : " + pathStringEntry.getValue() + " is DELETED. Total space released in gigabytes: " + releasedSpace);
                    retBool = true;
                }
                else {
                    printStream.println(pathStringEntry.getKey() + " : " + pathStringEntry.getValue());
                }
            }
            catch (IOException e) {
                messageToUser.error(e.getMessage());
                retBool = false;
            }
        }
        return retBool;
    }
    
    private int limitOfDeleteFiles() {
        int stringsInLogFile = remainFiles.size();
        
        if (System.currentTimeMillis() < lastModifiedLog + TimeUnit.DAYS.toMillis(1)) {
            System.out.println(stringsInLogFile = (stringsInLogFile / 100) * 10);
        }
        else if (System.currentTimeMillis() < lastModifiedLog + TimeUnit.DAYS.toMillis(2)) {
            System.out.println(stringsInLogFile = (stringsInLogFile / 100) * 25);
        }
        else if (System.currentTimeMillis() < lastModifiedLog + TimeUnit.DAYS.toMillis(3)) {
            System.out.println(stringsInLogFile = (stringsInLogFile / 100) * 75);
        }
        else {
            System.out.println(stringsInLogFile);
        }
        
        return stringsInLogFile;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Cleaner{");
        sb.append("pathAttrMap=").append(pathAttrMap);
        
        sb.append(", lastModifiedLog=").append(lastModifiedLog);
        sb.append('}');
        return sb.toString();
    }
    
    protected Map<Path, String> getPathAttrMap() {
        return Collections.unmodifiableMap(pathAttrMap);
    }
}
