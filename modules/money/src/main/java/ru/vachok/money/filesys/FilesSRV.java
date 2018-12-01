package ru.vachok.money.filesys;


import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import ru.vachok.money.config.AppComponents;
import ru.vachok.money.services.TForms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;


/**
 Сервис для {@link FilesCTRL}
 <p>

 @since 29.11.2018 (22:52) */
@Service
public class FilesSRV {

    /*Fields*/
    private static final Logger LOGGER = AppComponents.getLogger();

    private String userInput;

    public String getUserInput() {
        return userInput;
    }

    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }

    String getInfo() {
        FilesCheckerCleaner filesCheckerCleaner = new FilesCheckerCleaner();
        if(userInput.equalsIgnoreCase("all")){
            return scanAllDrives(filesCheckerCleaner);
        }
        Path path = Paths.get(this.userInput);
        try{
            Files.walkFileTree(path, filesCheckerCleaner);
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
        ConcurrentMap<String, String> resMap = filesCheckerCleaner.getResMap();
        return new TForms().toStringFromArray(resMap);
    }

    private String scanAllDrives(FilesCheckerCleaner filesCheckerCleaner) {
        List<String> resList = new ArrayList<>();
        Path[] paths = {Paths.get("c:\\"), Paths.get("d:\\"), Paths.get("e:\\"),
            Paths.get("f:\\"), Paths.get("g:\\")};
        for(Path path : paths){
            try{
                Files.walkFileTree(path, filesCheckerCleaner);
                ConcurrentMap<String, String> resMap = filesCheckerCleaner.getResMap();
                resList.add(new TForms().toStringFromArray(resMap, true));
            }
            catch(IOException e){
                LOGGER.error(e.getMessage(), e);
            }
        }
        return new TForms().toStringFromArray(resList);
    }
}