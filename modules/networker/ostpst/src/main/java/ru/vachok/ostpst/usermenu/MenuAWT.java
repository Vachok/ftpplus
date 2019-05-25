package ru.vachok.ostpst.usermenu;


import javax.swing.*;


/**
 @since 20.05.2019 (14:50) */
public class MenuAWT implements UserMenu {
    
    
    @Override public void showMenu() {
        JFrame jFrame = new JFrame();
        JPanel jPanel = new JPanel();
        jFrame.add(jPanel);
        jFrame.setVisible(true);
        jPanel.revalidate();
    }
}
