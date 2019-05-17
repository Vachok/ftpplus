// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.usermenu;


import org.testng.annotations.Test;
import ru.vachok.ostpst.ConstantsFor;
import ru.vachok.ostpst.utils.CharsetEncoding;


public class MenuConsoleLocalTest {
    
    
    @Test
    public void checkCons() {
        UserMenu userMenu = new MenuConsoleLocal(new CharsetEncoding(ConstantsFor.CP_WINDOWS_1251)
            .getStrInAnotherCharset("c:\\Users\\ikudryashov\\OneDrive\\Документы\\Файлы Outlook\\ksamarchenko@velkomfood.ru.ost"));
        userMenu.showMenu();
    }
    
}