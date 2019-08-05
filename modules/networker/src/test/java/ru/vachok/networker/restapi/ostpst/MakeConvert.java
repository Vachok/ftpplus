// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.ostpst;


public interface MakeConvert {
    
    
    void setFileName(java.lang.String fileName);
    
    String convertToPST();
    
    void showFileContent();
    
    long copyierWithSave();
    
}
