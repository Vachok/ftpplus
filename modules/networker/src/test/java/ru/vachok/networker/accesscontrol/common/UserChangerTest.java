package ru.vachok.networker.accesscontrol.common;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.text.MessageFormat;
import java.util.List;


/**
 @since 17.07.2019 (11:44) */
public class UserChangerTest extends SimpleFileVisitor<Path> {
    
    
    private byte[] oldUserSID;
    
    private UserPrincipal newUserSID;
    
    private int filesCounter = 0;
    
    private int foldersCounter = 0;
    
    public UserChangerTest(String oldUser, UserPrincipal newUser) {
        this.oldUserSID = oldUser.getBytes();
        this.newUserSID = newUser;
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        List<AclEntry> aclEntries = Files.getFileAttributeView(file, AclFileAttributeView.class).getAcl();
        
        AclEntry.Builder aclBuilder = AclEntry.newBuilder();
        aclBuilder.setPrincipal(newUserSID);
        aclBuilder.setType(AclEntryType.ALLOW);
        aclBuilder.setPermissions(AclEntryPermission.values());
        
        for (AclEntry aclEntry : aclEntries) {
            if (aclEntry.toString().contains(new String(oldUserSID))) {
                AclEntry buildedNewACL = aclBuilder.setPrincipal(newUserSID).build();
//                System.out.println(MessageFormat.format("File {0}. principal = {1}", file, new TForms().fromArray(aclEntries)));
                FileSystemWorker.appendObjectToFile(new File(this.getClass().getSimpleName() + ".res"), aclEntry);
            }
        }
        this.filesCounter++;
        System.out.println(MessageFormat.format("File: {1}, owner = {0}\n\nCurrent principals: {2}.\nNeeded principals: {3}",
            Files.getOwner(file), file, new TForms().fromArray(aclEntries), aclBuilder.build().toString()));
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        FileSystemWorker.appendObjectToFile(new File(this.getClass().getSimpleName() + ".err"), file + "\n" + new TForms().fromArray(exc));
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        this.foldersCounter++;
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserChanger{");
        sb.append(", oldUser='").append(new String(oldUserSID)).append('\'');
        sb.append(", newUser='").append(newUserSID.getName()).append('\'');
        sb.append(", Files visited: ").append(filesCounter).append(". Folders visited: ").append(foldersCounter).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    protected void adSnapToUserRelolver() {
        try (InputStream inputStream = new FileInputStream("\\\\srv-fs\\Common_new\\14_ИТ_служба\\Внутренняя\\srv-ad.dat")) {
            int available = inputStream.available();
            byte[] inBytes = new byte[available];
            while (inputStream.available() > 0) {
                inputStream.read(inBytes, 0, available);
            }
            System.out.println("inBytes = " + inBytes.length);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    public static class TestLocal {
        
        
        private String iGarnaginaSID = "S-1-5-21-3970069352-2416023058-3822801030-7283";
        
        private String tBabichevaSID = "S-1-5-21-3970069352-2416023058-3822801030-4235";
        
        private UserPrincipal tBabicheva;
        
        private UserChangerTest userChangerTest = new UserChangerTest(iGarnaginaSID, tBabicheva);
        
        @Test
        public void userChangerTest() {
            Path startPath = Paths.get("\\\\srv-fs\\Common_new\\06_Маркетинг\\Внутренняя\\Marketing (MG)\\Sales\\");
            
            try {
                this.tBabicheva = Files.getOwner(Paths.get("\\\\srv-fs\\Common_new\\06_Маркетинг\\Внутренняя\\folder_acl.txt"));
                Assert.assertTrue(tBabicheva.toString().contains(tBabichevaSID));
            }
            catch (IOException e) {
                Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            }
            
            UserChangerTest userChangerTest = new UserChangerTest(iGarnaginaSID, tBabicheva);
            Assert.assertTrue(userChangerTest.toString().contains(iGarnaginaSID));
            Assert.assertTrue(userChangerTest.toString().contains(tBabichevaSID));
            try {
                Files.walkFileTree(startPath, userChangerTest);
            }
            catch (IOException e) {
                Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            }
        }
        
        @Test(enabled = false)
        public void testAdSnapToUserRelolver() {
            userChangerTest.adSnapToUserRelolver();
        }
    }
}
