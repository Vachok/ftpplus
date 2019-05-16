// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.utils;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;


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
            directories = Paths.get(toString + fileName);
            String fromArray = new TForms().fromArray(e);
            writeStringToFile(directories.toString(), fromArray);
        }
        catch (IOException ex) {
            e.printStackTrace();
        }
        return rootPath.toAbsolutePath().toString();
    }
    
    public static String writeFile(String fileName, Stream<String> stream) {
        Path pathWritten = Paths.get(fileName);
        StringBuilder stringBuilder = new StringBuilder();
        try (OutputStream outputStream = new FileOutputStream(pathWritten.toAbsolutePath().toString())) {
            stream.forEach(x->{
                try {
                    outputStream.write(x.getBytes());
                }
                catch (IOException e) {
                    stringBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e));
                }
            });
            stringBuilder.append("Strings written to: ").append(pathWritten.toAbsolutePath().normalize());
        }
        catch (IOException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e));
        }
        return stringBuilder.toString();
    }
    
    private static String getDelimiter() {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return "\\";
        }
        else {
            return "/";
        }
    }
    
    public static String writeStringToFile(String fileName, String stringToWrite) {
        Path pathToWrite = Paths.get(fileName);
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
