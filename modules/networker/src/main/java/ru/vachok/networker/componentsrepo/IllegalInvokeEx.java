// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo;


import ru.vachok.networker.ConstantsFor;


/**
 Class ru.vachok.networker.componentsrepo.IllegalInvokeEx
 <p>
 
 @since 23.06.2019 (0:27) */
public class IllegalInvokeEx extends IllegalStateException {
    
    
    private String message;
    
    private static final String APP_VERSION = ConstantsFor.APP_VERSION;
    
    public IllegalInvokeEx(String message) {
        super(message);
        this.message = message;
    }
    
    @Override public String getMessage() {
        return message + " this is " + APP_VERSION + " app";
    }
}