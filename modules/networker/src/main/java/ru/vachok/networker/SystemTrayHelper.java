package ru.vachok.networker;


import org.slf4j.Logger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.net.MyServer;
import ru.vachok.networker.services.ArchivesAutoCleaner;
import ru.vachok.networker.services.DBMessenger;
import ru.vachok.networker.services.Putty;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.time.Year;
import java.util.concurrent.*;

import static java.lang.System.*;


/**
 @since 29.09.2018 (22:33) */
public class SystemTrayHelper {

    /*Fields*/
    /**
     Путь к папке с рисунками
     */
    private static final String IMG_FOLDER_NAME = "/static/images/";

    private static final Logger LOGGER = AppComponents.getLogger();

    private static final String THIS_PC = ConstantsFor.thisPC();

    private static SystemTrayHelper systemTrayHelper = new SystemTrayHelper();

    private static MessageToUser messageToUser = new DBMessenger();

    public static SystemTrayHelper getInstance() {
        return systemTrayHelper;
    }

    /*Instances*/
    private SystemTrayHelper() {
    }

    static void addTray(String iconFileName) {
        boolean myPC;
        myPC = THIS_PC.toLowerCase().contains("no0027") || THIS_PC.equalsIgnoreCase("home");
        if(iconFileName==null){
            iconFileName = "icons8-ip-адрес-15.png";
        }
        else{
            if(myPC){
                iconFileName = "icons8-плохие-поросята-48.png";
            }
        }
        if(!srvGitIs()){
            iconFileName = "icons8-отменить-2-20.png";
        }
        iconFileName = IMG_FOLDER_NAME + iconFileName;
        Image image = Toolkit.getDefaultToolkit().getImage(SystemTrayHelper.class.getResource(iconFileName));
        PopupMenu popupMenu = new PopupMenu();
        MenuItem defItem = new MenuItem();
        TrayIcon trayIcon = new TrayIcon(image, AppComponents.versionInfo().getAppBuild() + " v. " +
            AppComponents.versionInfo().getAppVersion() + " " + AppComponents.versionInfo().getBuildTime(), popupMenu);

        ActionListener actionListener = e -> {
            try{
                Desktop.getDesktop().browse(URI.create("http://localhost:8880"));
            }
            catch(IOException e1){
                LOGGER.error(e1.getMessage(), e1);
            }
        };
        ActionListener exitApp = e -> {
            ConstantsFor.saveProps(ConstantsFor.getPROPS());
            IntoApplication.delTemp();
            exit(0);
        };
        addItems(popupMenu);
        trayIcon.setImageAutoSize(true);
        defItem.setLabel("Exit");
        defItem.addActionListener(exitApp);
        popupMenu.add(defItem);
        trayIcon.addActionListener(actionListener);
        try{
            if(SystemTray.isSupported()){
                SystemTray systemTray = SystemTray.getSystemTray();
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
    }

    private static boolean srvGitIs() {
        try{
            return InetAddress.getByName("srv-git.eatmeat.ru").isReachable(1000);
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    private static void addItems(PopupMenu popupMenu) {
        ThreadConfig threadConfig = new ThreadConfig();
        ThreadPoolTaskExecutor executor = threadConfig.threadPoolTaskExecutor();
        Thread thread = executor.createThread(SystemTrayHelper::recOn);
        thread.start();
        MenuItem gitStartWeb = new MenuItem();
        gitStartWeb.addActionListener(actionEvent -> {
            Callable<String> sshStr = () -> new SSHFactory.Builder(ConstantsFor
                .SRV_GIT, "sudo git instaweb;" +
                "sudo cd /usr/home/dpetrov/;" +
                "sudo git instaweb -p 11111;" +
                "sudo cd /usr/home/kudr/;" +
                "sudo git instaweb -p 9999;" +
                "exit").build().call();
            Future<String> submit = executor.submit(sshStr);
            try{
                LOGGER.info(submit.get(30, TimeUnit.SECONDS));
            }
            catch(InterruptedException | ExecutionException | TimeoutException e){
                Thread.currentThread().interrupt();
            }
        });
        gitStartWeb.setLabel("GIT WEB ON");
        popupMenu.add(gitStartWeb);

        MenuItem toConsole = new MenuItem();
        toConsole.setLabel("Console Back");
        toConsole.addActionListener(e -> System.setOut(err));
        popupMenu.add(toConsole);

        MenuItem puttyStarter = new MenuItem();
        puttyStarter.addActionListener(e -> new Putty().start());
        puttyStarter.setLabel("Putty");
        popupMenu.add(puttyStarter);

        MenuItem delFiles = new MenuItem();
        delFiles.addActionListener(e -> {
            executor.setThreadGroup(new ThreadGroup(("CLR")));
            executor.setThreadNamePrefix("CLEAN");
            executor.setThreadGroupName("12-17");
            int startYear = Integer.parseInt(ConstantsFor.getPROPS().getOrDefault("startyear", (Year.now().getValue() - 6)).toString());
            for(int i = startYear; i < startYear + 5; i++){
                String msg = ("starting clean for " + i).toUpperCase();
                LOGGER.info(msg);
                executor.setThreadNamePrefix(i + " ");
                Thread executorThread = executor.createThread(new ArchivesAutoCleaner(i));
                executorThread.setDaemon(true);
                executor.submit(executorThread);
            }
        });
        delFiles.setLabel("Autoclean");
        popupMenu.add(delFiles);
    }

    /**
     Reconnect Socket, пока он открыт.
     */
    private static void recOn() {
        String bSTR = ConstantsFor.checkDay() + " pcuserauto truncated";
        LOGGER.warn(bSTR);
        MyServer.setSocket(new Socket());
        while(!MyServer.getSocket().isClosed()){
            try{
                MyServer.reconSock();
            } catch(IOException | InterruptedException | NullPointerException e1){
                messageToUser.errorAlert(SystemTrayHelper.class.getSimpleName(), e1.getMessage(), new TForms().fromArray(e1, false));
                new ThreadConfig().threadPoolTaskExecutor().execute(MyServer.getI());
                Thread.currentThread().interrupt();
            }
        }
    }
}
