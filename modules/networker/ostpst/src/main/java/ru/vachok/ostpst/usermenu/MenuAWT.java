// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.usermenu;


import ru.vachok.ostpst.api.MessageToUser;
import ru.vachok.ostpst.api.MessengerOST;
import ru.vachok.ostpst.usermenu.traymenu.TrayMenu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.management.ManagementFactory;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 @since 20.05.2019 (14:50) */
public class MenuAWT implements UserMenu, Runnable {
    
    
    private MessageToUser messageToUser = new MessengerOST(getClass().getSimpleName());
    
    private String userInput;
    
    private String fileName;
    
    public MenuAWT() {
        this.fileName = Preferences.userRoot().get("ostfilename", "tmp_t.p.magdich.pst");
    }
    
    public MenuAWT(String fileName) {
        this.fileName = fileName;
        Preferences preferences = Preferences.userRoot();
        preferences.put("ostfilename", this.fileName);
        try {
            preferences.sync();
        }
        catch (BackingStoreException e) {
            messageToUser.error(e.getMessage());
        }
    }
    
    public String getFileName() {
        if (this.fileName == null) {
            return new MenuAWT().fileName;
        }
        else {
            return this.fileName;
        }
    }
    
    @Override public void run() {
        showMenu();
    }
    
    @Override public void showMenu() {
        getAWTMenu();
        Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(new TrayMenu(getFileName()));
    }
    
    private void getAWTMenu() {
        getFrame();
    }
    
    private JFrame getFrame() {
        JFrame jFrame = new JFrame();
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int frameX = (int) (screenSize.getWidth() / 3.8);
        int frameY = (int) (screenSize.getHeight() / 3.8);
        jFrame.setBounds(frameX, frameY, 320, 240);
        jFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        jFrame.setAlwaysOnTop(true);
        jFrame.setVisible(true);
        jFrame.add(getJPanel(jFrame));
        return jFrame;
    }
    
    private JPanel getJPanel(JFrame jFrame) {
        JPanel jPanel = new JPanel();
        JTextArea searchArea = new JTextArea("Search query");
        JButton okButton = new JButton("Search");
        JButton copyButton = new JButton("Copy to TMP");
        JButton cancelButton = new JButton("Cancel");
        jFrame.setTitle(Paths.get(fileName).toFile().getName());
        
        searchArea.setLineWrap(true);
        searchArea.setColumns(30);
        searchArea.addMouseListener(new MenuAWT.SetTextToNull(searchArea));
        okButton.addMouseListener(new OkButtonClick(searchArea, jFrame, okButton));
        copyButton.addMouseListener(new CopyButtonClick(searchArea, jFrame, copyButton));
        cancelButton.addMouseListener(new ExitButtonClick(cancelButton));
    
        jPanel.add(searchArea);
        jPanel.add(okButton);
        jPanel.add(copyButton);
        jPanel.add(cancelButton);
    
        jFrame.add(jPanel);
        jFrame.revalidate();
        return jPanel;
    }
    
    private class SetTextToNull extends MouseAdapter {
        
        
        private final JTextArea searchArea;
        
        public SetTextToNull(JTextArea searchArea) {
            this.searchArea = searchArea;
        }
        
        @Override public void mouseExited(MouseEvent e) {
            searchArea.setText(String.valueOf(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage()));
        }
        
        @Override public void mousePressed(MouseEvent e) {
            searchArea.setText("");
        }
    }
    
    
    
    private class OkButtonClick extends MouseAdapter {
        
        
        private final JTextArea searchArea;
        
        private final JFrame jFrame;
        
        private JButton okButton;
        
        public OkButtonClick(JTextArea searchArea, JFrame jFrame, JButton okButton) {
            this.searchArea = searchArea;
            this.jFrame = jFrame;
            this.okButton = okButton;
        }
        
        @Override public void mouseClicked(MouseEvent e) {
            TrayMenu trayMenu = new TrayMenu(getFileName());
            AWTItemsImpl awtItems = AWTItemsImpl.getAwtItems(fileName);
            awtItems.setTrayMenu(trayMenu);
            String areaText = searchArea.getText();
            userInput = areaText;
            awtItems.setUserInput(areaText, 1);
            Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(trayMenu);
            trayMenu.setSearching();
            jFrame.removeNotify();
        }
    }
    
    
    
    private class CopyButtonClick extends MouseAdapter {
        
        
        private final JTextArea searchArea;
        
        private final JFrame jFrame;
        
        private JButton jButton;
        
        public CopyButtonClick(JTextArea searchArea, JFrame jFrame, JButton jButton) {
            this.searchArea = searchArea;
            this.jFrame = jFrame;
            this.jButton = jButton;
        }
        
        @Override public void mouseClicked(MouseEvent e) {
            TrayMenu trayMenu = new TrayMenu(getFileName());
            AWTItemsImpl awtItems = AWTItemsImpl.getAwtItems(fileName);
            awtItems.setTrayMenu(trayMenu);
            String areaText = searchArea.getText();
            userInput = areaText;
            awtItems.setUserInput(areaText, 2);
            Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(trayMenu);
            trayMenu.setSearching();
            jFrame.removeNotify();
        }
        
        @Override public void mouseExited(MouseEvent e) {
            jButton.setBackground(Color.darkGray);
        }
        
        @Override public void mouseEntered(MouseEvent e) {
            jButton.setBackground(Color.GREEN);
        }
    }
    
    
    
    private class ExitButtonClick extends MouseAdapter {
        
        
        private JButton button;
        
        public ExitButtonClick(JButton button) {
            
            this.button = button;
        }
        
        @Override public void mouseClicked(MouseEvent e) {
            exitProgram(fileName);
        }
    }
}
