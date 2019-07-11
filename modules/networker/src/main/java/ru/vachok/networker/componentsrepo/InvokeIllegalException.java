// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo;


import ru.vachok.networker.ConstantsFor;


/**
 Class ru.vachok.networker.componentsrepo.InvokeIllegalException
 <p>
 
 @since 23.06.2019 (0:27) */
public class InvokeIllegalException extends IllegalStateException {
    
    
    private String message;
    
    private static final String APP_VERSION = ConstantsFor.APP_VERSION;
    
    public InvokeIllegalException(String message) {
        super(message);
        this.message = message;
    }
    
    public InvokeIllegalException() {
        this.message = "This functional is not ready yet.";
    }
    
    @Override public String getMessage() {
        System.out.println("ConstantsFor.thisPC() = " + ConstantsFor.thisPC());
        System.out.println("ConstantsFor.getMemoryInfo() = " + ConstantsFor.getMemoryInfo());
        return message + " this is " + APP_VERSION + " :" + getStackTrace()[0];
    }
}