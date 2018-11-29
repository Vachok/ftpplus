package ru.vachok.money.filesys;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.money.services.TForms;

import java.util.concurrent.ConcurrentMap;


/**
 Сервис для {@link FilesCTRL}
 <p>

 @since 29.11.2018 (22:52) */
@Service
public class FilesSRV {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = FilesSRV.class.getSimpleName();

    private FilesCheckerCleaner filesCheckerCleaner;

    String getInfo() {
        ConcurrentMap<String, String> resMap = filesCheckerCleaner.getResMap();
        return new TForms().toStringFromArray(resMap);
    }

    /*Instances*/
    @Autowired
    public FilesSRV(FilesCheckerCleaner filesCheckerCleaner) {
        this.filesCheckerCleaner = filesCheckerCleaner;
    }
}