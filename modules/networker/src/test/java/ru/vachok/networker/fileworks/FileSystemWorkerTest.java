// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.fileworks;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;


/**
 @see FileSystemWorker
 @since 23.06.2019 (9:44) */
@SuppressWarnings("ALL") public class FileSystemWorkerTest extends SimpleFileVisitor<Path> {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private String testRootPath = Paths.get(ConstantsFor.ROOT_PATH_WITH_SEPARATOR + "tmp").toAbsolutePath().normalize()
        .toString() + ConstantsFor.FILESYSTEM_SEPARATOR;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
        if (Paths.get(testRootPath).toAbsolutePath().normalize().toFile().exists() || Paths.get(testRootPath).toAbsolutePath().normalize().toFile().isDirectory()) {
            System.out.println("testRootPath = " + testRootPath);
        
        }
        else {
            try {
                Files.createDirectories(Paths.get(testRootPath));
            }
            catch (IOException e) {
                messageToUser.error(MessageFormat.format("FileSystemWorkerTest.setUp says: {0}. Parameters: \n[]: {1}", e.getMessage(), new TForms().fromArray(e)));
            }
        }
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    /**
     @see FileSystemWorker#countStringsInFile(Path)
     */
    @Test
    public void testCountStringsInFile() {
        String fileSeparator = System.getProperty("file.separator");
        Path fileToCount = Paths.get(".gitignore").toAbsolutePath().normalize();
        int stringsInMaxOnline = FileSystemWorker.countStringsInFile(fileToCount);
        Assert.assertTrue(stringsInMaxOnline > 50, stringsInMaxOnline + " strings in " + fileToCount.toFile().getName());
    }
    
    @Test
    public void testWriteFile() {
        throw new InvokeEmptyMethodException("15.07.2019 (20:43)");
    }
    
    @Test
    public void testDelTemp() {
        throw new InvokeEmptyMethodException("15.07.2019 (20:43)");
    }
    
    @Test
    public void testCopyOrDelFileWithPath() {
        Path pathForTestOriginal = Paths.get(testRootPath + "testCopyOrDelFileWithPath.test");
        Path pathForCopy = Paths.get(pathForTestOriginal.toString().replace("test", "log"));
        try (OutputStream outputStream = new FileOutputStream(pathForTestOriginal.normalize().toString());
             PrintStream printStream = new PrintStream(outputStream, true)) {
            printStream.println(new TForms().fromArray(Thread.currentThread().getStackTrace()));
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        FileSystemWorker.copyOrDelFileWithPath(pathForTestOriginal.toFile(), pathForCopy, false);
        Assert.assertTrue(pathForTestOriginal.toFile().exists());
        Assert.assertTrue(pathForCopy.toFile().exists());
        FileSystemWorker.copyOrDelFileWithPath(pathForTestOriginal.toFile(), pathForCopy, true);
        Assert.assertFalse(pathForTestOriginal.toFile().exists());
        Assert.assertTrue(pathForCopy.toFile().exists());
        Assert.assertTrue(pathForCopy.toFile().lastModified() > System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10));
    }
    
    @Test
    public void testError() {
        throw new InvokeEmptyMethodException("15.07.2019 (20:43)");
    }
    
    @Test
    public void testReadFile() {
        throw new InvokeEmptyMethodException("15.07.2019 (20:43)");
    }
    
    @Test
    public void testReadFileToQueue() {
        throw new InvokeEmptyMethodException("15.07.2019 (20:43)");
    }
    
    @Test
    public void testCopyOrDelFile() {
        throw new InvokeEmptyMethodException("15.07.2019 (20:43)");
    }
    
    @Test
    public void testReadFileToList() {
        throw new InvokeEmptyMethodException("15.07.2019 (20:43)");
    }
    
    @Test
    public void testReadFileToSet() {
        throw new InvokeEmptyMethodException("15.07.2019 (20:43)");
    }
    
    @Test
    public void testAppendObjectToFile() {
        throw new InvokeEmptyMethodException("15.07.2019 (20:43)");
    }
    
    @Test
    public void testReadFileAsStream() {
        throw new InvokeEmptyMethodException("15.07.2019 (20:43)");
    }
    
    @Test
    public void testPackFiles() {
        throw new InvokeEmptyMethodException("15.07.2019 (20:43)");
    }
}