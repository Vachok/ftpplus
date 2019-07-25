package ru.vachok.networker.accesscontrol.common.usermanagement;


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
 @since 25.07.2019 (13:27) */
public class UserACLCommonAdder extends SimpleFileVisitor<Path> {
    
    
    private UserPrincipal oldUser;
    
    private Path startPath;
    
    private UserPrincipal newUser;
    
    private int foldersCounter;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private int filesCounter;
    
    private List<AclEntry> neededACLs = new ArrayList<>();
    
    public UserACLCommonAdder(Path startPath, UserPrincipal newUser) {
        this.startPath = startPath;
        this.newUser = newUser;
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        foldersCounter++;
        try {
            if (createACLs(dir)) {
                Files.getFileAttributeView(dir, AclFileAttributeView.class).setAcl(neededACLs);
            }
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
            if (createACLs(file)) {
                Files.getFileAttributeView(file, AclFileAttributeView.class).setAcl(neededACLs);
            }
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("UserACLCommonAdder.visitFile: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        
        return FileVisitResult.CONTINUE;
    }
    
    private boolean createACLs(Path path) throws IOException {
        AclFileAttributeView aclFileAttributeView = Files.getFileAttributeView(path, AclFileAttributeView.class);
        List<AclEntry> aclEntryList = aclFileAttributeView.getAcl();
        AclEntry addAcl = createACLForUserFromExistsACL(aclEntryList.get(0), newUser);
        neededACLs.add(addAcl);
        neededACLs.addAll(aclEntryList);
        return neededACLs.size() > aclEntryList.size();
    }
}
