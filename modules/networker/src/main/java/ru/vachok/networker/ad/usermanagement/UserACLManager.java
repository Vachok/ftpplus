// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.usermanagement;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.attribute.UserPrincipal;


/**
 @see UserACLManagerTest */
public interface UserACLManager {
    
    
    String ACL_PARSING = "ACLParser";
    
    String ADD = "UserACLAdder";
    
    String DEL = "UserACLDeleter";
    
    String DB_SEARCH = "ACLDatabaseSearcher";
    
    String addAccess(UserPrincipal newUser);
    
    String removeAccess(UserPrincipal oldUser);
    
    String replaceUsers(UserPrincipal oldUser, UserPrincipal newUser);
    
    void setClassOption(Object classOption);
    
    String getResult();
    
    @Contract("_, _ -> new")
    static @NotNull UserACLManager getInstance(@NotNull String parsing, Path path) {
        switch (parsing) {
            default:
                return new ACLParser();
        }
    }
}
