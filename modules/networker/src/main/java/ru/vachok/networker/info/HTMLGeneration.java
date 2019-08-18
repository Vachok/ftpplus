// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.UsefulUtilities;


public interface HTMLGeneration {
    
    
    default String getHTMLCenterColor(String color, String text) {
        return UsefulUtilities.getHTMLCenterColor(color, text);
    }
    
    PageGenerationHelper getInst();
    
    String getFooter(@NotNull String aboutWhat);
    
    void setClassOption(Object classOption);
    
    String getAsLink(String linkTo, String text);
    
    String setColor(String color, String text);
}
