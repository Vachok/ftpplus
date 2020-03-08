package ru.vachok.networker.componentsrepo.systray.actions;


import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.ad.common.Cleaner;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import javax.swing.*;
import java.awt.event.ActionEvent;


public class ActionCleanerOld extends AbstractAction {
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Cleaner cleaner = (Cleaner) IntoApplication.getConfigurableApplicationContext().getBean(Cleaner.class.getSimpleName());
        AppConfigurationLocal.getInstance().execute(cleaner);
    }
}
