package ru.vachok.networker.systray;


import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.componentsrepo.AppComponents;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.concurrent.Executors;

/**
 Action on Reload Context button
 <p>

 @see ru.vachok.networker.IntoApplication
 @since 25.01.2019 (13:30) */
class ActionReloadCTX extends AbstractAction {

    private static final String[] ARGS = new String[0];

    @Override
    public void actionPerformed(ActionEvent e) {
        IntoApplication.getConfigurableApplicationContext().close();
        ThreadPoolTaskExecutor executor = AppComponents.threadConfig().getTaskExecutor();
        executor.getThreadPoolExecutor().shutdown();
        ExitApp exitApp = new ExitApp(getClass().getSimpleName());
        exitApp.reloadCTX();
        Executors.unconfigurableExecutorService(Executors.newSingleThreadScheduledExecutor()).execute(() -> IntoApplication.main(ARGS));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ActionReloadCTX{");
        sb.append("ARGS=").append(Arrays.toString(ARGS));
        sb.append('}');
        return sb.toString();
    }
}
