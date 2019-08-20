// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.htmlgen;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.net.scanner.NetScannerSvc;


public interface HTMLInfo extends HTMLGeneration {
    
    
    String fillWebModel();
    
    String fillAttribute(String attributeName);
    
    @Override
    default HTMLGeneration getInst() {
        return new NetScannerSvc();
    }
    
    @Override
    default String getFooter(@NotNull String aboutWhat) {
        return new PageGenerationHelper().getFooter(aboutWhat);
    }
    
    @Override
    void setClassOption(Object classOption);
    
    @Override
    default String getAsLink(String linkTo, String text) {
        return new PageGenerationHelper().getAsLink(linkTo, text);
    }
    
    @Override
    default String setColor(String color, String text) {
        return new PageGenerationHelper().setColor(color, text);
    }
}
