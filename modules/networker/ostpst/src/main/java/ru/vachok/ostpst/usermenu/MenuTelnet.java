package ru.vachok.ostpst.usermenu;


import ru.vachok.ostpst.utils.InProgressException;


/**
 @since 14.05.2019 (9:20) */
public class MenuTelnet implements UserMenu {
    
    
    @Override public void showMenu() {
        throw new InProgressException();
    }
}
