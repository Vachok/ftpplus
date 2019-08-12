// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserPrincipal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 @see ACLParser
 @since 04.07.2019 (9:47) */
public class ACLParserTest {
    
    
    private long linesLimit = Long.MAX_VALUE;
    
    private InformationFactory rightsParsing = new ACLParser();
    
    private int countDirectories;
    
    @Test
    public void realRunTest() {
        List<String> searchPatterns = new ArrayList<>();
        searchPatterns.add("sapr");
        searchPatterns.add("\\\\srv-fs\\Common_new\\Проекты\\Проекты _УИ\\");
        rightsParsing.setInfo(searchPatterns);
        String parsingInfoAbout;
        if (UsefulUtilities.thisPC().toLowerCase().contains("do")) {
            parsingInfoAbout = rightsParsing.getInfoAbout("");
        }
        else {
            parsingInfoAbout = rightsParsing.getInfoAbout("3000");
        }
        System.out.println("parsingInfoAbout = " + parsingInfoAbout);
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
        rightsParsing.setInfo(Collections.singletonList("kudr"));
        FileSystemWorker.writeFile("folders", rightsParsing.getInfoAbout("5000"));
        Path foldersFile = Paths.get("folders");
        Assert.assertTrue(foldersFile.toFile().exists());
        int inFile = FileSystemWorker.countStringsInFile(foldersFile);
        System.err.println(MessageFormat.format("{1} contains {0} strings", inFile, foldersFile.toAbsolutePath().normalize().toString()));
    }
    
    @Test
    public void testTestToString() {
        String toStr = rightsParsing.toString();
        Assert.assertTrue(toStr.contains("RightsParsing{fileWithRights"), toStr);
    }
}