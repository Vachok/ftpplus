package ru.vachok.networker.accesscontrol.common.usermanagement;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.text.MessageFormat;
import java.util.*;

import static ru.vachok.networker.accesscontrol.common.usermanagement.UserACLManager.createACLForUserFromExistsACL;


/**
 @since 25.07.2019 (16:41) */
public class UserACLReplacer extends SimpleFileVisitor<Path> implements Runnable {
    
    
    private final UserPrincipal oldUser;
    
    private final Path startPath;
    
    private final UserPrincipal newUser;
    
    private final File fileForAppend = new File(this.getClass().getSimpleName() + ".res");
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private int followLinks = Integer.MAX_VALUE;
    
    private Set<AclEntry> currentACLEntries = new HashSet<>();
    
    private List<AclEntry> neededACLEntries = new ArrayList<>();
    
    private int foldersCounter = 0;
    
    private int filesCounter = 0;
    
    public UserACLReplacer(UserPrincipal oldUser, Path startPath, UserPrincipal newUser) {
        this.oldUser = oldUser;
        this.startPath = startPath;
        this.newUser = newUser;
        fileForAppend.delete();
    }
    
    public UserACLReplacer(UserPrincipal oldUser, UserPrincipal newUser) {
        this.oldUser = oldUser;
        this.newUser = newUser;
        this.startPath = Paths.get("\\\\srv-fs.eatmeat.ru\\common_new");
        fileForAppend.delete();
    }
    
    public void setFollowLinks(int followLinks) {
        this.followLinks = followLinks;
    }
    
    @Override
    public void run() {
        System.out.println("oldUser = " + oldUser);
        System.out.println("newUser = " + newUser);
        System.out.println("startPath = " + startPath);
        System.out.println("followLinks = " + followLinks);
        try {
            Files.walkFileTree(startPath, Collections.singleton(FileVisitOption.FOLLOW_LINKS), followLinks, this);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("UserACLReplacer.run: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        checkOwner(dir);
        try {
            currentACLEntries.addAll(Files.getFileAttributeView(dir, AclFileAttributeView.class).getAcl());
            if (currentACLEntries.size() > 0) {
                neededACLEntries.clear();
                for (AclEntry aclEntry : currentACLEntries) {
                    messageToUser.info(MessageFormat.format("Folder num: {0}) CHECKING FOR FOLDER NEW USER (checked {1} files)", foldersCounter, filesCounter));
                    checkACL(aclEntry);
                }
            }
            if (neededACLEntries.size() > 0) {
                Files.getFileAttributeView(dir, AclFileAttributeView.class).setAcl(neededACLEntries);
            }
            currentACLEntries.addAll(Files.getFileAttributeView(dir, AclFileAttributeView.class).getAcl());
            messageToUser.info(MessageFormat.format("{0}({2}) | {1} ", this.foldersCounter++, dir, this.filesCounter));
            return FileVisitResult.CONTINUE;
        }
        catch (IOException e) {
            return FileVisitResult.CONTINUE;
        }
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        checkOwner(file);
        try {
            currentACLEntries.addAll(Files.getFileAttributeView(file, AclFileAttributeView.class).getAcl());
            if (currentACLEntries.size() > 0) {
                neededACLEntries.clear();
                for (AclEntry acl : currentACLEntries) {
                    checkACL(acl);
                }
            }
            if (neededACLEntries.size() > 0) {
                Files.getFileAttributeView(file, AclFileAttributeView.class).setAcl(neededACLEntries);
            }
            currentACLEntries.addAll(Files.getFileAttributeView(file, AclFileAttributeView.class).getAcl());
            this.filesCounter++;
            return FileVisitResult.CONTINUE;
        }
        catch (IOException e) {
            return FileVisitResult.CONTINUE;
        }
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        FileSystemWorker.appendObjectToFile(fileForAppend, file.toAbsolutePath().normalize().toString() + "\n" + new TForms().fromArray(exc));
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        new ConcreteFolderACLWriter(dir, FileNames.FILENAME_OWNER + ".replacer").run();
        FileSystemWorker.appendObjectToFile(fileForAppend,
            MessageFormat.format("Directory: {0}, owner: {1}\n", dir, Files.getOwner(dir)));
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
                String objectToAppend = MessageFormat.format("New owner for {0} is {1}", path.toAbsolutePath().normalize().toString(), newUser);
                messageToUser.warn(Files.getOwner(path).getName() + " NEW OWNER SET");
                FileSystemWorker.appendObjectToFile(fileForAppend, objectToAppend);
            }
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("UserChanger.visitFile: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    }
    
    private void checkACL(@NotNull AclEntry acl) {
        if (!acl.principal().equals(newUser) && acl.principal().equals(oldUser)) {
            AclEntry newUserACL = createACLForUserFromExistsACL(acl, newUser);
            neededACLEntries.add(newUserACL);
            String appendObjectToFile = FileSystemWorker.appendObjectToFile(fileForAppend, newUserACL.toString() + " is created.\n");
            messageToUser.warn(newUserACL.toString() + " SET ");
        }
        else {
            neededACLEntries.add(acl);
        }
    }
    
}
