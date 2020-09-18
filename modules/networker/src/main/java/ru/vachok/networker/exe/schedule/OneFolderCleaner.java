// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import com.google.gson.JsonObject;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 Очистка от логов IIS на srv-mail3
 <p>
 Оставляет последние 5 дней
 
 @since 21.12.2018 (9:23) */
public class OneFolderCleaner extends SimpleFileVisitor<Path> implements Runnable {
    
    
    private static final String EXT_LOG = ".log";
    
    private static final MessageToUser LOGGER = MessageToUser.getInstance(MessageToUser.DB, OneFolderCleaner.class.getTypeName());
    
    private long filesSize;
    
    private final List<String> toLog = new ArrayList<>();
    
    private final String cleanPath;
    
    private final long cleanDuration;
    
    public OneFolderCleaner() {
        this.cleanPath = "\\\\srv-mail3.eatmeat.ru\\c$\\inetpub\\logs\\LogFiles\\W3SVC1\\";
        this.cleanDuration = 5;
    }
    
    public OneFolderCleaner(String cleanPath, long cleanDuration) {
        this.cleanPath = cleanPath;
        this.cleanDuration = cleanDuration;
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        toLog.add("Current directory: " + dir);
        toLog.add(ConstantsFor.FILES + Objects.requireNonNull(dir.toFile().listFiles()).length);
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (attrs.isRegularFile() && attrs.creationTime().toMillis() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(cleanDuration)) {
            this.filesSize += file.toFile().length();
            toLog.add("Removing file: " + file);
            boolean deleteIfExists = Files.deleteIfExists(file);
            toLog.add(deleteIfExists + " deleteIfExists");
            return FileVisitResult.CONTINUE;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        toLog.add(file.toString());
        toLog.add(AbstractForms.fromArray(exc));
        return super.visitFileFailed(file, exc);
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        toLog.add(filesSize / ConstantsFor.MBYTE + " total megabytes removed");
        toLog.add(Objects.requireNonNull(dir.toFile().listFiles()).length + " files left");
        return super.postVisitDirectory(dir, exc);
    }

    @Override
    public void run() {
        Path cleanDir = Paths.get(cleanPath);
        toLog.add("Starting IIS logs cleaner.");
        toLog.add("Date: ");
        toLog.add(new Date(UsefulUtilities.getAtomicTime()).toString());
        try {
            Files.walkFileTree(cleanDir, this);
        }
        catch (IOException e) {
            LOGGER.error(FileSystemWorker.error(getClass().getSimpleName() + ".run", e));
        }
        FileSystemWorker.writeFile(cleanDuration + EXT_LOG, toLog);
    }
    
    @Override
    public String toString() {
        final JsonObject jo = new JsonObject();
        jo.addProperty(ConstantsFor.JSONNAME_CLASS, "OneFolderCleaner");
        jo.addProperty("toLog", toLog.size());
        jo.addProperty(", filesSize", filesSize);
        jo.addProperty(", cleanPath", cleanPath);
        return jo.toString();
    }
}
