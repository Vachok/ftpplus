package ru.vachok.ostpst.utils;


public class InProgressException extends IllegalStateException {
    
    
    @Override
    public String getMessage() {
        
        return "This method currently unavailable. In progress.";
    }
}
