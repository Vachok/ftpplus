// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.common;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 @see ConcreteFolderACLWriter
 @since 22.07.2019 (11:20) */
public class ConcreteFolderACLWriterTest {
    
    
    private final Path currentPath = Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\");
    
    @Test
    public void testRun() {
        File fileOwner = new File(currentPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + FileNames.FILENAME_OWNER);
        fileOwner.delete();
        ConcreteFolderACLWriter concreteFolderACLWriter = new ConcreteFolderACLWriter(currentPath);
        concreteFolderACLWriter.run();
        Assert.assertTrue(fileOwner.exists());
        String readFile = FileSystemWorker.readFile(fileOwner.getAbsolutePath());
        Assert.assertTrue(readFile.contains("BUILTIN"), readFile);
    }
    
    @Test
    public void testWriteACLs() {
        new ConcreteFolderACLWriter(currentPath).run();
        File ownerUsers = new File(currentPath.toAbsolutePath().normalize().toString() + "\\owner_users.txt");
        Assert.assertTrue(ownerUsers.exists());
        String readFileOwnerUsers = FileSystemWorker.readFile(ownerUsers.getAbsolutePath());
        System.out.println("readFileOwnerUsers = " + readFileOwnerUsers);
    }
    
    @Test
    public void testToString1() {
        Assert.assertTrue(new ConcreteFolderACLWriter(currentPath).toString().contains("currentPath=\\\\srv-fs\\it$$\\ХЛАМ"));
    }
}