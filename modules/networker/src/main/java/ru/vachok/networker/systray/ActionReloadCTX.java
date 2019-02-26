package ru.vachok.networker.systray;


import ru.vachok.networker.ExitApp;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

/**
 Action on Reload Context button
 <p>

 @see ru.vachok.networker.IntoApplication
 @since 25.01.2019 (13:30) */
class ActionReloadCTX extends AbstractAction {

    private static final String[] ARGS = new String[0];

    @Override
    public void actionPerformed(ActionEvent e) {
        ExitApp exitApp = new ExitApp(getClass().getSimpleName());
        exitApp.reloadCTX();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ActionReloadCTX{");
        sb.append("ARGS=").append(Arrays.toString(ARGS));
        sb.append('}');
        return sb.toString();
    }
}
