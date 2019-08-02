// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.*;


/**
 @see FileRestorer
 @since 05.07.2019 (10:16) */
public class FileRestorerTest extends SimpleFileVisitor<Path> {
    
    
    private @NotNull Path restoreFilePattern;
    
    private int restorePeriodDays = 365;
    
    private List<String> restoredFiles = new ArrayList<>();
    
    private int dirLevel;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    @Test
    public void realCall() {
        String restoreFilePattern = "\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\Общая\\owner_users.txt";
        FileRestorer fileRestorer = new FileRestorer(restoreFilePattern, "3640");
        Future<List<?>> submit = Executors.newSingleThreadExecutor().submit(fileRestorer);
        try {
            List<?> restoreCall = submit.get(20, TimeUnit.SECONDS);
            for (Object o : restoreCall) {
                Set<String> stringSet = parseElement(o);
                System.out.println(new TForms().fromArray(stringSet));
            }
        }
        catch (InterruptedException | TimeoutException | ExecutionException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        System.out.println("dir = " + dir);
        return FileVisitResult.CONTINUE;
    }
    
    @Test(enabled = false)
    public void call() {
        this.restoreFilePattern = Paths.get("\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\Общая\\График");
    
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
    
    private Set<String> parseElement(Object listElement) {
        Set<String> resSet = new TreeSet<>();
        if (listElement instanceof Path) {
            resSet.add(elementIsPath(listElement));
        }
        else {
            resSet.add(listElement + "\n");
        }
        return resSet;
    }
    
    private String elementIsPath(Object listElement) {
        StringBuilder sb = new StringBuilder();
        sb.append("00 " + listElement + "\n");
        
        if (((Path) listElement).toFile().isDirectory()) {
            dirLevel++;
            sb.append(showDir(((Path) listElement).toFile().listFiles()));
        }
        return sb.toString();
    }
    
    private void fileIsMached(Path file) {
        Path pathToCopy = Paths.get(restoreFilePattern.toAbsolutePath().normalize().getParent() + "\\" + file.getFileName());
        boolean isCopy = FileSystemWorker.copyOrDelFile(file.toFile(), pathToCopy, false);
        String toShowAndAdd = isCopy + " is copy " + pathToCopy;
        restoredFiles.add(toShowAndAdd);
        System.out.println(toShowAndAdd);
    }
    
    private String showDir(File[] listElement) {
        StringBuilder stringBuilder = new StringBuilder();
        for (File file : listElement) {
            if (file.isDirectory()) {
                dirLevel++;
                stringBuilder.append(showDir(Objects.requireNonNull(file.listFiles())));
            }
            else {
                stringBuilder.append(dirLevelGetVisual() + " " + (file.getAbsolutePath()) + ("\n"));
            }
        }
        dirLevel--;
        return stringBuilder.toString();
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