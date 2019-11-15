package ru.vachok.networker.restapi;


import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.info.InformationFactory;


public class Trains implements InformationFactory {
    
    
    @Override
    public String getInfo() {
        throw new TODOException("15.11.2019 (18:42)");
    }
    
    @Override
    public void setClassOption(Object option) {
        throw new TODOException("just do it!");
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        throw new TODOException("null");
        
    }
}
