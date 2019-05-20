// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.usermenu;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.fileworks.FileChecker;
import ru.vachok.ostpst.fileworks.FileWorker;
import ru.vachok.ostpst.utils.CharsetEncoding;

import java.util.Scanner;


/**
 @since 14.05.2019 (9:20) */
public class MenuConsoleLocal implements UserMenu {
    
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    private String fileName;
    
    public MenuConsoleLocal(String fileName) {
        this.fileName = fileName;
    }
    
    public MenuConsoleLocal() {
        this.fileName = null;
    }
    
    @Override public void showMenu() {
        System.out.println("Please, enter a filename: ");
        if (fileName == null) {
            try (Scanner scanner = new Scanner(System.in)) {
                while (scanner.hasNextLine()) {
                    String nextLine = scanner.nextLine();
                    if (nextLine.equals("exit")) {
                        exitProgram(fileName);
                    }
                    else {
                        this.fileName = nextLine;
                        FileWorker fileWorker = new FileChecker(fileName);
                        String chkFileStr = fileWorker.chkFile();
                        if (chkFileStr.contains("true")) {
                            messageToUser.info(getClass().getSimpleName() + ".showMenu", "chkFileStr", " = " + chkFileStr);
                            this.fileName = chkFileStr.split("Filename is: ")[1].split("\n")[0];
                            MenuItems menuItems = new MenuItemsConsoleImpl(fileName);
                            menuItems.askUser();
                        }
                    }
                }
            }
            catch (RuntimeException e) {
                messageToUser.error(e.getMessage());
            }
        }
        else {
            callFromConstructor();
        }
    }
    
    private void callFromConstructor() {
        FileWorker fileWorker = new FileChecker(fileName);
        this.fileName = new CharsetEncoding(ConstantsOst.CP_WINDOWS_1251).getStrInAnotherCharset(fileName);
        String chkFileStr = fileWorker.chkFile();
        if (chkFileStr.contains("true")) {
            MenuItems menuItems = new MenuItemsConsoleImpl(fileName);
            menuItems.askUser();
        }
    }
    
    
    
}
