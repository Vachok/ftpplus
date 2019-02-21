package ru.vachok.networker.systray;


import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.componentsrepo.AppComponents;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.Executors;

/**
 Action on Reload Context button
 <p>

 @see ru.vachok.networker.IntoApplication
 @since 25.01.2019 (13:30) */
class ActionReloadCTX extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        IntoApplication.getConfigurableApplicationContext().close();
        ThreadPoolTaskExecutor executor = AppComponents.threadConfig().getTaskExecutor();
        executor.getThreadPoolExecutor().shutdown();
        AppComponents.threadConfig().killAll();
        Executors.unconfigurableExecutorService(Executors.newSingleThreadScheduledExecutor())
            .execute(() -> IntoApplication.main(new String[0]));
    }
}
