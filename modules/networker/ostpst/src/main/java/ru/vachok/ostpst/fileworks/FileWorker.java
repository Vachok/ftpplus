package ru.vachok.ostpst.fileworks;


/**
 @since 16.05.2019 (11:48) */
public interface FileWorker {
    
    
    String chkFile();
    
    String clearCopy();
    
    long continuousCopy();
    
    void showCurrentResult();
    
    String saveAndExit();
    
    boolean processNewCopy();
}
