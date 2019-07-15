// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.exceptions;


/**
 @since 27.06.2019 (10:45) */
public class InvokeEmptyMethodException extends IllegalStateException {
    
    
    private String typeName;
    
    private String methodName;
    
    public InvokeEmptyMethodException(String typeName, String methodName) {
        this.typeName = typeName;
        this.methodName = methodName;
    }
    
    public InvokeEmptyMethodException(String typeName) {
        this.typeName = typeName;
    }
    
    @Override public String getMessage() {
        return "Invoked empty method: " + methodName + " in class " + typeName + "\n";
    }
}
