// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.usermenu;


import java.io.File;


/**
 @since 14.05.2019 (9:20) */
public interface UserMenu {
    
    
    void showMenu();
    
    default void exitProgram(String fileName) {
        String ANSI_CLEAR_SEQ = "\u001b[2J";
        System.out.println(ANSI_CLEAR_SEQ);
        File workingFile = new File(fileName);
        System.out.println(workingFile.getAbsolutePath() + " setWritable = " + workingFile.setWritable(true));
        System.exit(222);
    }
}
