// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
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
        if (ConstantsFor.thisPC().toLowerCase().contains("home")) {
            this.rightsParsing = new RightsParsing(new ArrayList<>(), 500);
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
        int inFile = FileSystemWorker.countStringsInFile(Paths.get("folders"));
        System.out.println("inFile = " + inFile);
        Assert.assertTrue(inFile > 10);
    }
}