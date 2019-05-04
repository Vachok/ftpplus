// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.fileworks;


import ru.vachok.networker.AppComponents;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;


/**
 Class ru.vachok.networker.fileworks.BigFileCopy
 <p>
 
 @since 04.05.2019 (11:15) */
public class BigFileCopy extends FileSystemWorker {
    
    
    private Path pathToCopy;
    
    private File fileToBeCopied;
    
    public BigFileCopy(Path pathToCopy, File fileToBeCopied) {
        this.pathToCopy = pathToCopy;
        this.fileToBeCopied = fileToBeCopied;
    }
    
    @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }
    
    @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Properties p = AppComponents.getProps();
        if (file.toAbsolutePath().equals(fileToBeCopied.toPath().toAbsolutePath())) {
            RandomAccessFile randomAccessFile = new RandomAccessFile(fileToBeCopied, "r");
            randomAccessFile.seek(Long.parseLong(p.getProperty("filereadposition", "0")));
            throw new IllegalComponentStateException("04.05.2019 (11:20)");
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