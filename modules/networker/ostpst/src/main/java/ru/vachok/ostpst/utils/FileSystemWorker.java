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
            throw new IllegalStateException("07.05.2019 (16:11)");
        }
        catch (IOException ex) {
            e.printStackTrace();
        }
        return rootPath.toAbsolutePath().toString();
    }
}
