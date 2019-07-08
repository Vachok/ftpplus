package ru.vachok.networker.componentsrepo;


import ru.vachok.networker.TForms;


public class IllegalAnswerSSH extends IllegalStateException {
    
    
    private Object illegalObject;
    
    private Exception throwerEx;
    
    public IllegalAnswerSSH(Object illegalObject, Exception throwerEx) {
        this.illegalObject = illegalObject;
        this.throwerEx = throwerEx;
    }
    
    @Override public String getMessage() {
        return illegalObject.getClass().getTypeName() + " \n" + super.getMessage() + "\nStack thrower:\n" + new TForms().fromArray(throwerEx);
    }
}
