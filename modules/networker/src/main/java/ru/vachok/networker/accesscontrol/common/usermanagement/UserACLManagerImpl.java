// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common.usermanagement;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.UserPrincipal;
import java.text.MessageFormat;
import java.util.Queue;


/**
 @see ru.vachok.networker.accesscontrol.common.usermanagement.UserACLManagerImplTest
 @since 17.07.2019 (11:44) */
public abstract class UserACLManagerImpl extends SimpleFileVisitor<Path> implements UserACLManager {
    
    
    protected Path startPath;
    
    private int filesCounter;
    
    private int foldersCounter;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private UserPrincipal oldUser;
    
    private UserPrincipal newUser;
    
    private UserACLManagerImpl aclManager;
    
    public UserACLManagerImpl(Path startPath) {
        this.startPath = startPath;
    }
    
    @Override
    public String addAccess(UserPrincipal newUser) {
        try {
            this.aclManager = new UserACLAdder(startPath);
            aclManager.setClassOption(newUser);
            Files.walkFileTree(startPath, aclManager);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("UserACLCommonManagerImpl.addAccess: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        return startPath + " added " + newUser.getName();
    }
    
    @Override
    public String removeAccess(UserPrincipal oldUser) {
    
        try {
            this.aclManager = new UserACLDeleter(startPath);
            aclManager.setClassOption(oldUser);
            Files.walkFileTree(startPath, aclManager);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat
                    .format("UserACLCommonManagerImpl.removeAccess threw away: {0}, ({1}).\n\n{2}", e.getMessage(), e.getClass().getName(), new TForms().fromArray(e)));
        }
        return startPath + " removed " + oldUser.getName();
    }
    
    @Override
    public String replaceUsers(UserPrincipal oldUser, UserPrincipal newUser) {
        this.oldUser = oldUser;
        this.newUser = newUser;
        Path foldersFilePath = Paths.get("foldersFilePath").toAbsolutePath().normalize();
    
        if (foldersFilePath.toFile().exists()) {
            aclFromFile(foldersFilePath);
        }
        else {
            try {
                this.aclManager = new UserACLReplacer(oldUser, foldersFilePath, newUser);
                Files.walkFileTree(startPath, aclManager);
            }
            catch (IOException e) {
                messageToUser.error(MessageFormat.format("UserACLCommonManagerImpl.call: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            }
        }
        return MessageFormat.format("{0} users changed.\nWAS: {1} ; NOW: {2}", startPath, oldUser, newUser);
    }
    
    @Override
    public abstract void setClassOption(Object classOption);
    
    @Override
    public abstract String getResult();
    
    private void aclFromFile(Path foldersFilePath) {
        Queue<String> foldersWithACL = FileSystemWorker.readFileToQueue(foldersFilePath);
        while (!foldersWithACL.isEmpty()) {
            replaceACL(foldersWithACL.poll());
            messageToUser.warn(foldersWithACL.size() + " foldersWithACL size.");
        }
    }
    
    private void replaceACL(@NotNull String folderWithACL) {
        Path path = Paths.get("\\\\srv-fs.eatmeat.ru\\it$$\\ХЛАМ\\testClean\\");
        
        if (!folderWithACL.isEmpty()) {
            path = Paths.get(folderWithACL).normalize();
        }
        UserACLReplacer userACLReplacer = new UserACLReplacer(oldUser, path, newUser);
        userACLReplacer.setFollowLinks(1);
        userACLReplacer.run();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserACLCommonManagerImpl{");
        sb.append("filesCounter=").append(filesCounter);
        sb.append(", foldersCounter=").append(foldersCounter);
        sb.append(", messageToUser=").append(messageToUser);
        sb.append(", startPath=").append(startPath);
        sb.append('}');
        return sb.toString();
    }
}
