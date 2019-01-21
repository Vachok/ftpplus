package ru.vachok.networker.services;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.SystemTrayHelper;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.awt.*;
import java.util.Collections;


/**
 Сообщения с учётом "локальных" особенностей
 <p>

 @since 21.01.2019 (23:46) */
public class MessageToTray implements MessageToUser {

    @Override
    public void errorAlert(String s, String s1, String s2) {
        if(SystemTray.isSupported()){
            SystemTrayHelper.getTrayIcon().displayMessage(s, s1 + " " + s2, TrayIcon.MessageType.ERROR);
        }
        else{
            new MessageCons().errorAlert(s, s1, s2);
        }
        FileSystemWorker.recFile(s + ConstantsFor.LOG, Collections.singletonList((s1 + "\n\n" + s2)));
    }

    @Override
    public void info(String s, String s1, String s2) {
        if(SystemTray.isSupported()){
            SystemTrayHelper.getTrayIcon().displayMessage(s, s1 + " " + s2, TrayIcon.MessageType.INFO);
        }
        else{
            new MessageCons().info(s, s1, s2);
        }
    }

    @Override
    public void infoNoTitles(String s) {
        if(SystemTray.isSupported()){
            SystemTrayHelper.getTrayIcon().displayMessage("FYI", s, TrayIcon.MessageType.INFO);
        }
        else{
            new MessageCons().infoNoTitles(s);
        }
    }

    @Override
    public void infoTimer(int i, String s) {
        throw new UnsupportedOperationException("Not impl to " + getClass().getSimpleName());
    }

    @Override
    public String confirm(String s, String s1, String s2) {
        throw new UnsupportedOperationException("Not impl to " + getClass().getSimpleName());

    }
}