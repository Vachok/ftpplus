// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.exceptions;


import ru.vachok.networker.TForms;


/**
 Class ru.vachok.networker.componentsrepo.exceptions.ReminderException
 <p>
 
 @since 12.07.2019 (21:04) */
public class ReminderException extends Exception {
    
    
    private Throwable cause;
    
    private String whatNeedRemember;
    
    public ReminderException(String whatNeedRemember, Throwable cause) {
        this.cause = cause;
        this.whatNeedRemember = whatNeedRemember + new TForms().fromArray(cause);
    }
    
    public ReminderException(String whatNeedRemember) {
        this.whatNeedRemember = whatNeedRemember;
    }
    
    @Override
    public String getMessage() {
        return whatNeedRemember;
    }
}