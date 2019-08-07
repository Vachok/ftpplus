// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.enums.UsefulUtilites;
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
import java.util.Map;


/**
 @see RightsParsing
 @since 04.07.2019 (9:47) */
public class RightsParsingTest {
    
    
    private long linesLimit = Long.MAX_VALUE;
    
    private RightsParsing rightsParsing = new RightsParsing(new ArrayList<>());
    
    private int countDirectories;
    
    @Test
    public void realRunTest() {
        RightsParsing rightsParsing = new RightsParsing("02", 1000);
        Map<Path, List<String>> pathListMap = rightsParsing.foundPatternMap();
        pathListMap.forEach((key, value)->{
            System.out.println(key);
            System.out.println(new TForms().fromArray(value, false));
        });
    }
    
    @Test
    public void readUserACL() {
        if (UsefulUtilites.thisPC().toLowerCase().contains("home")) {
            this.rightsParsing = new RightsParsing(new ArrayList<>(), 500);
        }
        else {
            this.rightsParsing = new RightsParsing(new ArrayList<>(), 5000);
        }
        List<String> searchPatterns = rightsParsing.getSearchPatterns();
        try {
            UserPrincipal principalNew = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\newuser.txt"));
            UserPrincipal principalOld = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\olduser.txt"));
            searchPatterns.add(principalOld.getName());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        searchPatterns.add("l.a.petrenko");
    
        Assert.assertNotNull(rightsParsing);
        FileSystemWorker.writeFile("folders", rightsParsing.foundPatternMap().keySet().stream());
        Path foldersFile = Paths.get("folders");
        Assert.assertTrue(foldersFile.toFile().exists());
        int inFile = FileSystemWorker.countStringsInFile(foldersFile);
        System.err.println(MessageFormat.format("{1} contains {0} strings", inFile, foldersFile.toAbsolutePath().normalize().toString()));
    }
    
    @Test
    public void testGetSearchPatterns() {
        List<String> srhPatterns = rightsParsing.getSearchPatterns();
        Assert.assertNotNull(srhPatterns);
    }
    
    @Test
    public void testFoundPatternMap() {
        this.rightsParsing = new RightsParsing(Collections.singletonList("Domain"));
        if (UsefulUtilites.thisPC().toLowerCase().contains("home")) {
            rightsParsing.setLinesLimit(500);
        }
        else {
            rightsParsing.setLinesLimit(5000);
        }
        Map<Path, List<String>> foundedFiles = rightsParsing.foundPatternMap();
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<Path, List<String>> entry : foundedFiles.entrySet()) {
            stringBuilder.append(entry.getKey()).append(":\n");
            stringBuilder.append(entry.getValue().toString().replaceAll("\\Q, \\E", "\n")).append("\n***\n");
        }
        System.out.println("foundedFiles = " + stringBuilder.toString());
    }
    
    @Test
    public void testTestToString() {
        String toStr = rightsParsing.toString();
        Assert.assertTrue(toStr.contains("RightsParsing{fileWithRights"), toStr);
    }
}