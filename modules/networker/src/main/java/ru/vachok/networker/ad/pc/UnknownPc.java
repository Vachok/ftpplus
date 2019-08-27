package ru.vachok.networker.ad.pc;


import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;


/**
 @since 27.08.2019 (11:04) */
class UnknownPc extends PCInfo {
    
    
    private static final String NO_EXISTS = "No pc exists";
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        return NO_EXISTS;
    }
    
    @Override
    public void setClassOption(Object classOption) {
        throw new InvokeIllegalException("26.08.2019 (22:39)");
    }
    
    @Override
    public String getInfo() {
        return NO_EXISTS;
    }
}
