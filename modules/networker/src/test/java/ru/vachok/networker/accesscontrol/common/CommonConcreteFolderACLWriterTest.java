package ru.vachok.networker.accesscontrol.common;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 @see CommonConcreteFolderACLWriter
 @since 22.07.2019 (11:20) */
public class CommonConcreteFolderACLWriterTest {
    
    
    private final Path currentPath = Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\");
    
    @Test
    public void testRun() {
        CommonConcreteFolderACLWriter concreteFolderACLWriter = new CommonConcreteFolderACLWriter(currentPath);
        concreteFolderACLWriter.run();
        File fileOwner = new File(currentPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + ConstantsFor.FILENAME_OWNER);
        Assert.assertTrue(fileOwner.exists());
        String readFile = FileSystemWorker.readFile(fileOwner.getAbsolutePath());
        Assert.assertTrue(readFile.contains("BUILTIN"));
        
    }
    
    @Test
    public void testWriteACLs() {
    }
    
    @Test
    public void testToString1() {
    }
}