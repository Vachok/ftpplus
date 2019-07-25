package ru.vachok.networker.accesscontrol.common.usermanagement;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.accesscontrol.common.CommonConcreteFolderACLWriter;
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
import java.util.ArrayList;
import java.util.List;

import static ru.vachok.networker.accesscontrol.common.usermanagement.UserACLCommonManager.createACLForUserFromExistsACL;


/**
 @since 25.07.2019 (16:41) */
public class UserACLReplacer extends SimpleFileVisitor<Path> {
    
    
    private final UserPrincipal oldUser;
    
    private final Path startPath;
    
    private final UserPrincipal newUser;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private List<AclEntry> currentACLEntries = new ArrayList<>();
    
    private List<AclEntry> neededACLEntries = new ArrayList<>();
    
    private int foldersCounter = 0;
    
    private int filesCounter = 0;
    
    public UserACLReplacer(UserPrincipal oldUser, Path startPath, UserPrincipal newUser) {
        this.oldUser = oldUser;
        this.startPath = startPath;
        this.newUser = newUser;
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        checkOwner(dir);
        try {
            currentACLEntries = Files.getFileAttributeView(dir, AclFileAttributeView.class).getAcl();
            if (currentACLEntries.size() > 0) {
                for (AclEntry aclEntry : currentACLEntries) {
                    checkACL(aclEntry);
                }
            }
            if (neededACLEntries.size() > 0) {
                Files.getFileAttributeView(dir, AclFileAttributeView.class).setAcl(neededACLEntries);
            }
            currentACLEntries = Files.getFileAttributeView(dir, AclFileAttributeView.class).getAcl();
        }
        catch (IOException e) {
            return FileVisitResult.CONTINUE;
        }
        messageToUser.info(MessageFormat.format("{0}) {1} SET:\n{2}", this.foldersCounter++, dir, new TForms().fromArray(currentACLEntries)));
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        checkOwner(file);
        try {
            currentACLEntries = Files.getFileAttributeView(file, AclFileAttributeView.class).getAcl();
            if (currentACLEntries.size() > 0) {
                neededACLEntries.clear();
                for (AclEntry acl : currentACLEntries) {
                    checkACL(acl);
                }
            }
            if (neededACLEntries.size() > 0) {
                Files.getFileAttributeView(file, AclFileAttributeView.class).setAcl(neededACLEntries);
            }
            currentACLEntries = Files.getFileAttributeView(file, AclFileAttributeView.class).getAcl();
        }
        catch (IOException e) {
            return FileVisitResult.CONTINUE;
        }
        messageToUser.info(MessageFormat.format("{0}) {1} SET:\n{2}", this.filesCounter++, file, new TForms().fromArray(currentACLEntries)));
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        new CommonConcreteFolderACLWriter(dir).run();
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserACLReplacer{");
        sb.append("oldUser=").append(oldUser);
        sb.append(", startPath=").append(startPath);
        sb.append(", newUser=").append(newUser);
        sb.append(", foldersCounter=").append(foldersCounter);
        sb.append(", filesCounter=").append(filesCounter);
        sb.append('}');
        return sb.toString();
    }
    
    private void checkOwner(Path path) {
        currentACLEntries.clear();
        try {
            if (Files.getOwner(path).equals(oldUser)) {
                Files.setOwner(path, newUser);
            }
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("UserChanger.visitFile: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    }
    
    private void checkACL(@NotNull AclEntry acl) {
        if (acl.principal().equals(oldUser)) {
            neededACLEntries.add(createACLForUserFromExistsACL(acl, newUser));
        }
        else {
            neededACLEntries.add(acl);
        }
        
    }
    
}
