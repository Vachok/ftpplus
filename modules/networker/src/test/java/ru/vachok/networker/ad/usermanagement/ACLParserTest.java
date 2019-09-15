// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.usermanagement;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserPrincipal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 @see ACLParser
 @since 04.07.2019 (9:47) */
public class ACLParserTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(ACLParserTest.class.getSimpleName(), System.nanoTime());
    
    private long linesLimit = Integer.MAX_VALUE;
    
    private UserACLManager rightsParsing;
    
    private int countDirectories;
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 2));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
        this.rightsParsing = new ACLParser();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void realRunTest() {
        List<String> searchPatterns = new ArrayList<>();
        searchPatterns.add("kudr");
        searchPatterns.add("\\\\srv-fs.eatmeat.ru\\common_new\\12_СК\\Общая\\TQM");
        rightsParsing.setClassOption(searchPatterns);
        if (!UsefulUtilities.thisPC().toLowerCase().contains("eatmeat")) {
            rightsParsing.setClassOption(150);
        }
        String parsingInfoAbout = rightsParsing.getResult();
        Assert.assertTrue(parsingInfoAbout.contains("ACLParser.txt"), parsingInfoAbout);
        File resultsFile = new File(ACLParser.class.getSimpleName() + ".txt");
        Assert.assertNotNull(resultsFile);
        Assert.assertTrue(resultsFile.isFile());
        Assert.assertTrue(resultsFile.lastModified() > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(100)));
    }
    
    @Test
    public void readUserACL() {
        try {
            UserPrincipal principalNew = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\newuser.txt"));
            UserPrincipal principalOld = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\olduser.txt"));
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        Assert.assertNotNull(rightsParsing);
        rightsParsing.setClassOption(Collections.singletonList("kudr"));
        rightsParsing.setClassOption(1500);
        FileSystemWorker.writeFile("folders", rightsParsing.getResult());
        Path foldersFile = Paths.get("folders");
        Assert.assertTrue(foldersFile.toFile().exists());
        int inFile = FileSystemWorker.countStringsInFile(foldersFile);
        System.err.println(MessageFormat.format("{1} contains {0} strings", inFile, foldersFile.toAbsolutePath().normalize().toString()));
    }
    
    @Test
    public void testTestToString() {
        String toStr = rightsParsing.toString();
        Assert.assertTrue(toStr.contains("ACLParser["), toStr);
    }
    
    @Test
    public void testReadAllACLWithSearchPatternFromDB() {
        ACLParser aclParser = new ACLParser();
        aclParser.setLinesLimit(100);
        boolean fromDB = aclParser.readAllACLWithSearchPatternFromDB();
        Assert.assertTrue(fromDB);
    }
}