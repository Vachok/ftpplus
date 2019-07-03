package ru.vachok.networker.accesscontrol.common;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.systray.SystemTrayHelper;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 Очистка папки \\192.168.14.10\IT-Backup\SRV-FS\Archives
 
 @see ru.vachok.networker.accesscontrol.common.ArchivesAutoCleanerTest
 @see SystemTrayHelper
 @since 15.11.2018 (14:09) */
public class ArchivesAutoCleaner extends SimpleFileVisitor<Path> implements Runnable {
    
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchivesAutoCleaner.class.getSimpleName());

    /**
     Первоначальная папка
     */
    private static final String SRV_FS_ARCHIVES = "\\\\192.168.14.10\\IT-Backup\\SRV-FS\\Archives\\";

    @SuppressWarnings("CanBeFinal")
    private static PrintWriter printWriter;
    
    private List<String> copyList = new ArrayList<>();

    static {
        try {
            OutputStream outputStream = new FileOutputStream("cleaner.log.txt");
            printWriter = new PrintWriter(outputStream, true);
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        String msg = new StringBuilder().append("Cleaning the directory: ")
            .append(dir).append("\n")
            .append(attrs.lastModifiedTime()).append(" is last modified time.")
            .append(Objects.requireNonNull(dir.toFile().listFiles()).length)
            .append(" files in.").toString();
        LOGGER.info(msg);
        return FileVisitResult.CONTINUE;
    }

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        File ditToCopyFiles = new File("\\\\192.168.14.10\\IT-Backup\\SRV-FS\\bluray\\");
        FileStore fileStore = Files.getFileStore(ditToCopyFiles.toPath());
        long totalSpace = fileStore.getTotalSpace();
        if (attrs.isRegularFile() &&
            totalSpace < ConstantsFor.GBYTE * 47 &&
            attrs.lastModifiedTime().toMillis() < new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365)).getTime()) {

            String copiedFilePathStr = file.toAbsolutePath().toString();
            copiedFilePathStr = copiedFilePathStr.replaceAll("\\Q\\\\192.168.14.10\\IT-Backup\\SRV-FS\\Archives\\\\E",
                "\\\\192.168.14.10\\IT-Backup\\SRV-FS\\bluray\\");

            Path toCPPath = Paths.get(copiedFilePathStr);
            Path copyPath = Files.copy(file, toCPPath);
            Files.delete(file);
    
            String msg = file + " is copied!\n" + copyPath.toAbsolutePath();
            printWriter.println(msg);
            return FileVisitResult.CONTINUE;
        } else return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        String size = exc.getMessage() + " visit failed (" + file.toAbsolutePath() + ")";
        LOGGER.error(size);
        return FileVisitResult.CONTINUE;
    }

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        try {
            if (dir.toFile().isDirectory() & dir.getNameCount() == 0) {
                Files.delete(dir);
                String msg = dir + " deleted!";
                LOGGER.warn(msg);
            }
            return FileVisitResult.CONTINUE;
        } catch (Exception e) {
            return FileVisitResult.CONTINUE;
        }
    }
    
    /**
     @see ru.vachok.networker.accesscontrol.common.ArchivesAutoCleanerTest#testRun()
     */
    @Override
    public void run() {
        FileVisitor<Path> archivesAutoCleaner = new ArchivesAutoCleaner();
        try {
            Files.walkFileTree(Paths.get(SRV_FS_ARCHIVES), archivesAutoCleaner);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
    }

}
