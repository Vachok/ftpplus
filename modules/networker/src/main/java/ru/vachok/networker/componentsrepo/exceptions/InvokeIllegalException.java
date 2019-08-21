// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.exceptions;


import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.UsefulUtilities;


/**
 Class ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException
 <p>
 
 @since 23.06.2019 (0:27) */
public class InvokeIllegalException extends IllegalStateException {
    
    
    private String message;
    
    public InvokeIllegalException(String message) {
        super(message);
        this.message = message;
    }
    
    public InvokeIllegalException() {
        this.message = "This functional is not ready yet.";
    }
    
    @Override
    public String getMessage() {
        System.out.println("ConstantsFor.thisPC() = " + UsefulUtilities.thisPC());
        System.out.println("InformationFactory.getRunningInformation() = " + UsefulUtilities.getRunningInformation());
        return message + " this is " + ConstantsFor.APPNAME_WITHMINUS + " :" + getStackTrace()[0];
    }
}