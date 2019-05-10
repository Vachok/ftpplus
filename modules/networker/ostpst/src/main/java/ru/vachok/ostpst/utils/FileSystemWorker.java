// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.utils;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 @since 07.05.2019 (15:27) */
public abstract class FileSystemWorker {
    
    
    public static String error(String fileName, Exception e) {
        Path rootPath = Paths.get("err");
        try {
            Files.createDirectories(rootPath);
        }
        catch (IOException ex) {
            e.printStackTrace();
        }
        return rootPath.toAbsolutePath().toString();
    }
}
