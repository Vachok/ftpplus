// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.fileworks;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;


/**
 @see FileSystemWorker
 @since 23.06.2019 (9:44) */
public class FileSystemWorkerTest {
    
    /**
     @see FileSystemWorker#countStringsInFile(Path)
     */
    @Test
    public void testCountStringsInFile() {
        String fileSeparator = System.getProperty("file.separator");
        int stringsInMaxOnline = FileSystemWorker.countStringsInFile(Paths.get("." + fileSeparator + "lan" + fileSeparator + "max.online").normalize());
        Assert.assertTrue(stringsInMaxOnline > 50, stringsInMaxOnline + " strings in max.online");
    }
}