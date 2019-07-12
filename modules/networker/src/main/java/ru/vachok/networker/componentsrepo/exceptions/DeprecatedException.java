// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.exceptions;


import ru.vachok.networker.TForms;


/**
 Class ru.vachok.networker.componentsrepo.exceptions.DeprecatedException
 <p>
 
 @since 09.07.2019 (2:33) */
public class DeprecatedException extends IllegalStateException {
    
    
    private String deprecatedDescription;
    
    public DeprecatedException(String deprecatedDescription) {
        this.deprecatedDescription = deprecatedDescription;
    }
    
    public DeprecatedException(StackTraceElement[] stackTraceElements, String deprecatedDescription) {
        this.deprecatedDescription = deprecatedDescription + "\nSTACK:\n" + new TForms().fromArray(stackTraceElements);
    }
    
    @Override
    public String getMessage() {
        return deprecatedDescription;
    }
}