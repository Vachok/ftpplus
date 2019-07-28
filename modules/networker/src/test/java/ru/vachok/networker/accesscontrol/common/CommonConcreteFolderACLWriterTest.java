package ru.vachok.networker.accesscontrol.common;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;


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
        new CommonConcreteFolderACLWriter(currentPath).run();
        File ownerUsers = new File(currentPath.toAbsolutePath().normalize().toString() + "\\owner_users.txt");
        Assert.assertTrue(ownerUsers.exists());
        String readFileOwnerUsers = FileSystemWorker.readFile(ownerUsers.getAbsolutePath());
        Assert.assertTrue(readFileOwnerUsers.contains(LocalDate.now().toString()));
    }
    
    @Test
    public void testToString1() {
        Assert.assertTrue(new CommonConcreteFolderACLWriter(currentPath).toString().contains("currentPath=\\\\srv-fs\\it$$\\ХЛАМ"));
    }
}