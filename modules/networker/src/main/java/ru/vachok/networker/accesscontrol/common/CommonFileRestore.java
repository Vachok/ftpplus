package ru.vachok.networker.accesscontrol.common;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.accesscontrol.common.CommonFileRestoreTest
 @since 05.07.2019 (10:16) */
public class CommonFileRestore extends SimpleFileVisitor<Path> implements Callable<String> {
    
    
    private Path restoreFilePattern;
    
    private int restorePeriodDays;
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private List<String> restoredFiles = new ArrayList<>();
    
    public CommonFileRestore(String restoreFilePattern, String restorePeriodDays) {
        this.restoreFilePattern = Paths.get(restoreFilePattern);
        this.restorePeriodDays = Integer.parseInt(restorePeriodDays);
    }
    
    public CommonFileRestore(String restoreFilePattern) {
        this.restoreFilePattern = Paths.get(restoreFilePattern);
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("CommonFileRestore{");
        sb.append("restoreFilePattern='").append(restoreFilePattern).append('\'');
        sb.append(", restorePeriodDays='").append(restorePeriodDays).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    @Override public String call() {
        return searchFiles();
    }
    
    @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (dir.equals(restoreFilePattern)) {
            return FileVisitResult.TERMINATE;
        }
        else {
            return FileVisitResult.CONTINUE;
        }
    }
    
    @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String restoreFileName = restoreFilePattern.getFileName().toString();
        if (attrs.lastModifiedTime().toMillis() > (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(restorePeriodDays)) & file.getFileName().toString().contains(restoreFileName)) {
            Path pathToCopy = Paths.get(restoreFilePattern.toAbsolutePath().normalize().getParent() + "\\" + file.getFileName());
            boolean isCopy = FileSystemWorker.copyOrDelFile(file.toFile(), pathToCopy, false);
            restoredFiles.add(isCopy + " is copy " + pathToCopy);
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
    
    @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
    
    private String searchFiles() {
        
        String archivesFilePattern = checkRestorePattern().getParent().toString().toLowerCase().split("common_new")[1];
        if (archivesFilePattern.contains("\\Q.\\E")) {
            archivesFilePattern = "\\\\192.168.14.10\\IT-Backup\\Srv-Fs\\Archives" + archivesFilePattern;
        }
        else {
            archivesFilePattern = checkRestorePattern().toString().toLowerCase();
        }
        try {
            Files.walkFileTree(Paths.get(archivesFilePattern), this);
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        String fromArray = new TForms().fromArray(restoredFiles, true);
        return fromArray;
    }
    
    private Path checkRestorePattern() {
        if (!restoreFilePattern.toString().toLowerCase().contains("common_new")) {
            return Paths.get("\\\\srv-fs.eatmeat.ru\\common_new\\" + restoreFilePattern);
        }
        else {
            return restoreFilePattern;
        }
    }
}
