// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.htmlgen;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.ad.inet.AccessLogHTMLMaker;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.info.InformationFactory;


public interface HTMLGeneration {
    
    
    String MESSAGE_RU_ERROR_NULL = "Ничего нет или БД недоступна. Попробуйте ещё раз через какое-то время.";
    
    
    default String getHTMLCenterColor(String color, String text) {
        return UsefulUtilities.getHTMLCenterColor(color, text);
    }
    
    @Contract("_ -> new")
    static @NotNull HTMLGeneration getInstance(@NotNull String type) {
        if (InformationFactory.ACCESS_LOG_HTMLMAKER.equals(type)) {
            return new AccessLogHTMLMaker();
        }
        return new PageGenerationHelper();
    }
    
    String getFooter(@NotNull String aboutWhat);
    
    void setClassOption(Object classOption);
    
    String getAsLink(String linkTo, String text);
    
    String setColor(String color, String text);
}
