// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.htmlgen;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.inet.AccessLogHTMLMaker;


public interface HTMLGeneration {
    
    
    default String getHTMLCenterColor(String color, String text) {
        return UsefulUtilities.getHTMLCenterColor(color, text);
    }
    
    @Contract("_ -> new")
    static @NotNull HTMLGeneration getInstance(@NotNull String type) {
        switch (type) {
            case InformationFactory.ACCESS_LOG:
                return new AccessLogHTMLMaker();
            default:
                return new PageGenerationHelper();
        }
    }
    
    String getFooter(@NotNull String aboutWhat);
    
    void setClassOption(Object classOption);
    
    String getAsLink(String linkTo, String text);
    
    String setColor(String color, String text);
}
