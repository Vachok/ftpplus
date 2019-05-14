package ru.vachok.ostpst;


/**
 @since 30.04.2019 (9:19) */
public interface MakeConvert {
    
    
    String convertToPST();
    
    void saveFolders();
    
    /**
     @param csvFileName can be null
     @return path to saved csv
     */
    String saveContacts(String csvFileName);
    
    void setFileName(String fileName);
    
    long copyierWithSave();
    
    String folderContentItemsString();
    
    default void testMe() {
        System.out.println("true = " + true);
    }
}
