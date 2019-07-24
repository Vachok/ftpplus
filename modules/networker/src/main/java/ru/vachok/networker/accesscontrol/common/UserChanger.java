// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;


/**
 @see ru.vachok.networker.accesscontrol.common.UserChangerTest
 @since 17.07.2019 (11:44) */
public class UserChanger extends SimpleFileVisitor<Path> implements Callable<String> {
    
    
    private UserPrincipal oldUser;
    
    private UserPrincipal newUser;
    
    private int filesCounter;
    
    private int foldersCounter;
    
    private boolean isAdd;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private Collection<String> filesACLs = new LinkedBlockingQueue<>();
    
    private Path startPath;
    
    public UserChanger(UserPrincipal oldUser, Path startPath, UserPrincipal newUser, String needAdd) {
        this.oldUser = oldUser;
        this.newUser = newUser;
        this.isAdd = needAdd.equalsIgnoreCase("add");
        this.startPath = startPath;
    }
    
    public UserChanger(UserPrincipal oldUser, Path startPath, UserPrincipal newUser) {
        this.oldUser = oldUser;
        this.newUser = newUser;
        this.startPath = startPath;
    }
    
    @Override
    public String call() throws Exception {
        if (isAdd) {
            Files.walkFileTree(startPath, new UserAdder(oldUser, startPath, newUser));
        }
        else {
            Files.walkFileTree(startPath, this);
        }
        return FileSystemWorker.writeFile(this.getClass().getSimpleName() + ".log", MessageFormat.format("{0} files in {1} directories changed from {2} to {3}",
            filesCounter, foldersCounter, oldUser.getName(), newUser.getName()));
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (newUser != null && Files.getOwner(dir).equals(oldUser)) {
            Files.setOwner(dir, newUser);
            messageToUser.info(MessageFormat.format("{0}) USER SET", foldersCounter), dir.toString(), newUser.toString());
        }
        new CommonConcreteFolderACLWriter(dir).run();
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        List<AclEntry> currentACLEntries = Files.getFileAttributeView(file, AclFileAttributeView.class).getAcl();
        List<AclEntry> neededACLEntries = new ArrayList<>();
        
        currentACLEntries.forEach((acl)->{
            if (acl.principal().equals(oldUser)) {
                try {
                    Files.setOwner(file, newUser);
                    filesACLs.add(MessageFormat.format("File: {0}\nOld ACL:\n{1}\nNew ACL:\n{2}\n\n",
                        file, new TForms().fromArray(currentACLEntries), new TForms().fromArray(neededACLEntries)));
                }
                catch (IOException e) {
                    messageToUser.error(MessageFormat.format("UserChanger.visitFile: {0}, ({1})", e.getMessage(), e.getClass().getName()));
                }
                
                if (newUser != null) {
                    neededACLEntries.add(changeACL(acl, newUser));
                }
                else {
                    neededACLEntries.add(changeACL(acl, oldUser));
                }
                
                messageToUser.info(MessageFormat.format("{0}) FILE SET", filesCounter), file.toString(), neededACLEntries.toString());
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
            messageToUser.error(MessageFormat.format("UserChanger.adSnapToUserRelolver: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    }
    
    private AclEntry changeACL(AclEntry acl, UserPrincipal principal) {
        AclEntry.Builder aclBuilder = AclEntry.newBuilder();
        aclBuilder.setPermissions(acl.permissions());
        aclBuilder.setType(acl.type());
        aclBuilder.setPrincipal(principal);
        aclBuilder.setFlags(acl.flags());
        return aclBuilder.build();
    }
    
    private List<AclEntry> addAcl(Path path) throws IOException {
        AclFileAttributeView aclFileAttributeView = Files.getFileAttributeView(path, AclFileAttributeView.class);
        List<AclEntry> aclEntries = aclFileAttributeView.getAcl();
        for (AclEntry aclEntry : aclFileAttributeView.getAcl()) {
            if (aclEntry.principal().equals(newUser)) {
                AclEntry addAcl = changeACL(aclEntry, oldUser);
                aclEntries.add(addAcl);
            }
        }
        return aclEntries;
    }
    
    private class UserAdder extends UserChanger {
        
        
        public UserAdder(UserPrincipal oldUser, Path startPath, UserPrincipal newUser) {
            super(oldUser, startPath, newUser);
        }
        
        @Override
        public String call() throws Exception {
            Files.walkFileTree(startPath, this);
            return oldUser.toString();
        }
        
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            foldersCounter++;
            
            List<AclEntry> aclEntryList = addAcl(dir);
            Files.getFileAttributeView(dir, AclFileAttributeView.class).setAcl(aclEntryList);
            
            Files.getFileAttributeView(dir, AclFileAttributeView.class).getAcl().forEach(acl->{
                if (acl.principal().equals(oldUser)) {
                    messageToUser.info("ACL TRUE!", foldersCounter + " foldersCounter", filesCounter + " filesCounter");
                }
            });
            new CommonConcreteFolderACLWriter(dir).run();
            return FileVisitResult.CONTINUE;
        }
        
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            filesCounter++;
            addAcl(file);
            
            return FileVisitResult.CONTINUE;
        }
    }
    
}
