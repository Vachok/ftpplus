package ru.vachok.networker.info;


import ru.vachok.networker.UsefulUtilities;


public interface HTMLGeneration extends InformationFactory {
    
    
    default String getHTMLCenterColor(String centerRedColorHTML, String color) {
        return UsefulUtilities.getHTMLCenterColor(centerRedColorHTML, color);
    }
    
    String getAsLink(String text, String linkTo);
}
