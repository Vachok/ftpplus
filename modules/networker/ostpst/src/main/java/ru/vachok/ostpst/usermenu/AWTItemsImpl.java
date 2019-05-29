// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.usermenu;


import com.pff.PSTException;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.MakeConvertOrCopy;
import ru.vachok.ostpst.fileworks.ConverterImpl;
import ru.vachok.ostpst.fileworks.nopst.HardCopy;
import ru.vachok.ostpst.usermenu.traymenu.TrayMenu;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Paths;
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
    
    public void setUserInput(String text, int methodToLaunch) {
        this.userInput = text;
        if (methodToLaunch == 1) {
            Executors.newSingleThreadExecutor().execute(this::searchEverywhere);
        }
        if (methodToLaunch == 2) {
            Executors.newSingleThreadExecutor().execute(this::copyFile);
        }
    }
    
    @Override public void showSecondStage() {
        throw new IllegalComponentStateException("29.05.2019 (21:27)");
    }
    
    @Override public void askUser() {
        new MenuAWT().showMenu();
    }
    
    private void copyFile() {
        HardCopy hardCopy = new HardCopy(fileName, "tmp_" + Paths.get(fileName).toAbsolutePath().normalize().getFileName().toString());
        hardCopy.setBufLen(ConstantsOst.KBYTE_BYTES * ConstantsOst.KBYTE_BYTES * 42);
        hardCopy.continuousCopy();
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
            makeConvertOrCopy.searchMessages(userInput);
            trayMenu.setSearching();
        }
        catch (PSTException | IOException e) {
            e.printStackTrace();
        }
        trayMenu.setDefault();
    }
}
