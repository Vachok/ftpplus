// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.systray;



import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.net.DiapazonedScan;
import ru.vachok.networker.services.MessageLocal;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;


/**
 Класс запуска тестов

 @since 28.01.2019 (1:21) */
@SuppressWarnings("ClassWithoutLogger") public class ActionTests implements ActionListener {

    private MessageToUser messageToUser = new MessageLocal(ActionTests.class.getSimpleName());


    @Override
    public void actionPerformed(ActionEvent e) {
        DiapazonedScan instance = DiapazonedScan.getInstance();
        messageToUser.info(String.valueOf(new Date(instance.getStopClassStampLong())));
        instance.run();
        System.out.println(instance.toString());
    }
}