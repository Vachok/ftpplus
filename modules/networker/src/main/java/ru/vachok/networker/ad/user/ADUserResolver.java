package ru.vachok.networker.ad.user;


import ru.vachok.networker.componentsrepo.exceptions.TODOException;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;


/**
 @see ru.vachok.networker.ad.user.ADUserResolverTest
 @since 22.08.2019 (14:14) */
class ADUserResolver extends UserInfo {
    
    
    private Object classOption;
    
    @Override
    public Set<String> getPossibleVariantsOfPC(String userName, int resultsLimit) {
        Set<String> retSet = new ConcurrentSkipListSet<>();
        return retSet;
    }
    
    @Override
    public String getInfoAbout(String userName) {
        this.classOption=userName;
        return (String) classOption;
    }
    
    @Override
    public void setClassOption(Object classOption) {
        this.classOption = classOption;
    }
    
    @Override
    public String getInfo() {
        throw new TODOException("ru.vachok.networker.ad.user.ADUserResolver.getInfo created 22.08.2019 (14:13)");
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ADUserResolver{");
        sb.append("classOption=").append(classOption);
        sb.append('}');
        return sb.toString();
    }
}
