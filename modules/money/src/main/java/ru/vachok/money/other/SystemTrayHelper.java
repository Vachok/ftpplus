package ru.vachok.money.other;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.money.ConstantsFor;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;


/**
 @since 29.09.2018 (22:33) */
public class SystemTrayHelper {
    /*Fields*/
    private static InitProperties initProperties = new DBRegProperties(ConstantsFor.APP_NAME + SystemTrayHelper.class.getSimpleName());
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemTrayHelper.class.getSimpleName());

    private static Properties properties = new Properties();

    public void addTrayDefaultMinimum() {
        Properties properties = initProperties.getProps();
        SystemTray systemTray = SystemTray.getSystemTray();
        String defaultValue = "/static/images/icons8-монеты-15.png";
        if(ConstantsFor.localPc().equalsIgnoreCase("home")){
            defaultValue = "/static/images/icons8-скучающий-15.png";
        }
        Image image = Toolkit.getDefaultToolkit()
            .getImage(getClass()
                .getResource(properties.getProperty("icon", defaultValue)));
        PopupMenu popupMenu = popMenuSetter();
        TrayIcon trayIcon = new TrayIcon(image, "Money", popupMenu);
        try{
            if(SystemTray.isSupported()){
                systemTray.add(trayIcon);
            }
            else{
                LOGGER.warn("Tray not supported!");
                Thread.currentThread().interrupt();
            }
        }
        catch(AWTException e){
            LOGGER.warn(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        ActionListener actionListener = e -> {
            try{
                Desktop.getDesktop().browse(URI.create("http://localhost:8881"));
            }
            catch(IOException e1){
                LOGGER.error(e1.getMessage(), e1);
            }
        };
        trayIcon.addActionListener(actionListener);
    }

    private PopupMenu popMenuSetter() {
        PopupMenu popupMenu = new PopupMenu();
        MenuItem exitItem = new MenuItem();
        MenuItem openSysInfoPage = new MenuItem();
        MenuItem ideIdea = new MenuItem();
        MenuItem moneyItem = new MenuItem();
        MenuItem rebootSys = new MenuItem();
        MenuItem offSys = new MenuItem();

        ActionListener exitApp = e -> System.exit(0);
        exitItem.addActionListener(exitApp);
        exitItem.setLabel("Exit");
        openSysInfoPage.addActionListener(e -> {
            try{
                Desktop.getDesktop().browse(URI.create("http://localhost:8881/sysinfo"));
            }
            catch(IOException ex){
                LOGGER.error(ex.getMessage(), ex);
            }
        });
        openSysInfoPage.setLabel("Открыть System Info Page");
        ideIdea.addActionListener(e -> {
            try{
                String ideExe = "G:\\My_Proj\\.IdeaIC2017.3\\apps\\IDEA-C\\ch-0\\182.4505.22\\bin\\idea64.exe";
                if(ConstantsFor.localPc().equalsIgnoreCase("home")){
                    Runtime.getRuntime().exec(ideExe);
                }
                else{
                    Toolkit.getDefaultToolkit().beep();
                }
            }
            catch(IOException e1){
                LOGGER.error(e1.getMessage(), e1);
            }
        });
        ideIdea.setLabel("Запуск Idea");
        moneyItem.addActionListener(e -> {
            try{
                Desktop.getDesktop().browse(URI.create("http://localhost:8881/money"));
            }
            catch(IOException e1){
                LOGGER.error(e1.getMessage(), e1);
            }
        });
        moneyItem.setLabel("Считаем деньги");
        rebootSys.addActionListener(e -> {
            try{
                Runtime.getRuntime().exec("shutdown /r /f");
            }
            catch(IOException e1){
                LOGGER.error(e1.getMessage(), e1);
            }
        });
        rebootSys.setLabel("REBOOT THIS PC!");
        offSys.addActionListener(e -> {
            try{
                Runtime.getRuntime().exec("shutdown /p /f");
            }
            catch(IOException e1){
                LOGGER.error(e1.getMessage(), e1);
            }
        });
        offSys.setLabel("TURN OFF THIS PC!");

        popupMenu.add(ideIdea);
        popupMenu.add(moneyItem);
        popupMenu.add(openSysInfoPage);
        popupMenu.addSeparator();
        popupMenu.add(rebootSys);
        popupMenu.add(offSys);
        popupMenu.addSeparator();
        popupMenu.add(exitItem);
        return popupMenu;
    }
}