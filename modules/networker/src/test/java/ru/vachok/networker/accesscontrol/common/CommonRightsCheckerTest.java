// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;


/**
 @see CommonRightsChecker
 @since 22.06.2019 (11:13) */
public class CommonRightsCheckerTest {
    
    
    /**
     @see CommonRightsChecker#run()
     */
    @Test
    public void testRun() {
        CommonRightsChecker rightsChecker = new CommonRightsChecker(Paths
            .get("\\\\srv-fs.eatmeat.ru\\it$$\\_AdminTools\\ru_vachok_inet_inetor_main\\ru.vachok.inet.inetor.main\\app\\inetor_main\\"), null);
        File ownFile = new File(ConstantsFor.FILENAME_COMMONOWN);
        File rghtFile = new File(ConstantsFor.FILENAME_COMMONRGH);
        rightsChecker.run();
        File rghCopyFile = new File("\\\\srv-fs.eatmeat.ru\\it$$\\common.rgh");
        File ownCopyFile = new File("\\\\srv-fs.eatmeat.ru\\it$$\\common.own");
    
        Assert.assertTrue(rghCopyFile.exists());
        Assert.assertTrue(rghCopyFile.lastModified() > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2));
        Assert.assertTrue(ownCopyFile.exists());
        Assert.assertTrue(ownCopyFile.lastModified() > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2));
    
        Assert.assertTrue(FileSystemWorker.readFile(ownCopyFile.getAbsolutePath()).contains("BUILTIN\\Администраторы"));
        Assert.assertTrue(FileSystemWorker.readFile(rghCopyFile.getAbsolutePath()).contains("app"));
    
        rightsChecker = new CommonRightsChecker(Paths.get("\\\\srv-fs.eatmeat.ru\\it$$\\ХЛАМ\\"), null);
        rightsChecker.run();
        Assert.assertTrue(FileSystemWorker.readFile(ownCopyFile.getAbsolutePath()).contains("BUILTIN\\Администраторы"));
        Assert.assertTrue(FileSystemWorker.readFile(rghCopyFile.getAbsolutePath()).contains("ХЛАМ"));
        final long currentMillis = System.currentTimeMillis();
        FileSystemWorker.appendObjToFile(ownCopyFile, currentMillis);
        FileSystemWorker.appendObjToFile(rghCopyFile, currentMillis);
        Assert.assertTrue(FileSystemWorker.readFile(ownCopyFile.getAbsolutePath()).contains(String.valueOf(currentMillis)));
        Assert.assertTrue(FileSystemWorker.readFile(rghCopyFile.getAbsolutePath()).contains(String.valueOf(currentMillis)));
    }
}