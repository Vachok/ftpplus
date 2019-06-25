// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.fileworks;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.IllegalInvokeEx;

import java.nio.file.Path;
import java.nio.file.Paths;


/**
 @see FileSystemWorker
 @since 23.06.2019 (9:44) */
public class FileSystemWorkerTest {
    
    
    @Test
    public void testWriteFile() {
        throw new IllegalInvokeEx("23.06.2019 (4:02)");
    }
    
    @Test
    public void testDelTemp() {
        throw new IllegalInvokeEx("23.06.2019 (4:02)");
    }
    
    @Test
    public void testCopyOrDelFile() {
        throw new IllegalInvokeEx("23.06.2019 (4:02)");
    }
    
    @Test
    public void testReadFile() {
        throw new IllegalInvokeEx("23.06.2019 (4:02)");
    }
    
    @Test
    public void testError() {
        throw new IllegalInvokeEx("23.06.2019 (4:02)");
    }
    
    @Test
    public void testAppendObjToFile() {
        throw new IllegalInvokeEx("23.06.2019 (4:02)");
    }
    
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