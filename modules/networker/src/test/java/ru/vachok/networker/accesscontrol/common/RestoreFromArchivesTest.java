// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 @deprecated
 @see RestoreFromArchives
 @since 22.06.2019 (22:32) */
@Deprecated
@SuppressWarnings("ALL") public class RestoreFromArchivesTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private String pathToRestoreAsStr = "14_ИТ_служба\\Общая";
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    
    /**
     @see RestoreFromArchives#toString()
     */
    @Test(enabled = false)
    public void testToString1() {
        try {
            RestoreFromArchives restoreFromArchives = new RestoreFromArchives(pathToRestoreAsStr, "150");
            String toString = restoreFromArchives.toString();
            Assert.assertTrue(toString.contains("firstLeverSTR='14_ИТ_служба'"), toString);
        }
        catch (InvocationTargetException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
    
    @Test(enabled = false)
    public void runRestoreFolder() {
        RestoreFromArchives restoreFromArchives = null;
        try {
            restoreFromArchives = new RestoreFromArchives(pathToRestoreAsStr, "200");
        }
        catch (InvocationTargetException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
        try {
            Files.walkFileTree(Paths.get(pathToRestoreAsStr), restoreFromArchives);
            Assert.assertTrue(restoreFromArchives.toString().contains("это пропащая папка:"));
            Assert.assertTrue(restoreFromArchives.toString().contains("это папка из архива:"));
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
    
    @Test(enabled = false)
    public void runRestoreFile() {
        RestoreFromArchives restoreFromArchives = null;
        try {
            restoreFromArchives = new RestoreFromArchives(Paths.get(pathToRestoreAsStr + "\\" + "График отпусков 2019г  IT.XLSX").toAbsolutePath().normalize().toString(), "360");
        }
        catch (InvocationTargetException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
        try {
            Files.walkFileTree(Paths.get(pathToRestoreAsStr), restoreFromArchives);
            System.out.println(restoreFromArchives.toString());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
    
    private void pathsWorking(String pathToRestoreAsStrFrom) {
        URI uriRestore = Paths.get(pathToRestoreAsStrFrom).toAbsolutePath().normalize().toUri();
        StringBuilder stringBuilder = new StringBuilder();
        Assert.assertNotNull(uriRestore);
        Assert.assertEquals(uriRestore.getHost(), "srv-fs.eatmeat.ru");
        Path uriPath = Paths.get(uriRestore).toAbsolutePath().normalize();
    
        Path rootPath = uriPath.getRoot();
        System.out.println("uriPath.getNameCount() = " + uriPath.getNameCount());
    }
    
    private void urlWorks(URI uriRestore) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            URL urlRestore = uriRestore.toURL();
            try (InputStream inputStream = urlRestore.openStream()) {
                byte[] bytes = new byte[ConstantsFor.KBYTE];
                int inRead;
                while ((inRead = inputStream.read(bytes)) > 0) {
                    stringBuilder.append(new String(bytes)).append(" readed: ").append(inRead);
                }
            }
            System.out.println(stringBuilder);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
        
    }
}
