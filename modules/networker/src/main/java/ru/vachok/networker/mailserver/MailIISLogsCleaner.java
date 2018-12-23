package ru.vachok.networker.mailserver;


import org.slf4j.Logger;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 Очистка от логов IIS на srv-mail3
 <p>
 Оставляет последние 5 дней

 @since 21.12.2018 (9:23) */
public class MailIISLogsCleaner extends FileSystemWorker implements Runnable {

    private static final Logger LOGGER = AppComponents.getLogger();

    private long filesSize = 0;

    private PrintWriter printWriter;

    {
        try (OutputStream outputStream = new FileOutputStream("iislogsclean.log")) {
            printWriter = new PrintWriter(outputStream, true);
        } catch (IOException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        printWriter.println("Current directory: " + dir.toString());
        printWriter.println("Files: " + Objects.requireNonNull(dir.toFile().listFiles()).length);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (attrs.isRegularFile() && attrs.creationTime().toMillis() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5)) {
            this.filesSize = this.filesSize + file.toFile().length();
            printWriter.println("Removing file: " + file);
            boolean deleteIfExists = Files.deleteIfExists(file);
            printWriter.println(deleteIfExists);
            return FileVisitResult.CONTINUE;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        printWriter.println(file);
        printWriter.println(new TForms().fromArray(exc, false));
        return super.visitFileFailed(file, exc);
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        printWriter.println(filesSize / ConstantsFor.MBYTE + " total megabytes removed");
        printWriter.println(Objects.requireNonNull(dir.toFile().listFiles()).length + " files left");
        return super.postVisitDirectory(dir, exc);
    }

    @Override
    public void run() {
        Path iisLogsDir = Paths.get("\\\\srv-mail3.eatmeat.ru\\c$\\inetpub\\logs\\LogFiles\\W3SVC1\\");
        printWriter.println("Starting IIS logs cleaner.");
        printWriter.println("Date: ");
        printWriter.println(new Date(ConstantsFor.getAtomicTime()));
        try {
            Files.walkFileTree(iisLogsDir, this);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            printWriter.println(e.getMessage());
            printWriter.println(new TForms().fromArray(e, false));
        }
    }
}
