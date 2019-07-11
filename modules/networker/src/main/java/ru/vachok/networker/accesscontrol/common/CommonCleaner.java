// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.InvokeIllegalException;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.accesscontrol.common.CommonCleanerTest
 @since 25.06.2019 (11:37) */
public class CommonCleaner extends SimpleFileVisitor<Path> implements Callable<String> {
    
    
    private File fileWithInfoAboutOldCommon;
    
    private Map<Path, String> pathAttrMap = new ConcurrentHashMap<>();
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private long lastModifiedLog;
    
    public CommonCleaner(File fileWithInfoAboutOldCommon) {
        this.fileWithInfoAboutOldCommon = fileWithInfoAboutOldCommon;
    }
    
    public Map<Path, String> getPathAttrMap() {
        return Collections.unmodifiableMap(pathAttrMap);
    }
    
    /**
     @return имя файла-лога, с информацией об удалениях.
     */
    @Override public String call() {
        if (pathAttrMap.size() == 0) {
            List<String> remainFiles = fillMapFromFile();
            if (makeDeletions()) {
                if (FileSystemWorker.writeFile(fileWithInfoAboutOldCommon.getAbsolutePath(), remainFiles.stream())) {
                    boolean isLastModifiedSet = fileWithInfoAboutOldCommon.setLastModified(lastModifiedLog);
                    System.out.println(new StringBuilder()
                        .append(fileWithInfoAboutOldCommon.getName()).append(" is Last Modified Set = ")
                        .append(isLastModifiedSet).append(" (")
                        .append(new Date(fileWithInfoAboutOldCommon.lastModified())).append(")"));
                }
            }
            else {
                throw new InvokeIllegalException(getClass().getTypeName() + ".call");
            }
        }
        return "Remain in " + fileWithInfoAboutOldCommon + " " + FileSystemWorker.countStringsInFile(fileWithInfoAboutOldCommon.toPath().toAbsolutePath().normalize()) + " positions.";
    }
    
    private boolean makeDeletions() {
        boolean retBool = false;
        long releasedSpace = 0;
        for (Map.Entry<Path, String> pathStringEntry : pathAttrMap.entrySet()) {
            try (OutputStream outputStream = new FileOutputStream(getClass().getSimpleName() + ".log", true);
                 PrintStream printStream = new PrintStream(outputStream, true, "UTF-8")
            ) {
                Path keyPathToDelete = pathStringEntry.getKey();
                System.out.println("Trying remove: " + keyPathToDelete + " (" + keyPathToDelete.toFile().length() / ConstantsFor.MBYTE + " megabytes, attributes: " + pathStringEntry
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
    
    private List<String> fillMapFromFile() {
        int limitOfDeleteFiles = countLimitOfDeleteFiles();
        List<String> fileAsList = FileSystemWorker.readFileToList(fileWithInfoAboutOldCommon.toPath().toAbsolutePath().normalize().toString());
        Random random = new Random();
        
        for (int i = 0; i < limitOfDeleteFiles; i++) {
            int index = random.nextInt(fileAsList.size());
            String deleteFileAsString = fileAsList.get(index);
            try {
                String[] pathAndAttrs = deleteFileAsString.split(", ,");
                pathAttrMap.putIfAbsent(Paths.get(pathAndAttrs[0]), pathAndAttrs[1]);
                fileAsList.remove(index);
            }
            catch (IndexOutOfBoundsException | NullPointerException e) {
                messageToUser.error(e.getMessage());
            }
        }
        return fileAsList;
    }
    
    private int countLimitOfDeleteFiles() {
        int stringsInLogFile = FileSystemWorker.countStringsInFile(fileWithInfoAboutOldCommon.toPath().toAbsolutePath().normalize());
        this.lastModifiedLog = fileWithInfoAboutOldCommon.lastModified();
        
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
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("CommonCleaner{");
        sb.append("fileWithInfoAboutOldCommon=").append(fileWithInfoAboutOldCommon);
        sb.append(", lastModifiedLog=").append(lastModifiedLog);
        sb.append('}');
        return sb.toString();
    }
}
