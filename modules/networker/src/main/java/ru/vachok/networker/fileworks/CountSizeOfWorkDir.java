// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.fileworks;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.services.MessageLocal;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


/**
 Class ru.vachok.networker.fileworks.CountSizeOfWorkDir
 <p>
 
 @since 06.04.2019 (13:15) */
public class CountSizeOfWorkDir extends SimpleFileVisitor<Path> implements ProgrammFilesWriter {
    
    private long sizeBytes;
    
    private Map<Long, String> longStrPathMap = new TreeMap<>();
    
    private String fileName;
    
    private PrintStream printStream;
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    public CountSizeOfWorkDir(String fileName) {
        this.fileName = fileName;
    
        try (OutputStream outputStream = new FileOutputStream(fileName)) {
            this.printStream = new PrintStream(outputStream, true);
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
    }
    
    @Override public String call() throws Exception {
        return getSizeOfDir();
    }
    
    @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (file.toFile().length() <= 0 && !file.toFile().getName().equalsIgnoreCase("scan.tmp")) {
            try {
                Files.deleteIfExists(file);
            }
            catch (Exception e) {
                file.toFile().deleteOnExit();
            }
        }
        if (attrs.isRegularFile()) {
            this.sizeBytes += file.toFile().length();
            if (attrs.lastAccessTime().toMillis() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3)) {
                longStrPathMap.putIfAbsent(file.toFile().length(),
                    file.toAbsolutePath() + "<b> 3 days old...</b>");
            }
            else {
                longStrPathMap.putIfAbsent(file.toFile().length(), file.toAbsolutePath().toString());
            }
        }
        return FileVisitResult.CONTINUE;
    }
    
    
    @Override public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        printStream.println(file);
        printStream.println(new TForms().fromArray(exc, false));
        return FileVisitResult.CONTINUE;
    }
    
    
    @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
    
    
    @Override public boolean writeFile(List<?> toWriteList) {
        throw new IllegalComponentStateException("Not ready");
    }
    
    
    @Override public boolean writeFile(Stream<?> toWriteStream) {
        File file = new File(fileName + "stream");
        printStream.println("Writing stream. Now: ");
        printStream.print(new Date());
        toWriteStream.forEach(x->printStream.println(x));
        return file.exists();
    }
    
    
    @Override public boolean writeFile(Map<?, ?> toWriteMap) {
        throw new IllegalComponentStateException("Not ready");
    }
    
    
    @Override public boolean writeFile(File toWriteFile) {
        return false;
    }
    
    
    @Override
    public boolean writeFile(Exception e) {
        File file = new File(fileName + "_" + LocalTime.now().toSecondOfDay() + "_.err");
        printStream.println(new Date());
        printStream.println();
        printStream.println(new TForms().fromArray(e, false));
        messageToUser.info(file + " is " + file.exists());
        return file.exists();
    }
    
    
    @Override public String error(String fileName, Exception e) {
        boolean isWritten = writeFile(e);
        if (isWritten) {
            return new File(fileName).getAbsolutePath();
        }
        else {
            return "NO FILE!";
        }
    }
    
    
    @Override public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    private String getSizeOfDir() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        Files.walkFileTree(Paths.get(".").normalize(), this);
        stringBuilder.append("Total size = ").append(sizeBytes / ConstantsFor.KBYTE / ConstantsFor.KBYTE).append(" MB<br>\n");
        longStrPathMap.forEach((x, y)->stringBuilder.append(String.format("%.02f", (float) x / ConstantsFor.KBYTE)).append(" kb in: ").append(y).append("<br>\n"));
        return stringBuilder.toString();
    }
}