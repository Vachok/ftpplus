package ru.vachok.money.filesys;


import org.slf4j.Logger;
import ru.vachok.money.config.AppComponents;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/**
 <b>Работа с файлами</b>

 @since 31.12.2018 (22:21) */
public abstract class FileSysWorker extends SimpleFileVisitor<Path> {

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    public static List<String> readFileAsList(File toRead) {
        List<String> fileAsList = new ArrayList<>();
        try(InputStream inputStream = new FileInputStream(toRead);
            Scanner scanner = new Scanner(inputStream)){
            while(scanner.hasNext()){
                fileAsList.add(scanner.nextLine());
            }
        }
        catch(IOException e){
            LOGGER.warn(e.getMessage(), e);
        }
        return fileAsList;
    }
}