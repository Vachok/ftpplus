// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad;


import org.jetbrains.annotations.Contract;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.info.InformationFactory;


/**
 @see ru.vachok.networker.ad.PCUserNameHTMLResolverTest
 @since 02.10.2018 (17:32) */
public class PCUserNameHTMLResolver {
    
    
    
    
    private String pcName;
    
    private InformationFactory informationFactory;
    
    @Contract(pure = true)
    public PCUserNameHTMLResolver(InformationFactory informationFactory) {
        this.informationFactory = informationFactory;
    }
    
    public PCUserNameHTMLResolver(String aboutWhat) {
        this.pcName = aboutWhat;
    }
    
    public String getInfoAbout(String aboutWhat) {
        this.pcName = aboutWhat;
        throw new InvokeIllegalException("21.08.2019 (11:22) DEPRECATED. FOR REMOVAL");
    }
    
    
    public void setClassOption(Object classOption) {
        this.pcName = (String) classOption;
    }
    
    public String getInfo() {
        this.informationFactory = InformationFactory.getInstance(InformationFactory.INET_USAGE);
        return informationFactory.getInfoAbout(pcName);
    }
}
