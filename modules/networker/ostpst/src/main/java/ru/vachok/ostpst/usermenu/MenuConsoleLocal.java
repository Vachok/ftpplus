// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.usermenu;


import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.api.MessageToUser;
import ru.vachok.ostpst.api.MessengerOST;
import ru.vachok.ostpst.fileworks.FileChecker;
import ru.vachok.ostpst.fileworks.FileWorker;
import ru.vachok.ostpst.utils.CharsetEncoding;

import java.util.Scanner;


/**
 @since 14.05.2019 (9:20) */
public class MenuConsoleLocal implements UserMenu {
    
    
    private MessageToUser messageToUser;
    
    private String fileName;
    
    public MenuConsoleLocal(String fileName) {
        this.fileName = fileName;
        this.messageToUser = new MessengerOST(getClass().getSimpleName());
    }
    
    public MenuConsoleLocal() {
        this.fileName = null;
        this.messageToUser = new MessengerOST(getClass().getSimpleName());
    }
    
    public MessageToUser getMessageToUser() {
        return messageToUser;
    }
    
    public void setMessageToUser(MessageToUser messageToUser) {
        this.messageToUser = messageToUser;
    }
    
    @Override
    public void showMenu() {
        if (fileName == null) {
            System.out.println("Please, enter a filename: ");
            try (Scanner scanner = new Scanner(System.in)) {
                while (scanner.hasNextLine()) {
                    String nextLine = scanner.nextLine();
                    if (nextLine.equals("exit")) {
                        exitProgram(fileName);
                    }
                    if (nextLine.equals("yesterday")) {
                        System.out.println(new CharsetEncoding(ConstantsOst.DEFAULT, "UTF-8")
                            .getStrInAnotherCharset("I'm leaving yesterday, and you? Жду в среду на конец "));
                    }
                    else {
                        startMenu(nextLine);
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
    
    private void startMenu(String nextLine) {
        this.fileName = nextLine;
        FileWorker fileWorker = new FileChecker(fileName);
        String chkFileStr = fileWorker.chkFile();
        if (chkFileStr.contains("true")) {
            messageToUser.info(getClass().getSimpleName() + ".showMenu", "chkFileStr", " = " + chkFileStr);
            this.fileName = chkFileStr.split("Filename is: ")[1].split("\n")[0];
            MenuItems menuItems = new MenuItemsConsoleImpl(fileName);
            menuItems.askUser();
        }
        else {
            System.err.println("Incorrect file, please enter another filename, or type exit for exit: ");
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
