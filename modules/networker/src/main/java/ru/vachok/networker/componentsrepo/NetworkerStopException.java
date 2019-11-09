package ru.vachok.networker.componentsrepo;


import java.text.MessageFormat;


public class NetworkerStopException extends Exception {
    
    
    private String classThrower;
    
    private String methodThrower;
    
    private int lineNum;
    
    public NetworkerStopException(String classThrower, String methodThrower, int lineNum) {
        this.classThrower = classThrower;
        this.methodThrower = methodThrower;
        this.lineNum = lineNum;
    }
    
    @Override
    public String getMessage() {
        return MessageFormat.format("{0}.{1} throwed on line {2} NetworkerStopException", classThrower, methodThrower, lineNum);
    }
}
