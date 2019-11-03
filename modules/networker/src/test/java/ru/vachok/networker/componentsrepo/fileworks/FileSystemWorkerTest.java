// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.fileworks;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.fsworks.UpakFiles;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 @see FileSystemWorker
 @since 23.06.2019 (9:44) */
@SuppressWarnings("ALL")
public class FileSystemWorkerTest extends SimpleFileVisitor<Path> {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, FileSystemWorkerTest.class.getSimpleName());
    
    private String testRootPath = Paths.get(ConstantsFor.ROOT_PATH_WITH_SEPARATOR + "tmp").toAbsolutePath().normalize()
            .toString() + ConstantsFor.FILESYSTEM_SEPARATOR;
    
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
        String fileSeparator = System.getProperty(PropertiesNames.SYS_SEPARATOR);
        Path fileToCount = Paths.get(ConstantsFor.ROOT_PATH_WITH_SEPARATOR + "inetstats\\ok\\10.200.213.98-11.txt").toAbsolutePath().normalize();
        final long startNano = System.nanoTime();
        int stringsInCommonOwn = FileSystemWorker.countStringsInFile(fileToCount);
        final long endNano = System.nanoTime();
        Assert.assertTrue(stringsInCommonOwn > 11, MessageFormat.format("{0} strings in {1}", stringsInCommonOwn, fileToCount.toFile().getName()));
        long nanoElapsed = endNano - startNano;
        Assert.assertTrue((nanoElapsed < 26_927_200_499L), String.valueOf(nanoElapsed));
        try {
            testConfigureThreadsLogMaker.getPrintStream().println(MessageFormat.format("Standart = {0} nanos", nanoElapsed));
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        catch (RuntimeException ignore) {
            //07.10.2019 (16:36)
        }
        System.out.println("stringsInCommonOwn = " + stringsInCommonOwn);
    }
    
    @Test(enabled = false)
    public void countStringsInFileAsStream() {
        Path fileToCount = Paths.get(ConstantsFor.ROOT_PATH_WITH_SEPARATOR + "tmp\\common.own");
        final long startNano = System.nanoTime();
        try (InputStream inputStream = new FileInputStream(fileToCount.toAbsolutePath().normalize().toString());
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            long count = bufferedReader.lines().count();
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("FileSystemWorker.countStringsInFileAsStream: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        final long endNano = System.nanoTime();
        
        long nanoElapsed = endNano - startNano;
        System.out.println(MessageFormat.format("AsStream = {0} nanos", nanoElapsed));
        try {
            testConfigureThreadsLogMaker.getPrintStream().println(MessageFormat.format("Standart = {0} nanos", nanoElapsed));
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testWriteFile() {
        FileSystemWorker.writeFile(getClass().getSimpleName() + ".test", "test");
        File testFile = new File(getClass().getSimpleName() + ".test");
        Assert.assertTrue(testFile.lastModified() > (System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1)), testFile + " : " + new Date(testFile.lastModified())
                .toString());
    }
    
    @Test
    public void testDelTemp() {
        FileSystemWorker.delTemp();
        File file = new File("DeleterTemp.txt");
        Assert.assertTrue(file.lastModified() > (System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1)), file.getAbsolutePath());
    }
    
    @Test(enabled = false)
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
        messageToUser.info(FileSystemWorker.copyOrDelFileWithPath(pathForTestOriginal.toFile(), pathForCopy, false));
        Assert.assertTrue(pathForTestOriginal.toFile().exists());
        Assert.assertTrue(pathForCopy.toFile().exists());
        messageToUser.info(FileSystemWorker.copyOrDelFileWithPath(pathForTestOriginal.toFile(), pathForCopy, true));
        Assert.assertFalse(pathForTestOriginal.toFile().exists());
        Assert.assertTrue(pathForCopy.toFile().exists());
        Assert.assertTrue(pathForCopy.toFile().lastModified() > System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10));
    }
    
    @Test
    public void testError() {
        Path rootPath = Paths.get(".");
        String errorMsg;
        try {
            throw new TODOException("22.07.2019 (16:28)");
        }
        catch (Exception e) {
            errorMsg = FileSystemWorker.error(getClass().getSimpleName() + ".testError", e);
        }
        Assert.assertTrue(errorMsg.contains("\\err\\FileSystemWorkerTest."), errorMsg);
        Assert.assertTrue(new File(errorMsg).exists());
    }
    
    @Test
    public void testReadFile() {
        String readFile = FileSystemWorker.readFile(FileNames.BUILD_GRADLE);
        Assert.assertTrue(readFile.contains("rsion"));
    }
    
    @Test
    public void testReadFileToQueue() {
        Queue<String> stringQueue = FileSystemWorker.readFileToQueue(Paths.get(FileNames.BUILD_GRADLE));
        Assert.assertFalse(stringQueue.isEmpty());
    }
    
    @Test
    public void testCopyOrDelFile() {
        File origin = new File(FileNames.BUILD_GRADLE);
        Path toCopy = Paths.get("build.gradle.bak");
        FileSystemWorker.copyOrDelFile(origin, toCopy, false);
        Assert.assertTrue(origin.exists());
        Assert.assertTrue(toCopy.toFile().exists());
    
        Path buildBak = Paths.get("build.bak");
        FileSystemWorker.copyOrDelFile(toCopy.toFile(), buildBak, true);
        Assert.assertFalse(toCopy.toFile().exists());
        Assert.assertTrue(buildBak.toFile().exists());
        Assert.assertTrue(buildBak.toFile().lastModified() > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10)));
    }
    
    @Test
    public void testReadFileToList() {
        List<String> stringSet = FileSystemWorker.readFileToList(String.valueOf(Paths.get(FileNames.BUILD_GRADLE).toAbsolutePath().normalize()));
        Assert.assertTrue(stringSet.size() > 0);
    }
    
    @Test
    public void testReadFileToSet() {
        Set<String> stringSet = FileSystemWorker.readFileToSet(Paths.get(FileNames.BUILD_GRADLE).toAbsolutePath().normalize());
        Assert.assertTrue(stringSet.size() > 0);
    }
    
    @Test
    public void testAppendObjectToFile() {
        File forAppend = new File(this.getClass().getSimpleName());
        FileSystemWorker.appendObjectToFile(forAppend, "test");
        FileSystemWorker.appendObjectToFile(forAppend, "test1");
        Assert.assertTrue(FileSystemWorker.readFile(forAppend.getAbsolutePath()).contains("test1"));
        Assert.assertTrue(forAppend.delete());
    }
    
    @Test
    public void testPackFiles() {
        UpakFiles upakFiles = new UpakFiles();
        List<File> filesToUpak = new ArrayList<>();
        filesToUpak.add(new File(FileNames.BUILD_GRADLE));
        filesToUpak.add(new File("settings.gradle"));
        upakFiles.createZip(filesToUpak, "gradle.zip", 5);
        File gradleZip = new File("gradle.zip");
        Assert.assertTrue(gradleZip.exists());
        long bytesOrig = 0;
        for (File file : filesToUpak) {
            bytesOrig = bytesOrig + file.length();
        }
        Assert.assertTrue(bytesOrig > gradleZip.length());
    }
    
    @Test
    public void testReadFileEncodedToQueue() {
        Queue<String> stringQueue = FileSystemWorker.readFileEncodedToQueue(Paths.get("\\\\srv-fs\\Common_new\\14_ИТ_служба\\Общая\\Меркурий.txt"), "Windows-1251");
        String queueArr = new TForms().fromArray(stringQueue);
        Assert.assertTrue(queueArr.contains("хозяйствующих_субъектов"), queueArr);
    
        Queue<String> stringQueueBadEncoding = FileSystemWorker.readFileEncodedToQueue(Paths.get("\\\\srv-fs\\Common_new\\14_ИТ_служба\\Общая\\Меркурий.txt"), "utf8");
        queueArr = new TForms().fromArray(stringQueueBadEncoding);
        Assert.assertFalse(queueArr.contains("хозяйствующих_субъектов"), queueArr);
    }
}