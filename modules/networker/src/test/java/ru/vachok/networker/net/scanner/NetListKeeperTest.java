package ru.vachok.networker.net.scanner;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;


/**
 @see NetListKeeper
 @since 12.07.2019 (16:27) */
public class NetListKeeperTest {
    
    
    @Test
    public void testToString1() {
        Assert.assertTrue(NetListKeeper.getI().toString().contains("offLines="));
    }
    
    @Test
    public void testCheckSwitchesAvail() {
        NetListKeeper.getI().checkSwitchesAvail();
        File fileResults = new File("sw.list.log");
        Assert.assertTrue(fileResults.exists());
        checkFileContent(fileResults);
    }
    
    private void checkFileContent(File results) {
        String readFile = FileSystemWorker.readFile(results.getAbsolutePath());
        System.out.println(readFile);
    }
}