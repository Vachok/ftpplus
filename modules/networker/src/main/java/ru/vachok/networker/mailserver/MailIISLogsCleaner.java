package ru.vachok.networker.mailserver;


import org.slf4j.Logger;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
public class MailIISLogsCleaner extends FileSystemWorker implements Runnable {

    private static final Logger LOGGER = AppComponents.getLogger(MailIISLogsCleaner.class.getSimpleName());

    private long filesSize = 0;

    private List<String> toLog = new ArrayList<>();

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        toLog.add("Current directory: " + dir.toString());
        toLog.add("Files: " + Objects.requireNonNull(dir.toFile().listFiles()).length);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (attrs.isRegularFile() && attrs.creationTime().toMillis() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5)) {
            this.filesSize = this.filesSize + file.toFile().length();
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
            LOGGER.error(e.getMessage(), e);
            toLog.add(e.getMessage());
            toLog.add(new TForms().fromArray(e, false));
        }
        FileSystemWorker.recFile(this.getClass().getSimpleName() + ConstantsFor.LOG, toLog);
    }
}
