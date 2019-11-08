package ru.vachok.networker.componentsrepo;


import ru.vachok.networker.AbstractForms;


public class NetworkerStopException extends Exception {
    
    
    @Override
    public String getMessage() {
        return AbstractForms.fromArray(this);
    }
}
