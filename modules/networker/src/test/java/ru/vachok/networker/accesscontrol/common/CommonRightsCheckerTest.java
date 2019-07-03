// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.IllegalInvokeEx;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserPrincipal;
import java.util.concurrent.TimeUnit;


/**
 @see CommonRightsChecker
 @since 22.06.2019 (11:13) */
public class CommonRightsCheckerTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.afterClass();
    }

    
    
    /**
     @see CommonRightsChecker#run()
     */
    @Test
    public void testRun() {
        CommonRightsChecker rightsChecker = new CommonRightsChecker(Paths
            .get("\\\\srv-fs.eatmeat.ru\\it$$\\_AdminTools\\ru_vachok_inet_inetor_main\\ru.vachok.inet.inetor.main\\app\\inetor_main\\"), Paths
            .get("\\\\srv-fs.eatmeat.ru\\it$$\\!!!Docs!!!\\"));
        File rghCopyFile = new File("\\\\srv-fs.eatmeat.ru\\it$$\\!!!Docs!!!\\common.rgh");
        File ownCopyFile = new File("\\\\srv-fs.eatmeat.ru\\it$$\\!!!Docs!!!\\common.own");
        File ownFile = new File(ConstantsFor.FILENAME_COMMONOWN);
        File rghtFile = new File(ConstantsFor.FILENAME_COMMONRGH);
        final long currentMillis = System.currentTimeMillis();
        
        try {
            Files.deleteIfExists(rghCopyFile.toPath().toAbsolutePath().normalize());
            Files.deleteIfExists(ownCopyFile.toPath().toAbsolutePath().normalize());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    
        rightsChecker.run();
    
        Assert.assertTrue(rghCopyFile.exists());
        Assert.assertTrue(rghCopyFile.lastModified() > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2));
        Assert.assertTrue(ownCopyFile.exists());
        Assert.assertTrue(ownCopyFile.lastModified() > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2));
    
        Assert.assertTrue(FileSystemWorker.readFile(ownCopyFile.getAbsolutePath()).contains("BUILTIN\\Администраторы"));
        Assert.assertTrue(FileSystemWorker.readFile(rghCopyFile.getAbsolutePath()).contains("app"));
    
        rightsChecker = new CommonRightsChecker(Paths.get("\\\\srv-fs.eatmeat.ru\\it$$\\ХЛАМ\\"), Paths.get("\\\\srv-fs.eatmeat.ru\\it$$\\!!!Docs!!!\\"));
        try {
            rightsChecker.run();
        }
        catch (IllegalInvokeEx e) {
            Assert.assertNotNull(e);
        }
        Assert.assertTrue(FileSystemWorker.readFile(ownCopyFile.getAbsolutePath()).contains("BUILTIN\\Администраторы"));
        Assert.assertTrue(FileSystemWorker.readFile(rghCopyFile.getAbsolutePath()).contains("READ_DATA/WRITE_DATA/APPEND_DATA"));
        FileSystemWorker.appendObjectToFile(ownCopyFile, currentMillis);
        FileSystemWorker.appendObjectToFile(rghCopyFile, currentMillis);
        Assert.assertTrue(FileSystemWorker.readFile(ownCopyFile.getAbsolutePath()).contains(String.valueOf(currentMillis)));
        Assert.assertTrue(FileSystemWorker.readFile(rghCopyFile.getAbsolutePath()).contains(String.valueOf(currentMillis)));
    }
    
    @Test
    public void testPrincipal() {
        try {
            Path file = Paths
                .get("\\\\srv-fs.eatmeat.ru\\common_new\\07_УЦП\\Внутренняя\\003.Служба складской логистики\\004.Склад готовой продукции\\Архив\\Склад 2\\Москвы карта\\Data\\Msk\\21\\12.gif");
            UserPrincipal userPrincipal = Files.getOwner(file);
            if (userPrincipal.toString().contains("Unknown")) {
                Files.setOwner(file, Files.getOwner(file.getRoot()));
            }
            Assert.assertFalse(userPrincipal.toString().contains("Unknown"), userPrincipal.toString());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
}