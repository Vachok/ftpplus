// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.exceptions;


/**
 @since 08.07.2019 (12:40) */
public class ScanFilesException extends IllegalStateException {
    
    
    private String msg;
    
    public ScanFilesException(String msg) {
        this.msg = msg;
    }
    
    @Override public String getMessage() {
        return msg;
    }
}