// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst;


import ru.vachok.ostpst.usermenu.MenuConsoleLocal;
import ru.vachok.ostpst.usermenu.MenuTelnet;
import ru.vachok.ostpst.usermenu.UserMenu;


/**
 @since 29.04.2019 (11:24) */
public class OstToPst {
    
    
    public static void main(String[] args) {
        UserMenu userMenu = new MenuConsoleLocal();
        
        if (args != null && args.length > 0) {
            for (String arg : args) {
                if (arg.toLowerCase().contains("-t")) {
                    userMenu = new MenuTelnet();
                }
            }
        }
        System.out.println("Hello. This is ost to pst converter.");
        userMenu.showMenu();
    }
}