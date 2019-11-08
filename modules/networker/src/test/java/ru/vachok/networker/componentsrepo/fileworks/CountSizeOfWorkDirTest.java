// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.fileworks;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.*;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.IOException;
import java.nio.file.*;
import java.text.MessageFormat;
import java.util.concurrent.*;

import static org.testng.Assert.assertNull;


/**
 @see CountSizeOfWorkDir
 @since 24.06.2019 (22:07) */
public class CountSizeOfWorkDirTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
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
     @see CountSizeOfWorkDir#call()
     */
    @Test
    public void testCall() {
        Future<String> countSizeOfWorkDir = AppComponents.threadConfig().getTaskExecutor().submit(new CountSizeOfWorkDir());
        try {
            String result = countSizeOfWorkDir.get(30, TimeUnit.SECONDS);
            Assert.assertTrue(result.contains(ConstantsFor.PREF_NODE_NAME));
            Assert.assertTrue(result.contains(ConstantsFor.GRADLE));
            Assert.assertTrue(result.contains("idea"));
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            e.printStackTrace();
            assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        finally {
            MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName()).warn("FINAL");
            Runtime.getRuntime().runFinalization();
        }
    
    }
    
    @Test
    public void countDiskSpace() {
        Path rootProgramPath = Paths.get(".").toAbsolutePath().normalize();
        try (FileSystem fileSystem = rootProgramPath.getFileSystem()) {
            for (FileStore fileStore : fileSystem.getFileStores()) {
                String spaces = MessageFormat.format("Store {0}. Usable = {1} Mb, unallocated = {2} Mb, total = {3} Mb",
                    fileStore.name(), fileStore.getUsableSpace() / ConstantsFor.MBYTE, fileStore.getUnallocatedSpace() / ConstantsFor.MBYTE, fileStore
                        .getTotalSpace() / ConstantsFor.MBYTE);
                MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName()).info(spaces);
                
            }
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
}