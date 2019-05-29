package ru.vachok.ostpst.usermenu;


import com.pff.PSTException;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.MakeConvertOrCopy;
import ru.vachok.ostpst.fileworks.ConverterImpl;
import ru.vachok.ostpst.usermenu.traymenu.TrayMenu;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


public class AWTItemsImpl implements MenuItems {
    
    
    private final MessageToUser messageToUser = new MessageSwing();
    
    private String fileName;
    
    private String userInput;
    
    private TrayMenu trayMenu;
    
    private MakeConvertOrCopy makeConvertOrCopy;
    
    public AWTItemsImpl(String fileName, TrayMenu trayMenu) {
        this.fileName = fileName;
        this.trayMenu = trayMenu;
        initIcon();
    }
    
    public AWTItemsImpl(String fileName) {
        this.fileName = fileName;
        initIcon();
    }
    
    public void setTrayMenu(TrayMenu trayMenu) {
        this.trayMenu = trayMenu;
    }
    
    public void setUserInput(String text) {
        this.userInput = text;
        showSecondStage();
    }
    
    @Override public void askUser() {
        new MenuAWT().showMenu();
    }
    
    @Override public void showSecondStage() {
        Executors.newSingleThreadExecutor().execute(this::searchEverywhere);
    }
    
    private void initIcon() {
        Preferences preferences = Preferences.userRoot();
        preferences.put("ostfilename", fileName);
        try {
            preferences.sync();
        }
        catch (BackingStoreException e) {
            messageToUser.error(e.getMessage());
        }
        this.makeConvertOrCopy = new ConverterImpl(fileName);
    }
    
    private void searchEverywhere() {
        try {
            if (userInput.contains("@")) {
                makeConvertOrCopy.searchMessagesByEmails(userInput);
            }
            else {
                makeConvertOrCopy.searchMessages(userInput);
            }
            trayMenu.setSearching();
        }
        catch (PSTException | IOException e) {
            e.printStackTrace();
        }
        trayMenu.setDefault();
    }
}
