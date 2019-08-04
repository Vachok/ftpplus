package ru.vachok.networker.accesscontrol.common.usermanagement;


import org.jetbrains.annotations.NotNull;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 @see ru.vachok.networker.accesscontrol.common.usermanagement.UserACLCommonDeleterTest
 @since 26.07.2019 (11:03) */
public class UserACLDeleter extends SimpleFileVisitor<Path> {
    
    
    private String userName;
    
    private UserPrincipal oldUser;
    
    private List<AclEntry> needACL = new ArrayList<>();
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    public UserACLDeleter(String userName) {
        this.userName = userName;
    }
    
    public UserACLDeleter(@NotNull UserPrincipal oldUser) {
        this.oldUser = oldUser;
        this.userName = oldUser.toString().split(" ")[0];
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        replaceOwner(dir);
        removeOldACL(dir);
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        needACL.clear();
        replaceOwner(file);
        removeOldACL(file);
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserACLCommonDeleter{");
        sb.append("userName='").append(userName).append('\'');
        sb.append(", oldUser=").append(oldUser);
        sb.append(", needACL=").append(needACL.size());
        sb.append('}');
        return sb.toString();
    }
    
    protected void replaceOwner(Path dir) throws IOException {
        UserPrincipal userPrincipal = Files.getOwner(dir);
        if (userPrincipal.equals(oldUser)) {
            Files.setOwner(dir, Files.getOwner(dir.getRoot()));
        }
    }
    
    private void removeOldACL(Path path) {
        AclFileAttributeView attributeView = Files.getFileAttributeView(path, AclFileAttributeView.class);
        Set<AclEntry> tmpSet = new HashSet<>();
        try {
            for (AclEntry aclEntry : attributeView.getAcl()) {
                if (!aclEntry.principal().equals(oldUser)) {
                    tmpSet.add(aclEntry);
                }
            }
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("UserACLCommonDeleter.removeOldACL: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        needACL.addAll(tmpSet);
        try {
            attributeView.setAcl(needACL);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format(path.toString(), e.getMessage(), e.getClass().getName()));
        }
    }
}
