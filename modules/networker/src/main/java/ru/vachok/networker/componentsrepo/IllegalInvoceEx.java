// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo;


/**
 Class ru.vachok.networker.componentsrepo.IllegalInvoceEx
 <p>
 
 @since 23.06.2019 (0:27) */
public class IllegalInvoceEx extends IllegalStateException {
    
    
    private String message;
    
    public IllegalInvoceEx(String message) {
        super(message);
        this.message = message;
    }
    
    @Override public String getMessage() {
        return message;
    }
}