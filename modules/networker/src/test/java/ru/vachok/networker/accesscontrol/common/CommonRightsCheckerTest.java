// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 @see CommonRightsChecker
 @since 22.06.2019 (11:13) */
public class CommonRightsCheckerTest {
    
    
    /**
     @see CommonRightsChecker#run()
     */
    @Test
    public void testRun() {
        CommonRightsChecker rightsChecker = new CommonRightsChecker();
        File ownFile = new File(ConstantsFor.FILENAME_COMMONOWN);
        File rghtFile = new File(ConstantsFor.FILENAME_COMMONRGH);
        rightsChecker.run();
        Assert.assertFalse(ownFile.exists());
        Assert.assertFalse(rghtFile.exists());
        Path absPathToCopyCommonOwn = Paths.get(ownFile.toPath().toAbsolutePath().normalize().toString()
            .replace(ownFile.getName(), "lan" + System.getProperty(ConstantsFor.PRSYS_SEPARATOR) + ownFile.getName())).toAbsolutePath().normalize();
        Assert.assertTrue(absPathToCopyCommonOwn.toFile().exists());
    
        try {
            Files.walkFileTree(Paths.get("\\\\srv-fs.eatmeat.ru\\it$$\\_AdminTools\\ru_vachok_inet_inetor_main\\ru.vachok.inet.inetor.main\\app\\inetor_main\\"), rightsChecker);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage());
        }
        Assert.assertTrue(ownFile.exists());
        Assert.assertTrue(rghtFile.exists());
        Assert.assertTrue(FileSystemWorker.readFile(ownFile.getAbsolutePath()).contains("BUILTIN\\Администраторы"));
        
        Assert.assertTrue(FileSystemWorker.readFile(rghtFile.getAbsolutePath()).contains("app"));
        
        rightsChecker = new CommonRightsChecker(Paths.get("\\\\srv-fs.eatmeat.ru\\it$$\\ХЛАМ\\"));
        rightsChecker.run();
        Assert.assertTrue(FileSystemWorker.readFile(ownFile.getAbsolutePath()).contains("BUILTIN\\Администраторы"));
        Assert.assertTrue(FileSystemWorker.readFile(rghtFile.getAbsolutePath()).contains("ХЛАМ"));
        final long currentMillis = System.currentTimeMillis();
        FileSystemWorker.appendObjToFile(ownFile, currentMillis);
        FileSystemWorker.appendObjToFile(rghtFile, currentMillis);
        Assert.assertTrue(FileSystemWorker.readFile(ownFile.getAbsolutePath()).contains(String.valueOf(currentMillis)));
        Assert.assertTrue(FileSystemWorker.readFile(rghtFile.getAbsolutePath()).contains(String.valueOf(currentMillis)));
    }
}