// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.systray;


import ru.vachok.networker.net.DiapazonedScan;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 Класс запуска тестов

 @since 28.01.2019 (1:21) */
@SuppressWarnings("ClassWithoutLogger") public class ActionTests implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        DiapazonedScan.getInstance().run();
    }
}