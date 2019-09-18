// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.usermanagement;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.*;
import java.text.MessageFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;


/**
 @see UserACLManagerImplTest
 @since 17.07.2019 (11:44) */
abstract class UserACLManagerImpl extends SimpleFileVisitor<Path> implements UserACLManager {
    
    
    protected Path startPath;
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, UserACLManagerImpl.class.getSimpleName());
    
    private UserPrincipal oldUser;
    
    private UserPrincipal newUser;
    
    private UserACLManagerImpl aclManager;
    
    UserACLManagerImpl(Path startPath) {
        this.startPath = startPath;
    }
    
    /**
     @param userPrincipal для кого создать acl
     @param rwInherit r - только чтение, rw - чтение/запись, i - наследовать. <br> Если нужно сделать разрешение ТОЛЬКО к объекту - i не ставим. Если нужно к дереву = добавляем i.
     @return acl для выбранного пользователя, чтобы включить в {@link AclFileAttributeView}
     */
    public static @NotNull AclEntry createACLFor(UserPrincipal userPrincipal, @NotNull String rwInherit) {
        final AclEntry.Builder builder = AclEntry.newBuilder();
        if (rwInherit.toLowerCase().contains("rw")) {
            builder.setPermissions(AclEntryPermission.values());
        }
        else {
            builder.setPermissions(setReadOnlyPermission());
        }
        builder.setType(AclEntryType.ALLOW);
        builder.setPrincipal(userPrincipal);
        if (rwInherit.toLowerCase().contains("i")) {
            builder.setFlags(AclEntryFlag.DIRECTORY_INHERIT);
            builder.setFlags(AclEntryFlag.FILE_INHERIT);
        }
        else {
            builder.setFlags(AclEntryFlag.NO_PROPAGATE_INHERIT);
        }
        return builder.build();
    }
    
    public static @NotNull AclEntry createACLForUserFromExistsACL(@NotNull AclEntry templateACL, UserPrincipal userNeeded) {
        AclEntry.Builder aclBuilder = AclEntry.newBuilder();
        aclBuilder.setPermissions(templateACL.permissions());
        aclBuilder.setType(templateACL.type());
        aclBuilder.setPrincipal(userNeeded);
        aclBuilder.setFlags(templateACL.flags());
        return aclBuilder.build();
    }
    
    public static void setACLToAdminsOnly(@NotNull Path pathToFile) {
        AclFileAttributeView attributeView = Files.getFileAttributeView(pathToFile, AclFileAttributeView.class);
        try {
            UserPrincipal userPrincipal = Files.getOwner(pathToFile.getRoot());
            AclEntry newACL = createACLFor(userPrincipal, "rw");
            List<AclEntry> aclEntries = Files.getFileAttributeView(pathToFile, AclFileAttributeView.class).getAcl();
            aclEntries.add(newACL);
            Files.setOwner(pathToFile, userPrincipal);
            Files.getFileAttributeView(pathToFile, AclFileAttributeView.class).setAcl(aclEntries);
        }
        catch (IOException e) {
            LoggerFactory.getLogger(UserACLManager.class.getSimpleName()).error(MessageFormat
                    .format("UserACLManager.setACLToAdminsOnly: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    }
    
    private static @NotNull Set<AclEntryPermission> setReadOnlyPermission() {
        Set<AclEntryPermission> permList = new LinkedHashSet<>();
        for (AclEntryPermission permission : AclEntryPermission.values()) {
            if (!permission.toString().toLowerCase().contains("write") & !permission.toString().toLowerCase().contains("delete") & !permission.toString()
                .toLowerCase().contains("append")) {
                permList.add(permission);
            }
        }
        return permList;
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
        sb.append(", startPath=").append(startPath);
        sb.append('}');
        return sb.toString();
    }
    
    @Contract("_, _ -> new")
    protected static @NotNull UserACLManagerImpl getI(@NotNull String type, Path startPath) {
        switch (type) {
            case UserACLManager.ADD:
                return new UserACLAdder(startPath);
            case UserACLManager.DEL:
                return new UserACLDeleter(startPath);
            default:
                return new UserACLReplacer(startPath);
        }
    }
}
