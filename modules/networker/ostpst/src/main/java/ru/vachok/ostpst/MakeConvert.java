// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst;


import java.io.IOException;
import java.util.Deque;
import java.util.List;


/**
 @since 30.04.2019 (9:19) */
public interface MakeConvert {
    
    
    String convertToPST();
    
    String saveFolders() throws IOException;
    
    /**
     @param csvFileName can be null
     @return path to saved csv
     */
    String saveContacts(String csvFileName);
    
    void setFileName(String fileName);
    
    String copyierWithSave(String newCP);
    
    String cleanPreviousCopy();
    
    String showListFolders();
    
    default void testMe() {
        System.out.println("true = " + true);
    }
    
    Deque<String> getDequeFolderNamesAndWriteToDisk() throws IOException;
    
    String showContacts();
    
    String getObjectItemsByID(long id);
    
    List<String> getListMessagesSubjectWithID(long folderID);
}
