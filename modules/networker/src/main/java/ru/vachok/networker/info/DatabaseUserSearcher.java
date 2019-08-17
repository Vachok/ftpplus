// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import ru.vachok.networker.ad.PCUserNameResolver;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;


/**
 @since 16.08.2019 (10:32) */
public class DatabaseUserSearcher extends DatabaseInfo {
    
    
    private String aboutWhat;
    
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
    public String getUserPCFromDB(String userName) {
        throw new TODOException("16.08.2019 (10:45)");
    }
    
    @Override
    public String getCurrentPCUsers(String pcName) {
        return new PCUserNameResolver(aboutWhat).getInfo();
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.aboutWhat = aboutWhat;
        throw new TODOException("16.08.2019 (12:14)");
    }
}
