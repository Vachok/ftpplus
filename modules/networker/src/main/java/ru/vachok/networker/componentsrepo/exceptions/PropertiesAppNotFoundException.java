package ru.vachok.networker.componentsrepo.exceptions;


import java.text.MessageFormat;


public class PropertiesAppNotFoundException extends IllegalStateException {
    
    
    int propsSize;
    
    public PropertiesAppNotFoundException(int propsSize) {
        this.propsSize = propsSize;
    }
    
    @Override
    public String getMessage() {
        return MessageFormat.format("Application properties have {0} items.", propsSize);
    }
}
