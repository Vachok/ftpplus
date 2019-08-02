package ru.vachok.networker.accesscontrol.common.usermanagement;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.abstr.Keeper;

import java.nio.file.attribute.*;
import java.util.HashSet;
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
}
