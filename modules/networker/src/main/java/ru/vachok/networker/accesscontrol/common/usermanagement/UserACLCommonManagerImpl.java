// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common.usermanagement;


import ru.vachok.networker.TForms;
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
    public String addAccess(UserPrincipal newUser) {
        throw new TODOException("25.07.2019 (17:07)\n" + UserACLCommonAdder.class.getTypeName());
    }
    
    @Override
    public String removeAccess(UserPrincipal oldUser) {
    
        try {
            Files.walkFileTree(startPath, new UserACLCommonDeleter(oldUser));
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat
                .format("UserACLCommonManagerImpl.removeAccess threw away: {0}, ({1}).\n\n{2}", e.getMessage(), e.getClass().getName(), new TForms().fromArray(e)));
        }
        return startPath + " removed " + oldUser.getName();
    }
    
    @Override
    public String replaceUsers(UserPrincipal oldUser, UserPrincipal newUser) {
        try {
            Files.walkFileTree(startPath, new UserACLReplacer(oldUser, startPath, newUser));
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("UserACLCommonManagerImpl.call: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        return "";
    }
}
