package ru.vachok.networker.accesscontrol.common;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 @see CommonFileRestore
 @since 05.07.2019 (10:16) */
public class CommonFileRestoreTest extends SimpleFileVisitor<Path> {
    
    
    @NotNull
    private Path restoreFilePattern;
    
    private int restorePeriodDays = 365;
    
    private List<String> restoredFiles = new ArrayList<>();
    
    @Test
    public void call() {
        this.restoreFilePattern = Paths.get("\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\Общая\\График");
        this.restorePeriodDays = 10;
        
        String archivesFilePattern = restoreFilePattern.getParent().toString().toLowerCase().split("common_new")[1];
        archivesFilePattern = "\\\\192.168.14.10\\IT-Backup\\Srv-Fs\\Archives" + archivesFilePattern;
        try {
            Files.walkFileTree(Paths.get(archivesFilePattern), this);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        String fromArray = new TForms().fromArray(restoredFiles, true);
        Assert.assertFalse(fromArray.isEmpty());
        System.out.println(fromArray);
    }
    
    
    @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
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
}