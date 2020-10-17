// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.common;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.TimeoutException;


/**
 @see RightsChecker
 @since 22.06.2019 (11:13) */
public class RightsCheckerTest {


    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());

    private final MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());

    private final Path startPath = Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\");

    private final Path logsCopyPath = Paths.get(Paths.get(".").toAbsolutePath().normalize() + System.getProperty(PropertiesNames.SYS_SEPARATOR) + "logscommon");

    private final RightsChecker rightsChecker = new RightsChecker(logsCopyPath);

    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }

    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }

    @Test(enabled = false)
    public void runChecker() {
        if (UsefulUtilities.thisPC().contains("mint")) {
            throw new IllegalStateException(UsefulUtilities.getRunningInformation());
        }
        RightsChecker rightsChecker = new RightsChecker(startPath, logsCopyPath);

        try {
            AppConfigurationLocal.getInstance().executeBlock(rightsChecker, 60);
        }
        catch (TimeoutException e) {
            messageToUser.error(e.getMessage());
        }
        finally {
            Assert.assertEquals(Objects.requireNonNull(logsCopyPath.toFile().listFiles()).length, 2);
        }

        File copiedOwnFile = new File(logsCopyPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + FileNames.COMMON_OWN);
        String readFile = FileSystemWorker.readFile(copiedOwnFile.getAbsolutePath());

        Assert.assertTrue(readFile.contains("owned by: BUILTIN"), readFile);
        copyExistsFiles();
    }

    private void copyExistsFiles() {
        createdLogDir();
        File copiedOwnFile = new File(logsCopyPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + FileNames.COMMON_OWN);
        File copiedRghFile = new File(logsCopyPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + FileNames.COMMON_RGH);

        Assert.assertFalse(new File(FileNames.COMMON_OWN).exists());
        Assert.assertFalse(new File(FileNames.COMMON_RGH).exists());
        Assert.assertTrue(copiedOwnFile.exists(), copiedOwnFile.getAbsolutePath());
        Assert.assertTrue(copiedRghFile.exists(), copiedRghFile.getAbsolutePath());

    }

    private void createdLogDir() {
        Assert.assertTrue(logsCopyPath.toFile().exists());
        Assert.assertTrue(logsCopyPath.toFile().isDirectory());
    }
}