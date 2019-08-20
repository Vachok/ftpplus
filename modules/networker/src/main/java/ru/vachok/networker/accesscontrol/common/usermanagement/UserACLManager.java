// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common.usermanagement;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.*;
import java.text.MessageFormat;
import java.util.List;


public interface UserACLManager {
    
    
    String ACL_PARSING = ACLParser.class.getTypeName();
    
    String ADD = UserACLAdder.class.getTypeName();
    
    String DEL = UserACLDeleter.class.getTypeName();
    
    String addAccess(UserPrincipal newUser);
    
    String removeAccess(UserPrincipal oldUser);
    
    String replaceUsers(UserPrincipal oldUser, UserPrincipal newUser);
    
    void setClassOption(Object classOption);
    
    static @NotNull AclEntry createACLForUserFromExistsACL(@NotNull AclEntry acl, UserPrincipal principal) {
        AclEntry.Builder aclBuilder = AclEntry.newBuilder();
        aclBuilder.setPermissions(acl.permissions());
        aclBuilder.setType(acl.type());
        aclBuilder.setPrincipal(principal);
        aclBuilder.setFlags(acl.flags());
        return aclBuilder.build();
    }
    
    static void setACLToAdminsOnly(@NotNull Path pathToFile) {
        AclFileAttributeView attributeView = Files.getFileAttributeView(pathToFile, AclFileAttributeView.class);
        try {
            UserPrincipal userPrincipal = Files.getOwner(pathToFile.getRoot());
            Files.setOwner(pathToFile, userPrincipal);
            AclEntry newACL = UserACLManager.createNewACL(userPrincipal);
            List<AclEntry> aclEntries = Files.getFileAttributeView(pathToFile, AclFileAttributeView.class).getAcl();
            aclEntries.add(newACL);
            Files.getFileAttributeView(pathToFile, AclFileAttributeView.class).setAcl(aclEntries);
        }
        catch (IOException e) {
            LoggerFactory.getLogger(UserACLManager.class.getSimpleName()).error(MessageFormat
                    .format("UserACLManager.setACLToAdminsOnly: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    }
    
    static @NotNull AclEntry createNewACL(UserPrincipal userPrincipal) {
        AclEntry.Builder builder = AclEntry.newBuilder();
        builder.setPermissions(AclEntryPermission.values());
        builder.setType(AclEntryType.ALLOW);
        builder.setPrincipal(userPrincipal);
        builder.setFlags(AclEntryFlag.FILE_INHERIT);
        builder.setFlags(AclEntryFlag.DIRECTORY_INHERIT);
        builder.setFlags(AclEntryFlag.INHERIT_ONLY);
        return builder.build();
    }
    
    @Contract("_, _ -> new")
    static @NotNull UserACLManager getI(@NotNull String type, Path startPath) {
        if (type.equals(ACL_PARSING)) {
            return new ACLParser();
        }
        else if (type.equals(ADD)) {
            return new UserACLAdder(startPath);
        }
        else if (type.equals(DEL)) {
            return new UserACLDeleter(startPath);
        }
        else {
            return new UserACLReplacer(startPath);
        }
    }
    
    String getResult();
}
