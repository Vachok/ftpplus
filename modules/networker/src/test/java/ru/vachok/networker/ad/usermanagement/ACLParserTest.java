// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.usermanagement;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 @see ACLParser
 @since 04.07.2019 (9:47) */
public class ACLParserTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(ACLParserTest.class.getSimpleName(), System.nanoTime());

    private final long linesLimit = Integer.MAX_VALUE;

    private UserACLManager rightsParsing;

    private int countDirectories;

    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 4));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }

    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }

    @BeforeMethod
    public void initRightsParsing() {
        this.rightsParsing = new ACLParser(Paths.get("\\\\srv-fs.eatmeat.ru\\Common_new\\"));
    }

    @Test
    public void realRunTest() {
        List<String> searchPatterns = new ArrayList<>();
        searchPatterns.add("kudr");
        searchPatterns.add("\\\\srv-fs.eatmeat.ru\\common_new\\12_СК\\Общая\\TQM");
        rightsParsing.setClassOption(searchPatterns);
        Future<String> parsingFuture = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(()->rightsParsing.getResult());
        String parsingInfoAbout;
        try {
            parsingInfoAbout = parsingFuture.get(60, TimeUnit.SECONDS);
            Assert.assertTrue(parsingInfoAbout.contains("srv-fs"), parsingInfoAbout);
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }

    @Test
    public void readUserACL() {
        try {
            UserPrincipal principalNew = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\newuser.txt"));
            UserPrincipal principalOld = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\olduser.txt"));
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        Assert.assertNotNull(rightsParsing);
        rightsParsing.setClassOption(Collections.singletonList("kudr"));
        rightsParsing.setClassOption(150);
        Future<String> submit = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor()
            .submit(()->FileSystemWorker.writeFile("folders", rightsParsing.getResult()));
        try {
            submit.get(60, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        Path foldersFile = Paths.get("folders");
        Assert.assertTrue(foldersFile.toFile().exists());
        int inFile = FileSystemWorker.countStringsInFile(foldersFile);
    }

    @Test
    public void testTestToString() {
        String toStr = rightsParsing.toString();
        Assert.assertTrue(toStr.contains("ACLParser{"), toStr);
    }
}