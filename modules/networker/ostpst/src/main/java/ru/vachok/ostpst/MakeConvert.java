package ru.vachok.ostpst;


import java.util.Deque;


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
    
    String copyierWithSave();
    
    String showListFolders();
    
    default void testMe() {
        System.out.println("true = " + true);
    }
    
    Deque<String> getDequeFolderNames();
    
    String clearCopy();
}
