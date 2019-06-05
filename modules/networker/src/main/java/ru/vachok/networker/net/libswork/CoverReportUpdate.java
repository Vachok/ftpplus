package ru.vachok.networker.net.libswork;


import org.apache.commons.net.ftp.FTPClient;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;


/**
 @since 05.06.2019 (15:55) */
public class CoverReportUpdate extends RegRuFTPLibsUploader {
    
    
    private static MessageToUser messageToUser = new MessageSwing();
    
    private FTPClient ftpClient;
    
    @Override public String uploadLibs() throws AccessDeniedException {
        File[] upFiles = getLibFiles();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(upFiles.length).append(" files to upload").append("\n");
        for (File file : upFiles) {
            stringBuilder.append(file.getName()).append("\n");
        }
        return stringBuilder.toString();
    }
    
    @Override public void run() {
        this.ftpClient = getFtpClient();
        try {
            System.out.println("uploadLibs() = " + uploadLibs());
        }
        catch (AccessDeniedException e) {
            messageToUser.error(getClass().getSimpleName(), "UPLOAD ERROR", e.getMessage());
        }
    }
    
    @Override File[] getLibFiles() {
        Path rootPath = Paths.get(".").toAbsolutePath().normalize();
        String fSep = System.getProperty("file.separator");
        
        String pathOfUploadDirectoryStr = rootPath + fSep + "src" + fSep + "main" + fSep + "resources" + fSep + "static" + fSep + "cover";
        
        File[] files = new File(pathOfUploadDirectoryStr).listFiles();
        
        try {
            Files.walkFileTree(Paths.get(pathOfUploadDirectoryStr), new CoverFileUploader());
        }
        catch (IOException e) {
            messageToUser.error(getClass().getSimpleName(), "LIB FILES ERROR", e.getMessage());
        }
        
        return files;
    }
    
    
    private class CoverFileUploader extends SimpleFileVisitor<Path> {
        
        
        @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }
        
        @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
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
