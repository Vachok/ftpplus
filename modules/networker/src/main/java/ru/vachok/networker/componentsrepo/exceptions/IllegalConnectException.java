package ru.vachok.networker.componentsrepo.exceptions;


import ru.vachok.networker.AbstractNetworkerFactory;


public class IllegalConnectException extends IllegalStateException {
    
    
    private AbstractNetworkerFactory abstractNetworkerFactory;
    
    public IllegalConnectException(AbstractNetworkerFactory abstractNetworkerFactory) {
        this.abstractNetworkerFactory = abstractNetworkerFactory;
    }
    
    @Override
    public String getMessage() {
        return AbstractNetworkerFactory.getInstance().toString();
    }
}
