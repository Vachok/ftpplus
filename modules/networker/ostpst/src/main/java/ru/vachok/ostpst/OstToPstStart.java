// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst;


import ru.vachok.ostpst.fileworks.txtparse.InReader;
import ru.vachok.ostpst.usermenu.*;

import java.util.Arrays;


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
        final boolean isGraph = Arrays.toString(args).contains("-g");
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            try {
                if (arg.toLowerCase().contains("-t")) {
                    new MenuTelnet().showMenu();
                }
                else {
                    if (isGraph) {
                        menuItems = AWTItemsImpl.getAwtItems(argF);
                    }
                }
                if (arg.toLowerCase().contains("-f")) {
                    argF = args[i + 1];
                    System.out.println("File: " + argF);
                    if (isGraph) {
                        menuItems = AWTItemsImpl.getAwtItems(argF);
                    }
                }
                if (arg.toLowerCase().contains("-t")) {
                    new InReader(0).dozenReadFile();
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