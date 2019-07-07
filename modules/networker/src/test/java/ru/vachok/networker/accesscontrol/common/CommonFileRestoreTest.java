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
import java.util.*;
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
        restoreCall.forEach(listElement->parseElement(listElement, filesSet));
        int countGrafik = 0;
        for (File file : Objects.requireNonNull(new File("\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\Общая\\").listFiles())) {
            if (file.getName().toLowerCase().contains("график")) {
                countGrafik++;
            }
        }
        Assert.assertTrue(countGrafik > 2);
        filesSet.forEach(this::removeTestsFiles);
    }
    
    @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        System.out.println("dir = " + dir);
        return FileVisitResult.CONTINUE;
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
    
    @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        String restoreFileName = restoreFilePattern.getFileName().toString();
        if (attrs.lastModifiedTime().toMillis() > (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(restorePeriodDays)) & file.getFileName().toString().contains(restoreFileName)) {
            fileIsMached(file);
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return FileVisitResult.CONTINUE;
    }
    
    @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        return FileVisitResult.CONTINUE;
    }
    
    private void removeTestsFiles(String fileAbsPath) {
        File fileFromTest = new File(fileAbsPath);
        try {
            fileAbsPath = fileAbsPath.split("is copy ")[1];
            boolean isDelete = Files.deleteIfExists(Paths.get(fileAbsPath));
            System.out.println(fileAbsPath + " is delete: " + isDelete);
        }
        catch (IndexOutOfBoundsException | IOException | InvalidPathException e) {
            System.err.println(e.getMessage() + " " + fileAbsPath + " will be delete on exit");
            boolean isDelete = fileFromTest.delete();
            if (!isDelete) {
                fileFromTest.deleteOnExit();
            }
        }
    }
    
    private void parseElement(Object listElement, Set<String> filesSet) {
        if (listElement instanceof Path) {
            elementIsPath(listElement, filesSet);
        }
        else {
            filesSet.add(listElement + "\n");
        }
    }
    
    private void elementIsPath(Object listElement, Set<String> filesSet) {
        filesSet.add("00 " + listElement + "\n");
        if (((Path) listElement).toFile().isDirectory()) {
            dirLevel++;
            showDir(Objects.requireNonNull(((Path) listElement).toFile().listFiles()), filesSet);
        }
    }
    
    private void fileIsMached(Path file) {
        Path pathToCopy = Paths.get(restoreFilePattern.toAbsolutePath().normalize().getParent() + "\\" + file.getFileName());
        boolean isCopy = FileSystemWorker.copyOrDelFile(file.toFile(), pathToCopy, false);
        String toShowAndAdd = isCopy + " is copy " + pathToCopy;
        restoredFiles.add(toShowAndAdd);
        System.out.println(toShowAndAdd);
    }
    
    /**
     @param listElement файлы, для просмотра
     @param filesSet строкорый {@link TreeSet}, с путём к файлу и уровнем.
     @see CommonFileRestore
     */
    private void showDir(File[] listElement, Set<String> filesSet) {
        for (File file : listElement) {
            if (file.isDirectory()) {
                dirLevel++;
                showDir(Objects.requireNonNull(file.listFiles()), filesSet);
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