// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.usermenu;


import com.pff.PSTException;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.MakeConvertOrCopy;
import ru.vachok.ostpst.fileworks.ConverterImpl;
import ru.vachok.ostpst.fileworks.FileWorker;
import ru.vachok.ostpst.fileworks.nopst.Downloader;
import ru.vachok.ostpst.fileworks.nopst.Uploader;
import ru.vachok.ostpst.usermenu.traymenu.TrayMenu;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


public class AWTItemsImpl implements MenuItems {
    
    
    private final MessageToUser messageToUser = new MessageSwing();
    
    private static String fileName;
    
    private static String userInput;
    
    private static AWTItemsImpl awtItems = new AWTItemsImpl();
    
    private AWTItemsImpl() {
        initIcon();
    }
    
    public static String getFileName() {
        return fileName;
    }
    
    private TrayMenu trayMenu;
    
    public static String getUserInput() {
        return userInput;
    }
    
    public static void setUserInput(String userInput) {
        AWTItemsImpl.userInput = userInput;
    }
    
    public static AWTItemsImpl getAwtItems(String fileName) {
        AWTItemsImpl.fileName = fileName;
        return awtItems;
    }
    
    private MakeConvertOrCopy makeConvertOrCopy;
    
    public static AWTItemsImpl getI() {
        return getAwtItems(fileName);
    }
    
    public String getCopyStats(String readFileName) {
        AWTItemsImpl items = AWTItemsImpl.getAwtItems(readFileName);
        FileWorker upl = new Uploader(AWTItemsImpl.getFileName());
        return upl.toString();
    }
    
    public void setTrayMenu(TrayMenu trayMenu) {
        this.trayMenu = trayMenu;
    }
    
    public void setUserInput(String text, int methodToLaunch) {
        if (methodToLaunch == 1) {
            Executors.newSingleThreadExecutor().execute(this::searchEverywhere);
        }
        if (methodToLaunch == 2) {
            Executors.newSingleThreadExecutor().execute(this::copyFile);
        }
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("AWTItemsImpl{");
        sb.append(", fileName='").append(fileName).append('\'');
        sb.append(", userInput='").append(userInput).append('\'');
        sb.append(", makeConvertOrCopy=").append(makeConvertOrCopy.getClass().getSimpleName());
        sb.append('}');
        return sb.toString();
    }
    
    @Override public void showSecondStage() {
        throw new IllegalComponentStateException("29.05.2019 (21:27)");
    }
    
    @Override public void askUser() {
        new MenuAWT().showMenu();
    }
    
    private void copyFile() {
        Downloader downloader = new Downloader(fileName, "tmp_" + Paths.get(fileName).toAbsolutePath().normalize().getFileName());
        downloader.setBufLen(ConstantsOst.KBYTE_BYTES * ConstantsOst.KBYTE_BYTES * 42);
        downloader.continuousCopy();
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
