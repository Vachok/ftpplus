// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.systray.actions;


import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.services.MessageLocal;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;


/**
 Action on Reload Context button
 <p>
 
 @see ru.vachok.networker.IntoApplication
 @since 25.01.2019 (13:30)
 */
public class ActionReloadCTX extends AbstractAction {
    
    private static final String[] ARGS = new String[0];
    
    /**
     {@link MessageLocal}
     */
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    @Override
    public void actionPerformed(ActionEvent e) {
        ConfigurableApplicationContext context = IntoApplication.reloadConfigurableApplicationContext();
        context.close();
        new IntoApplication().setConfigurableApplicationContext(SpringApplication.run(IntoApplication.class));
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ActionReloadCTX{");
        sb.append("ARGS=").append(Arrays.toString(ARGS));
        sb.append('}');
        return sb.toString();
    }
}
