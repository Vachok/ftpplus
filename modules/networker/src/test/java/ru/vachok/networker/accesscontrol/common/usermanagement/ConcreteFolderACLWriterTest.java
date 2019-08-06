package ru.vachok.networker.accesscontrol.common.usermanagement;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.List;


/**
 @see ConcreteFolderACLWriter
 @since 22.07.2019 (11:20) */
public class ConcreteFolderACLWriterTest {
    
    
    private final Path currentPath = Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\");
    
    @Test
    public void testRun() {
        ConcreteFolderACLWriter concreteFolderACLWriter = new ConcreteFolderACLWriter(currentPath);
        concreteFolderACLWriter.run();
        File fileOwner = new File(currentPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + FileNames.FILENAME_OWNER);
        Assert.assertTrue(fileOwner.exists());
        setACLToAdminsOnly(fileOwner.toPath());
        String readFile = FileSystemWorker.readFile(fileOwner.getAbsolutePath());
        Assert.assertTrue(readFile.contains("BUILTIN"), readFile);
    }
    
    @Test
    public void testWriteACLs() {
        new ConcreteFolderACLWriter(currentPath).run();
        File ownerUsers = new File(currentPath.toAbsolutePath().normalize().toString() + "\\owner_users.txt");
        Assert.assertTrue(ownerUsers.exists());
        setACLToAdminsOnly(ownerUsers.toPath());
        String readFileOwnerUsers = FileSystemWorker.readFile(ownerUsers.getAbsolutePath());
        System.out.println("readFileOwnerUsers = " + readFileOwnerUsers);
    }
    
    private static final void setACLToAdminsOnly(@NotNull Path pathToFile) {
        AclFileAttributeView attributeView = Files.getFileAttributeView(pathToFile, AclFileAttributeView.class);
        try {
            UserPrincipal userPrincipal = Files.getOwner(pathToFile.getRoot());
            Files.setOwner(pathToFile, userPrincipal);
            AclEntry newACL = UserACLManager.createNewACL(userPrincipal);
            List<AclEntry> aclEntries = Files.getFileAttributeView(pathToFile, AclFileAttributeView.class).getAcl();
            aclEntries.add(newACL);
            Files.getFileAttributeView(pathToFile, AclFileAttributeView.class).setAcl(aclEntries);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testToString1() {
        Assert.assertTrue(new ConcreteFolderACLWriter(currentPath).toString().contains("currentPath=\\\\srv-fs\\it$$\\ХЛАМ"));
    }
}