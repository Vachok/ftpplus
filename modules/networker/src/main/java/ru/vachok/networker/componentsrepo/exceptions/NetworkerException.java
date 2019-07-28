package ru.vachok.networker.componentsrepo.exceptions;


/**
 @since 25.07.2019 (13:37) */
public class NetworkerException extends IllegalStateException {
    
    
    private String errMsg;
    
    public NetworkerException(String errMsg) {
        this.errMsg = errMsg;
    }
    
    @Override
    public String getMessage() {
        return errMsg + "\n" + super.getMessage();
    }
}
