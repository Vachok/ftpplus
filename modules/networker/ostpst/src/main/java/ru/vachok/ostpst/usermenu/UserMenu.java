package ru.vachok.ostpst.usermenu;


import java.io.File;


/**
 @since 14.05.2019 (9:20) */
public interface UserMenu {
    
    
    void showMenu();
    
    default void exitProgram(String fileName) {
        System.out.println("setWritable = " + new File(fileName).setWritable(true));
        System.exit(222);
    }
}
