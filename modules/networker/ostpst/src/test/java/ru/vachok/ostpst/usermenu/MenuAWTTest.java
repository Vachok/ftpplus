package ru.vachok.ostpst.usermenu;


import org.testng.annotations.Test;


public class MenuAWTTest {
    
    
    @Test
    public void graphMenu() {
        UserMenu userMenu = new MenuAWT();
        userMenu.showMenu();
    }
}