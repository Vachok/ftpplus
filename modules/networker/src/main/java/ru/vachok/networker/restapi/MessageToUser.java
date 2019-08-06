// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi;


import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;


public interface MessageToUser extends ru.vachok.messenger.MessageToUser {
    
    
    @Override
    default String confirm(String s, String s1, String s2) {
        throw new InvokeIllegalException("06.08.2019 (11:39)");
    }
}