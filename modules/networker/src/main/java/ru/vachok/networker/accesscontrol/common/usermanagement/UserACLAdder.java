package ru.vachok.networker.accesscontrol.common.usermanagement;


import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.text.MessageFormat;
import java.util.*;

import static ru.vachok.networker.accesscontrol.common.usermanagement.UserACLManager.createACLForUserFromExistsACL;


/**
 @see ru.vachok.networker.accesscontrol.common.usermanagement.UserACLAdderTest
 @since 25.07.2019 (13:27) */
class UserACLAdder extends SimpleFileVisitor<Path> {
    
    
    private UserPrincipal oldUser;
    
    private Path startPath;
    
    private UserPrincipal newUser;
    
    private int foldersCounter;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private int filesCounter;
    
    private Set<AclEntry> neededACLs = new HashSet<>();
    
    public UserACLAdder(UserPrincipal newUser) {
        this.newUser = newUser;
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        foldersCounter++;
        try {
            createACLs(dir);
            List<AclEntry> tipEnters = new ArrayList<>(neededACLs);
            Files.getFileAttributeView(dir, AclFileAttributeView.class).setAcl(tipEnters);
            return FileVisitResult.CONTINUE;
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("UserACLCommonAdder.preVisitDirectory: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            return FileVisitResult.CONTINUE;
        }
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        filesCounter++;
        try {
            createACLs(file);
            List<AclEntry> tipEnters = new ArrayList<>(neededACLs);
            Files.getFileAttributeView(file, AclFileAttributeView.class).setAcl(tipEnters);
    
            return FileVisitResult.CONTINUE;
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("UserACLCommonAdder.visitFile: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            return FileVisitResult.CONTINUE;
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserACLCommonAdder{");
        sb.append("oldUser=").append(oldUser);
        sb.append(", startPath=").append(startPath);
        sb.append(", newUser=").append(newUser);
        sb.append(", foldersCounter=").append(foldersCounter);
        sb.append(", messageToUser=").append(messageToUser);
        sb.append(", filesCounter=").append(filesCounter);
        sb.append(", neededACLs=").append(neededACLs);
        sb.append('}');
        return sb.toString();
    }
    
    protected Set<AclEntry> getNeededACLs() {
        return Collections.unmodifiableSet(neededACLs);
    }
    
    protected void createACLs(Path path) throws IOException {
        Map<UserPrincipal, AclEntry> principalAclEntry = new HashMap<>();
        AclFileAttributeView aclFileAttributeView = Files.getFileAttributeView(path, AclFileAttributeView.class);
        List<AclEntry> currentACLs = aclFileAttributeView.getAcl();
        List<AclEntry> rootACL = Files.getFileAttributeView(ConstantsFor.COMMON_DIR, AclFileAttributeView.class).getAcl();
        
        currentACLs.stream().forEach(acl->principalAclEntry.put(acl.principal(), acl));
        
        AclEntry ethalonACL = rootACL.get(0);
        for (AclEntry acl : rootACL) {
            if (!acl.principal().equals(newUser) & !acl.type().name().equalsIgnoreCase("deny") & acl.principal().toString().contains("BUILTIN\\Администраторы")) {
                ethalonACL = acl;
            }
        }
        AclEntry addAcl = createACLForUserFromExistsACL(ethalonACL, newUser);
        principalAclEntry.put(addAcl.principal(), addAcl);
        neededACLs.clear();
        neededACLs.addAll(principalAclEntry.values());
    }
}
