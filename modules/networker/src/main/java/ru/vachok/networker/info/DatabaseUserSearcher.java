// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import ru.vachok.networker.ad.PCUserNameResolver;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;


/**
 @since 16.08.2019 (10:32) */
public class DatabaseUserSearcher extends DatabaseInfo {
    
    
    private String aboutWhat;
    
    private InformationFactory informationFactory;
    
    public DatabaseUserSearcher(String userOrPc) {
        this.aboutWhat = userOrPc;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DatabaseUserSearcher{");
        sb.append("aboutWhat='").append(aboutWhat).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public String getInfo() {
        throw new TODOException("17.08.2019 (3:29)");
    }
    
    @Override
    public String getUserByPCNameFromDB(String userName) {
        this.informationFactory = InformationFactory.getInstance(InformationFactory.TYPE_PCINFO);
        return informationFactory.getInfoAbout(userName);
    }
    
    @Override
    public String getCurrentPCUsers(String pcName) {
        this.aboutWhat = pcName;
        return new PCUserNameResolver(aboutWhat).getInfo();
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.aboutWhat = aboutWhat;
        throw new TODOException("16.08.2019 (12:14)");
    }
}
