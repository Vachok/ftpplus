package ru.vachok.networker.fileworks;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.IllegalInvokeEx;

import java.nio.file.Paths;
import java.util.List;


/**
 @see CommonCleaner */
public class CommonCleanerTest {
    
    
    @Test
    public void testCall() {
        int stringsInFile = FileSystemWorker.countStringsInFile(Paths.get("C:\\Users\\ikudryashov\\IdeaProjects\\ftpplus\\modules\\networker\\inetstats\\potential.csv"));
        System.out.println("stringsInFile = " + stringsInFile);
        List<String> filesToBeDelete = FileSystemWorker.readFileToList("C:\\Users\\ikudryashov\\IdeaProjects\\ftpplus\\modules\\networker\\inetstats\\potential.csv");
        Assert.assertTrue(filesToBeDelete.size() > 10);
    }
    
    @Test
    public void testPreVisitDirectory() {
        throw new IllegalInvokeEx();
    }
    
    @Test
    public void testVisitFile() {
        throw new IllegalInvokeEx();
    }
    
    @Test
    public void testVisitFileFailed() {
        throw new IllegalInvokeEx();
    }
    
    @Test
    public void testPostVisitDirectory() {
        throw new IllegalInvokeEx();
    }
}