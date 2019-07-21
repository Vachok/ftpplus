package ru.vachok.networker.componentsrepo.exceptions;


import ru.vachok.networker.AbstractNetworkerFactory;

import java.text.MessageFormat;


public class IllegalConnectException extends IllegalStateException {
    
    
    private AbstractNetworkerFactory abstractNetworkerFactory;
    
    public IllegalConnectException(AbstractNetworkerFactory abstractNetworkerFactory) {
        this.abstractNetworkerFactory = abstractNetworkerFactory;
    }
    
    @Override
    public String getMessage() {
        return MessageFormat.format("Illegal connect! {1}\n{0}", abstractNetworkerFactory.getStatistics(), abstractNetworkerFactory.getCPU());
    }
}
