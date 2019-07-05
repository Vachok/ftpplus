// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;


/**
 @see CommonFileRestore
 @since 05.07.2019 (10:16) */
public class CommonFileRestoreTest extends SimpleFileVisitor<Path> {
    
    
    private @NotNull Path restoreFilePattern;
    
    private int restorePeriodDays = 365;
    
    private List<String> restoredFiles = new ArrayList<>();
    
    private int dirLevel;
    
    @Test
    public void realCall() {
        CommonFileRestore commonFileRestore = new CommonFileRestore("\\\\srv-fs\\Common_new\\14_ИТ_служба\\Общая\\График отпусков 2019г  IT.XLSX", "200");
        List<?> restoreCall = commonFileRestore.call();
        Set<String> filesSet = new TreeSet<>();
        restoreCall.stream().forEach(listElement->parseElement(listElement, filesSet));
        System.out.println(new TForms().fromArray(filesSet, false));
    }
    
    @Test(enabled = false)
    public void call() {
        this.restoreFilePattern = Paths.get("\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\Общая\\График");
        this.restorePeriodDays = 10;
    
        String archivesFilePattern = restoreFilePattern.getParent().toString().toLowerCase().split(ConstantsFor.FOLDERNAME_COMMONNEW)[1];
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
    
    private void parseElement(Object listElement, Set<String> filesSet) {
        if (listElement instanceof String) {
            filesSet.add(listElement + "\n");
        }
        if (listElement instanceof Path) {
            filesSet.add("00 " + listElement + "\n");
            if (((Path) listElement).toFile().isDirectory()) {
                dirLevel++;
                showDir(((Path) listElement).toFile().listFiles(), filesSet);
            }
        }
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
    
    private void showDir(File[] listElement, Set<String> filesSet) {
        for (File file : listElement) {
            if (file.isDirectory()) {
                dirLevel++;
                showDir(file.listFiles(), filesSet);
            }
            else {
                filesSet.add(dirLevelGetVisual() + " " + (file.getAbsolutePath()) + ("\n"));
            }
        }
        dirLevel--;
    }
    
    private String dirLevelGetVisual() {
        StringBuilder stringBuilder = new StringBuilder();
        String format = String.format("%02d", dirLevel);
        stringBuilder.append(format);
        for (int i = 0; i < dirLevel; i++) {
            stringBuilder.append(">");
        }
        return stringBuilder.toString();
    }
}