package ru.vachok.ostpst;


/**
 @since 30.04.2019 (9:19) */
public interface MakeConvert {
    
    
    String convertToPST();
    
    void showFileContent();
    
    void setFileName(String fileName);
    
    long copyierWithSave();
}