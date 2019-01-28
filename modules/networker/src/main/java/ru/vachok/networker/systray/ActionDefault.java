package ru.vachok.networker.systray;


import org.slf4j.Logger;
import ru.vachok.networker.componentsrepo.AppComponents;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;

/**
 Default Tray Action
 <p>

 @see SystemTrayHelper
 @since 25.01.2019 (9:56) */
public class ActionDefault extends AbstractAction {

    private static final Logger LOGGER = AppComponents.getLogger();

    private String goTo;

    public ActionDefault(String goTo) {
        this.goTo = goTo;
    }

    ActionDefault() {
        this.goTo = "http://localhost:8880/serviceinfo";
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        LOGGER.warn("ActionDefault.actionPerformed");
        try {
            Desktop.getDesktop().browse(URI.create(goTo));
        } catch (IOException e1) {
            LOGGER.error(e1.getMessage(), e1);
        }
    }
}
