// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst;


import ru.vachok.ostpst.usermenu.*;


/**
 @since 29.04.2019 (11:24) */
public class OstToPstStart {
    
    
    public static void main(String[] args) {
        System.setProperty(ConstantsOst.STR_ENCODING, "UTF8");
        if (args != null && args.length > 0) {
            readArgs(args);
        }
        else {
            UserMenu userMenu = new MenuConsoleLocal();
            System.out.println("Hello. I will help you to work with MS Outlook mail databases.");
            userMenu.showMenu();
        }
    }
    
    private static void readArgs(String[] args) {
        String argF = "";
        MenuItems menuItems = new MenuItemsConsoleImpl(argF);
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            try {
                if (arg.toLowerCase().contains("-t")) {
                    new MenuTelnet().showMenu();
                }
                if (arg.toLowerCase().contains("-f")) {
                    argF = args[i + 1];
                    System.out.println("File: " + argF);
                }
                if (arg.toLowerCase().contains("-g")) {
                    menuItems = new AWTItemsImpl(argF);
                }
            }
            catch (ArrayIndexOutOfBoundsException e) {
                UserMenu userMenu = new MenuConsoleLocal();
                userMenu.showMenu();
            }
        }
        menuItems.askUser();
    }
}