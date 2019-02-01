package ru.vachok.networker.systray;


import ru.vachok.messenger.MessageSwing;
import ru.vachok.networker.net.ScanOffline;
import ru.vachok.networker.net.ScanOnline;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 Класс запуска тестов
 <p>

 @since 28.01.2019 (1:21) */
public class ActionTests implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        new MessageSwing(500, 444, 20, 33).info(getClass().getSimpleName(), ScanOnline.class.getSimpleName(), ScanOnline.getI().toString());
        new MessageSwing(600, 666, 35, 49).info(getClass().getSimpleName(), ScanOffline.class.getSimpleName(), ScanOffline.getI().toString());
    }
}