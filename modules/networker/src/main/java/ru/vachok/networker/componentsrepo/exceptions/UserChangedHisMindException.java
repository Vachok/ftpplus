// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.exceptions;


import java.text.MessageFormat;


/**
 Class ru.vachok.networker.componentsrepo.exceptions.UserChangedHisMindException
 <p>
 
 @since 12.07.2019 (20:35) */
public class UserChangedHisMindException extends IllegalStateException {
    
    
    private String motiv = "User says NO...";
    
    public UserChangedHisMindException() {
    }
    
    public UserChangedHisMindException(String motiv) {
        this.motiv = MessageFormat.format("{0}. Motivation: {1}", motiv, this.motiv);
    }
    
    @Override
    public String getMessage() {
        return motiv;
    }
}