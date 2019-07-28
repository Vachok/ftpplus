package ru.vachok.networker.accesscontrol.common.usermanagement;


import org.jetbrains.annotations.NotNull;

import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.UserPrincipal;


public interface UserACLCommonManager {
    
    
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
}
