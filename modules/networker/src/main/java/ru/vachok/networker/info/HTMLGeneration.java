package ru.vachok.networker.info;


import ru.vachok.networker.UsefulUtilities;


public interface HTMLGeneration extends InformationFactory {
    
    
    default String getHTMLCenterColor(String color, String text) {
        return UsefulUtilities.getHTMLCenterColor(color, text);
    }
    
    String getAsLink(String linkTo, String text);
    
    String setColor(String color, String text);
}
