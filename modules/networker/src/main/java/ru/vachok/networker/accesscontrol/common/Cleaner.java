// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.message.MessageLocal;

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
 @see ru.vachok.networker.accesscontrol.common.CleanerTest
 @since 25.06.2019 (11:37) */
public class Cleaner extends SimpleFileVisitor<Path> implements Callable<String> {
    
    
    private File fileWithInfoAboutOldCommon;
    
    private Map<Path, String> pathAttrMap = new ConcurrentHashMap<>();
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private long lastModifiedLog;
    
    public Cleaner(@NotNull File fileWithInfoAboutOldCommon) {
        this.fileWithInfoAboutOldCommon = fileWithInfoAboutOldCommon;
        this.lastModifiedLog = fileWithInfoAboutOldCommon.lastModified();
    }
    
    /**
     @return имя файла-лога, с информацией об удалениях.
     */
    @Override
    public String call() {
        if (pathAttrMap.size() == 0) {
            List<String> remainFiles = fillMapFromFile();
            if (makeDeletions()) {
                FileSystemWorker.writeFile(fileWithInfoAboutOldCommon.getAbsolutePath(), remainFiles.stream());
                fileWithInfoAboutOldCommon.setLastModified(lastModifiedLog);
            }
            else {
                throw new InvokeIllegalException(getClass().getTypeName() + ".call");
            }
        }
        return "Remain in " + fileWithInfoAboutOldCommon + " " + FileSystemWorker
            .countStringsInFile(fileWithInfoAboutOldCommon.toPath().toAbsolutePath().normalize()) + " positions.";
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", Cleaner.class.getSimpleName() + "[\n", "\n]")
            .add("fileWithInfoAboutOldCommon = " + fileWithInfoAboutOldCommon.toPath().toAbsolutePath().normalize())
            .add("pathAttrMap = " + pathAttrMap.size())
            .add("lastModifiedLog = " + new Date(lastModifiedLog))
            .toString();
    }
    
    protected Map<Path, String> getPathAttrMap() {
        return Collections.unmodifiableMap(pathAttrMap);
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
    
    private @NotNull List<String> fillMapFromFile() {
        int limitOfDeleteFiles = countLimitOfDeleteFiles();
        List<String> remainFiles = FileSystemWorker.readFileToList(fileWithInfoAboutOldCommon.toPath().toAbsolutePath().normalize().toString());
        Random random = new Random();
        
        for (int i = 0; i < limitOfDeleteFiles; i++) {
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
        return remainFiles;
    }
    
    private int countLimitOfDeleteFiles() {
        int stringsInLogFile = FileSystemWorker.countStringsInFile(fileWithInfoAboutOldCommon.toPath().toAbsolutePath().normalize());
        
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
}
