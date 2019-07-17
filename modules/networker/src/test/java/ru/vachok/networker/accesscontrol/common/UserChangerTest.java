// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 @since 17.07.2019 (11:44) */
public class UserChangerTest extends SimpleFileVisitor<Path> {
    
    
    private UserPrincipal oldUser;
    
    private UserPrincipal newUser;
    
    private int filesCounter = 0;
    
    private int foldersCounter = 0;
    
    private Queue<String> filesACLs = new LinkedBlockingQueue<>();
    
    public UserChangerTest(UserPrincipal oldUser, UserPrincipal newUser) {
        this.oldUser = oldUser;
        this.newUser = newUser;
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        List<AclEntry> currentACLEntries = Files.getFileAttributeView(file, AclFileAttributeView.class).getAcl();
        List<AclEntry> neededACLEntries = new ArrayList<>();
    
        currentACLEntries.stream().forEach((acl)->{
            if (acl.equals(oldUser)) {
                try {
                    Files.setOwner(file, newUser);
                    filesACLs.add(MessageFormat.format("File: {0}\nOld ACL:\n{1}\nNew ACL:\n{2}\n\n",
                        file, new TForms().fromArray(currentACLEntries), new TForms().fromArray(neededACLEntries)));
                }
                catch (IOException e) {
                    Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
                }
                neededACLEntries.add(changeACL(acl));
            }
            else {
                neededACLEntries.add(acl);
            }
        });
    
        Files.getFileAttributeView(file, AclFileAttributeView.class).setAcl(neededACLEntries);
        this.filesCounter++;
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
        if (!filesACLs.isEmpty()) {
            FileSystemWorker.appendObjectToFile(new File(this.getClass().getSimpleName() + ".res"), filesACLs);
            filesACLs.clear();
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserChanger{");
        sb.append(", oldUser='").append(oldUser).append('\'');
        sb.append(", newUser='").append(newUser.getName()).append('\'');
        sb.append(", Files visited: ").append(filesCounter).append(". Folders visited: ").append(foldersCounter).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    protected void adSnapToUserRelolver() {
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream("\\\\srv-fs\\Common_new\\14_ИТ_служба\\Внутренняя\\srv-ad.dat"))) {
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
    
    private AclEntry changeACL(AclEntry acl) {
        AclEntry.Builder aclBuilder = AclEntry.newBuilder();
        aclBuilder.setPermissions(acl.permissions());
        aclBuilder.setType(acl.type());
        aclBuilder.setPrincipal(newUser);
        aclBuilder.setFlags(acl.flags());
        return aclBuilder.build();
    }
    


    public static class TestLocal {
    
    
        private UserPrincipal iGarnagina;
        
        private UserPrincipal tBabicheva;
    
        private UserChangerTest userChangerTest = new UserChangerTest(iGarnagina, tBabicheva);
    
        /**
         LONG RUNNING
         */
        @Test(enabled = false)
        public void userChangerTest() {
            Path startPath = Paths.get("\\\\srv-fs\\Common_new\\06_Маркетинг\\");
    
            String tBabichevaSID = "S-1-5-21-3970069352-2416023058-3822801030-4235";
            String iGarnaginaSID = "S-1-5-21-3970069352-2416023058-3822801030-7283";
            
            try {
                this.iGarnagina = Files.getOwner(Paths.get("\\\\srv-fs\\Common_new\\14_ИТ_служба\\Общая\\tmp_owner\\suchkova.txt"));
                this.tBabicheva = Files.getOwner(Paths.get("\\\\srv-fs\\Common_new\\14_ИТ_служба\\Общая\\tmp_owner\\tbabicheva.txt"));
    
                Assert.assertTrue(iGarnagina
                    .equals(Files.getOwner(Paths.get("\\\\srv-fs\\Common_new\\14_ИТ_служба\\Общая\\tmp_owner\\control.txt"))), tBabichevaSID + " " + tBabicheva
                    .toString());
            }
            catch (IOException e) {
                Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            }
    
            UserChangerTest userChangerTest = new UserChangerTest(iGarnagina, tBabicheva);
            try {
                Files.walkFileTree(startPath, userChangerTest);
            }
            catch (IOException e) {
                Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            }
            Assert.assertTrue(userChangerTest.toString().contains(iGarnaginaSID));
            Assert.assertTrue(userChangerTest.toString().contains(tBabichevaSID));
    
            try {
                Files.walkFileTree(startPath, userChangerTest);
            }
            catch (IOException e) {
                Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            }
        }
    
        @Test
        public void testAdSnapToUserRelolver() {
            userChangerTest.adSnapToUserRelolver();
        }
    }
}
