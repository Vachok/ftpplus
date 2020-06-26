// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.exceptions;


import ru.vachok.networker.AbstractForms;


/**
 @since 27.06.2019 (10:45) */
public class InvokeEmptyMethodException extends IllegalStateException {
    
    
    private final String typeName;
    
    private final String methodName;
    
    public InvokeEmptyMethodException(String typeName, String methodName) {
        this.typeName = typeName;
        this.methodName = methodName + " see test units";
    }
    
    public InvokeEmptyMethodException(String typeName) {
        this.typeName = typeName;
        this.methodName = AbstractForms.fromArray(Thread.currentThread().getStackTrace());
    }
    
    @Override
    public String getMessage() {
        return "Invoked empty method: " + methodName + " in class " + typeName + "\n";
    }
}
