// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.DBMessenger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 Очистка от логов IIS на srv-mail3
 <p>
 Оставляет последние 5 дней

 @since 21.12.2018 (9:23) */
public class MailIISLogsCleaner extends SimpleFileVisitor<Path> implements Runnable {
    
    
    private static final MessageToUser LOGGER = new DBMessenger(MailIISLogsCleaner.class.getTypeName());
    
    private long filesSize;

    private List<String> toLog = new ArrayList<>();

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        toLog.add("Current directory: " + dir);
        toLog.add("Files: " + Objects.requireNonNull(dir.toFile().listFiles()).length);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (attrs.isRegularFile() && attrs.creationTime().toMillis() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5)) {
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
        toLog.add(new TForms().fromArray(exc, false));
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
        Path iisLogsDir = Paths.get("\\\\srv-mail3.eatmeat.ru\\c$\\inetpub\\logs\\LogFiles\\W3SVC1\\");
        toLog.add("Starting IIS logs cleaner.");
        toLog.add("Date: ");
        toLog.add(new Date(ConstantsFor.getAtomicTime()).toString());
        try {
            Files.walkFileTree(iisLogsDir, this);
        } catch (IOException e) {
            LOGGER.error(FileSystemWorker.error(getClass().getSimpleName() + ".run", e));
        }
        FileSystemWorker.writeFile(this.getClass().getSimpleName() + ConstantsFor.FILEEXT_LOG, toLog);
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("MailIISLogsCleaner{");
        sb.append("filesSize=").append(filesSize);
        sb.append(", toLog=").append(new TForms().fromArray(toLog));
        sb.append('}');
        return sb.toString();
    }
}
