package ru.vachok.networker.net;


import org.slf4j.Logger;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.ExitApp;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 Скан диапазона адресов

 @since 19.12.2018 (11:35) */
public class DiapazonedScan implements Runnable {

    private static final Logger LOGGER = AppComponents.getLogger();

    private static volatile DiapazonedScan ourInstance;


    private DiapazonedScan() {
    }

    public static DiapazonedScan getInstance() {
        ConstantsFor.showMem();
        if (ourInstance == null) {
            synchronized (DiapazonedScan.class) {
                if (ourInstance == null) {
                    ourInstance = new DiapazonedScan();
                }
            }
        }
        LOGGER.info("DiapazonedScan.getInstance");
        return ourInstance;
    }

    @Override
    public String toString() {
        LOGGER.info("DiapazonedScan.toString");
        final StringBuilder sb = new StringBuilder("DiapazonedScan{");
        sb.append("<a href=\"/showalldev\">ALL_DEVICES=").append(ConstantsFor.ALL_DEVICES.size()).append("/4591");
        sb.append("</a>}");
        return sb.toString();
    }

    @Override
    public void run() {
        LOGGER.warn("DiapazonedScan.run");
        try {
            scanAll();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     Добавляет в {@link #swAddr} адреса 10.200.200-217.254
     <p>

     @throws IOException если адрес недоступен.
     */
    @SuppressWarnings("all")
    private void scanAll() throws IOException {
        long stArt = System.currentTimeMillis();
        LOGGER.info("DiapazonedScan.scanAll");
        String avaPathStr = Paths.get(".").toFile().getCanonicalPath();
        Path logPath = Paths.get(avaPathStr + "\\modules\\networker\\src\\main\\resources\\static\\texts\\available.txt");
        if (!logPath.toFile().isFile()) logPath = Paths.get("available.txt");
        OutputStream outputStream = new FileOutputStream(logPath.toFile());
        PrintWriter printWriter = new PrintWriter(outputStream, true);
        for (int i = 200; i < 218; i++) {
            for (int j = 0; j < 255; j++) {
                byte[] aBytes = InetAddress.getByName("10.200." + i + "." + j).getAddress();
                InetAddress byAddress = InetAddress.getByAddress(aBytes);

                if (byAddress.isReachable(200)) {
                    printWriter.println(byAddress.getHostName() + " " + byAddress.getHostAddress());
                    ConstantsFor.ALL_DEVICES.add("<font color=\"green\">" + byAddress.toString() + "</font><br>");
                } else ConstantsFor.ALL_DEVICES.add("<font color=\"red\">" + byAddress.toString() + "</font><br>");
            }
        }
        printWriter.close();
        outputStream.close();
        Runtime.getRuntime().addShutdownHook(new ExitApp(this.getClass().getSimpleName(), "scanAll"));
        String msg = "\nTime spend: " + TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - stArt);
        ConstantsFor.ALL_DEVICES.add(msg);
        LOGGER.warn(msg);
    }

    List<String> pingSwitch() throws IllegalAccessException {
        LOGGER.info("DiapazonedScan.pingSwitch");
        StringBuilder stringBuilder = new StringBuilder();
        Field[] swFields = Switches.class.getFields();
        List<String> swList = new ArrayList<>();
        for (Field swF : swFields) {
            swList.add("\n" + swF.get(swF).toString());
        }
        swList.forEach(stringBuilder::append);
        String retMe = stringBuilder.toString();
        LOGGER.warn(retMe);
        return swList;
    }


}
