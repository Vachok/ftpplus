package ru.vachok.networker.net.libswork;


import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;


/**
 @since 05.06.2019 (15:55) */
public class CoverReportUpdate extends RegRuFTPLibsUploader implements Callable<String> {
    
    
    private static MessageToUser messageToUser = new MessageSwing();
    
    private Queue<Path> directoriesQueue = new LinkedList<>();
    
    private Queue<Path> filesList = new LinkedList<>();
    
    @Override public String call() throws Exception {
        setUploadDirectoryStr();
        return uploadLibs();
    }
    
    @Override public String uploadLibs() throws AccessDeniedException {
        StringBuilder stringBuilder = new StringBuilder();
    
        try {
            stringBuilder.append(Files.walkFileTree(Paths.get(getUploadDirectoryStr()), new CoverFileUploader()));
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
    
        System.out.println("directoriesQueue.size() = " + directoriesQueue.size());
    
        String upDirToServer = null;
        try {
            upDirToServer = uploadToServer(directoriesQueue, true);
        }
        catch (IOException e) {
            stringBuilder.append(e.getMessage());
        }
    
        System.out.println(upDirToServer);
    
        System.out.println("filesList.size() = " + filesList.size());
        String upFileToServer = uploadToServer(filesList);
        System.out.println(upFileToServer);
        
        return stringBuilder.toString();
    }
    
    @Override public void run() {
        try {
            System.out.println("uploadLibs() = " + uploadLibs());
        }
        catch (AccessDeniedException e) {
            messageToUser.error(getClass().getSimpleName(), "UPLOAD ERROR", e.getMessage());
        }
    }
    
    private class CoverFileUploader extends SimpleFileVisitor<Path> {
        
        
        @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (attrs.isDirectory()) {
                directoriesQueue.add(dir);
            }
            return FileVisitResult.CONTINUE;
        }
        
        @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (attrs.isRegularFile()) {
                filesList.add(file);
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
    
}
