// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common.usermanagement;


import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserPrincipal;
import java.text.MessageFormat;


/**
 @see ru.vachok.networker.accesscontrol.common.usermanagement.UserACLCommonManagerImplTest
 @since 17.07.2019 (11:44) */
public class UserACLCommonManagerImpl implements UserACLCommonManager {
    
    private int filesCounter;
    
    private int foldersCounter;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private Path startPath;
    
    public UserACLCommonManagerImpl(Path startPath) {
        this.startPath = startPath;
    }
    
    @Override
    public String addAccess() {
        throw new TODOException("25.07.2019 (17:07)\n" + UserACLCommonAdder.class.getTypeName());
    }
    
    @Override
    public String removeAccess() {
        throw new TODOException("25.07.2019 (13:38)");
    }
    
    @Override
    public String replaceUsers(UserPrincipal oldUser, UserPrincipal newUser) {
        try {
            Files.walkFileTree(startPath, new UserACLReplacer(oldUser, startPath, newUser));
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("UserACLCommonManagerImpl.call: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        return "todo 25.07.2019 (17:36)";
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
