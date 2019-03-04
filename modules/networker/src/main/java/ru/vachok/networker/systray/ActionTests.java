package ru.vachok.networker.systray;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 Класс запуска тестов
 <p>

 @since 28.01.2019 (1:21) */
public class ActionTests implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        IntoApplication.getConfigurableApplicationContext().stop();
        try{
            Thread.currentThread().sleep(500);
        }
        catch(InterruptedException e1){
            MessageToUser messageToUser = new MessageLocal();
            messageToUser.errorAlert("ActionTests", "actionPerformed", e1.getMessage());
            FileSystemWorker.error("ActionTests.actionPerformed", e1);
            IntoApplication.getConfigurableApplicationContext().start();
            Thread.currentThread().interrupt();
        }
        ;
        IntoApplication.getConfigurableApplicationContext().start();
    }
}