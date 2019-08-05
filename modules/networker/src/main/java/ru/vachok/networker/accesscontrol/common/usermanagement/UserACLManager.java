package ru.vachok.networker.accesscontrol.common.usermanagement;


import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import ru.vachok.networker.abstr.Keeper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.*;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public interface UserACLManager extends Keeper {
    
    
    String addAccess(UserPrincipal newUser);
    
    String removeAccess(UserPrincipal oldUser);
    
    String replaceUsers(UserPrincipal oldUser, UserPrincipal newUser);
    
    static @NotNull AclEntry createACLForUserFromExistsACL(@NotNull AclEntry acl, UserPrincipal principal) {
        AclEntry.Builder aclBuilder = AclEntry.newBuilder();
        aclBuilder.setPermissions(acl.permissions());
        aclBuilder.setType(acl.type());
        aclBuilder.setPrincipal(principal);
        aclBuilder.setFlags(acl.flags());
        return aclBuilder.build();
    }
    
    static @NotNull AclEntry createNewACL(UserPrincipal userPrincipal) {
        AclEntry.Builder builder = AclEntry.newBuilder();
        builder.setPrincipal(userPrincipal);
        
        builder.setPermissions(AclEntryPermission.values());
        
        Set<AclEntryFlag> setFlags = new HashSet<>();
        setFlags.add(AclEntryFlag.DIRECTORY_INHERIT);
        setFlags.add(AclEntryFlag.FILE_INHERIT);
        
        builder.setFlags(setFlags);
        
        builder.setType(AclEntryType.ALLOW);
        
        return builder.build();
    }
    
    static boolean setACLToAdminsOnly(@NotNull Path pathToFile) {
        AclFileAttributeView attributeView = Files.getFileAttributeView(pathToFile, AclFileAttributeView.class);
        try {
            UserPrincipal userPrincipal = Files.getOwner(pathToFile.getRoot());
            Files.setOwner(pathToFile, userPrincipal);
            AclEntry newACL = UserACLManager.createNewACL(userPrincipal);
            List<AclEntry> aclEntries = Files.getFileAttributeView(pathToFile, AclFileAttributeView.class).getAcl();
            aclEntries.add(newACL);
            Files.getFileAttributeView(pathToFile, AclFileAttributeView.class).setAcl(aclEntries);
            return true;
        }
        catch (IOException e) {
            LoggerFactory.getLogger(UserACLManager.class.getSimpleName()).error(MessageFormat
                .format("UserACLManager.setACLToAdminsOnly: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            return false;
        }
    }
    
}
