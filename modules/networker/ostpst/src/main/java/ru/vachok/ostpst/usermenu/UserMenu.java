package ru.vachok.ostpst.usermenu;


import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;

import static java.math.RoundingMode.HALF_EVEN;


/**
 @since 14.05.2019 (9:20) */
@FunctionalInterface public interface UserMenu {
    
    
    void showMenu();
    
    default double checkFileSize(String fileName) {
        try {
            File file = new File(fileName.trim());
            long size = Files.size(file.toPath());
            BigDecimal valueOf = BigDecimal.valueOf((float) size).divide(BigDecimal.valueOf(1024 * 1024 * 1024)).setScale(2, HALF_EVEN);
            return valueOf.doubleValue();
        }
        catch (IOException | InvalidPathException e) {
            return 666;
        }
    }
    
}
