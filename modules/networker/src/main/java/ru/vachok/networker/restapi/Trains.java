package ru.vachok.networker.restapi;


import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.tutu.conf.BackEngine;
import ru.vachok.tutu.parser.SiteParser;


public class Trains implements InformationFactory {
    
    
    @Override
    public String getInfo() {
        BackEngine backEngine = new SiteParser();
        return AbstractForms.fromArray(backEngine.getComingTrains());
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
