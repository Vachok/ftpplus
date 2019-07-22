package ru.vachok.networker.accesscontrol.common;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Paths;


/**
 @see CommonConcreteFolderACLWriter
 @since 22.07.2019 (11:20) */
public class CommonConcreteFolderACLWriterTest {
    
    
    @Test
    public void testSetCurrentPath() {
        CommonConcreteFolderACLWriter concreteFolderACLWriter = new CommonConcreteFolderACLWriter();
        concreteFolderACLWriter.setCurrentPath(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\"));
        Assert.assertTrue(concreteFolderACLWriter.toString().contains("it$$"));
    }
    
    @Test
    public void testRun() {
    }
    
    @Test
    public void testWriteACLs() {
    }
    
    @Test
    public void testToString1() {
    }
}