// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.usermenu;


import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.usermenu.traymenu.TrayMenu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 @since 20.05.2019 (14:50) */
public class MenuAWT implements UserMenu, Runnable {
    
    
    private MessageToUser messageToUser = new MessageSwing();
    
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
        Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(new TrayMenu(getFileName()));
    }
    
    @Override public void showMenu() {
        getAWTMenu();
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
        searchArea.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                searchArea.setText("");
            }
        });
        
        okButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                TrayMenu trayMenu = new TrayMenu(getFileName());
                AWTItemsImpl awtItems = new AWTItemsImpl(fileName, trayMenu);
                String areaText = searchArea.getText();
                userInput = areaText;
                awtItems.setUserInput(areaText, 1);
                Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(trayMenu);
                trayMenu.setSearching();
                jFrame.removeNotify();
            }
        });
        copyButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                TrayMenu trayMenu = new TrayMenu(getFileName());
                AWTItemsImpl awtItems = new AWTItemsImpl(fileName, trayMenu);
                String areaText = searchArea.getText();
                userInput = areaText;
                awtItems.setUserInput(areaText, 2);
                Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(trayMenu);
                trayMenu.setSearching();
                jFrame.removeNotify();
            }
        });
        
        cancelButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                exitProgram(fileName);
            }
        });
        
        jPanel.add(searchArea);
        jPanel.add(okButton);
        jPanel.add(copyButton);
        jPanel.add(cancelButton);
        jFrame.add(jPanel);
        jFrame.revalidate();
        return jPanel;
    }
}
