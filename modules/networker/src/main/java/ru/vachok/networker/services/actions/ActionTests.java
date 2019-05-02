// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services.actions;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.ostpst.MakeConvert;
import ru.vachok.ostpst.OstToPst;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


/**
 Класс запуска тестов

 @since 28.01.2019 (1:21) */
@SuppressWarnings("ClassWithoutLogger") public class ActionTests implements ActionListener {

    private MessageToUser messageToUser = new MessageLocal(ActionTests.class.getSimpleName());


    @Override
    public void actionPerformed(ActionEvent e1) {
        MakeConvert makeConvert = new OstToPst();
        MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
        makeConvert.setFileName("\\\\192.168.14.10\\IT-Backup\\Mailboxes_users\\yu.gukov.pst");
        makeConvert.copyierWithSave();
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("app.properties"));
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        properties.setProperty("file", new File("test.pst").getAbsolutePath());
        makeConvert.showFileContent();
    }
}