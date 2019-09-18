// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.usermanagement;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.attribute.UserPrincipal;


/**
 @see UserACLManagerTest */
public interface UserACLManager {
    
    
    String ACL_PARSING = ACLParser.class.getTypeName();
    
    String ADD = "UserACLAdder";
    
    String DEL = "UserACLDeleter";
    
    String addAccess(UserPrincipal newUser);
    
    String removeAccess(UserPrincipal oldUser);
    
    String replaceUsers(UserPrincipal oldUser, UserPrincipal newUser);
    
    void setClassOption(Object classOption);
    
    @Contract("_, _ -> new")
    static @NotNull UserACLManager getInstance(@NotNull String type, Path startPath) {
        if (type.equals(ACL_PARSING)) {
            return new ACLParser();
        }
        else {
            return UserACLManagerImpl.getI(type, startPath);
        }
    }
    
    String getResult();
}
