package ru.vachok.ostpst.usermenu;


import org.testng.annotations.Test;
import ru.vachok.ostpst.utils.CharsetEncoding;


public class MenuConsoleLocalTest {
    
    
    @Test()
    public void checkCons() {
        UserMenu userMenu = new MenuConsoleLocal(new CharsetEncoding("windows-1251")
            .getStrInAnotherCharset("c:\\Users\\ikudryashov\\OneDrive\\Документы\\Файлы Outlook\\ksamarchenko@velkomfood.ru.ost"));
        userMenu.showMenu();
    }
    
}