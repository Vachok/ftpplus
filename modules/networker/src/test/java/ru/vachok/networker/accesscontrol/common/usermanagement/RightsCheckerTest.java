// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common.usermanagement;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;


/**
 @see RightsChecker
 @since 22.06.2019 (11:13) */
public class RightsCheckerTest {
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private Path startPath = Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\");
    
    private Path logsCopyPath = Paths.get(Paths.get(".").toAbsolutePath().normalize() + System.getProperty(PropertiesNames.PRSYS_SEPARATOR) + "logscommon");
    
    private RightsChecker rightsChecker = new RightsChecker(logsCopyPath);
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @Test
    public void runChecker() {
        RightsChecker rightsChecker = new RightsChecker(startPath, logsCopyPath);
    
        rightsChecker.run();
        Assert.assertTrue(Objects.requireNonNull(logsCopyPath.toFile().listFiles()).length == 2);
    
        File copiedOwnFile = new File(logsCopyPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + FileNames.FILENAME_COMMONOWN);
        String readFile = FileSystemWorker.readFile(copiedOwnFile.getAbsolutePath());
    
        Assert.assertTrue(readFile.contains("owned by: BUILTIN"), readFile);
        copyExistsFiles();
    }
    
    private void copyExistsFiles() {
        createdLogDir();
        File copiedOwnFile = new File(logsCopyPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + FileNames.FILENAME_COMMONOWN);
        File copiedRghFile = new File(logsCopyPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + FileNames.FILENAME_COMMONRGH);
    
        Assert.assertFalse(new File(FileNames.FILENAME_COMMONOWN).exists());
        Assert.assertFalse(new File(FileNames.FILENAME_COMMONRGH).exists());
        Assert.assertTrue(copiedOwnFile.exists(), copiedOwnFile.getAbsolutePath());
        Assert.assertTrue(copiedRghFile.exists(), copiedRghFile.getAbsolutePath());
        
    }
    
    private void createdLogDir() {
        Assert.assertTrue(logsCopyPath.toFile().exists());
        Assert.assertTrue(logsCopyPath.toFile().isDirectory());
    }
}