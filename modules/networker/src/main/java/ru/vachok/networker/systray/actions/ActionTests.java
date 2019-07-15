// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.systray.actions;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 Класс запуска тестов

 @since 28.01.2019 (1:21) */
@SuppressWarnings("ClassWithoutLogger") public class ActionTests implements ActionListener {

    private MessageToUser messageToUser = new MessageLocal(ActionTests.class.getSimpleName());


    @Override
    public void actionPerformed(ActionEvent e1) {
        throw new UnsupportedOperationException("05.05.2019 (21:15)");
    }
}