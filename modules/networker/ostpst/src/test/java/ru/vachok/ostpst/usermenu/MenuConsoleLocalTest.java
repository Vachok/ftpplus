package ru.vachok.ostpst.usermenu;


import org.testng.annotations.Test;


public class MenuConsoleLocalTest {
    
    
    @Test()
    public void checkCons() {
        UserMenu userMenu = new MenuConsoleLocal("c:\\Users\\ikudryashov\\OneDrive\\Документы\\Файлы Outlook\\ksamarchenko@velkomfood.ru.ost");
        userMenu.showMenu();
    }
    
}