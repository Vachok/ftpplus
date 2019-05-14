// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.utils;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


/**
 @since 07.05.2019 (15:27) */
public abstract class FileSystemWorker {
    
    
    public static final String SYSTEM_DELIMITER = getDelimiter();
    
    private static MessageToUser messageToUser = new MessageCons(FileSystemWorker.class.getSimpleName());
    
    public static String error(String fileName, Exception e) {
        Path rootPath = Paths.get("err");
        try {
            Path directories = Files.createDirectories(rootPath);
            String toString = directories.toAbsolutePath().normalize().toString();
            directories = Path.of(toString + fileName);
            String fromArray = new TForms().fromArray(e);
            writeFile(directories, fromArray);
        }
        catch (IOException ex) {
            e.printStackTrace();
        }
        return rootPath.toAbsolutePath().toString();
    }
    
    private static String getDelimiter() {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return "\\";
        }
        else {
            return "/";
        }
    }
    
    private static String writeFile(Path pathToWrite, String stringToWrite) {
        try {
            pathToWrite = Files.write(pathToWrite, stringToWrite.getBytes(), StandardOpenOption.CREATE);
            return pathToWrite.normalize().toAbsolutePath().toString();
        }
        catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
