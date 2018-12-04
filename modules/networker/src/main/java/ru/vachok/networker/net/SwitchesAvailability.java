package ru.vachok.networker.net;


import org.slf4j.Logger;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 Проверка свичей в локальной сети.
 <p>

 @since 04.12.2018 (9:23) */
public class SwitchesAvailability implements Runnable {

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    /**
     Время старта класса.
     <p>
     */
    private long startTime;

    /**
     {@link InetAddress} свчичей.
     */
    private List<InetAddress> swAddr = new ArrayList<>();

    @Override
    public void run() {
        LOGGER.info("SwitchesAvailability.run");
        this.swAddr.clear();
        this.startTime = System.currentTimeMillis();
        try {
            InetAddress ipName = InetAddress.getByName("10.200.200.1");
            byte[] bytes = ipName.getAddress();
            swAddr.add(InetAddress.getByAddress(bytes));
            addFirst();
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
        }
    }

    /**
     Добавляет в {@link #swAddr} адреса 10.200.200-217.254
     <p>

     @throws IOException если адрес недоступен.
     */
    private void addFirst() throws IOException {
        LOGGER.info("SwitchesAvailability.addFirst");
        for (int i = 200; i < 218; i++) {
            try {
                InetAddress ipName = InetAddress.getByName("10.200." + i + ".254");
                byte[] ipBytes = ipName.getAddress();
                swAddr.add(InetAddress.getByAddress(ipBytes));
            } catch (UnknownHostException e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }
        addSecond();
    }

    /**
     Добавляет в {@link #swAddr} адреса 10.200.*.250-253
     <p>

     @throws IOException если адрес недоступен.
     */
    private void addSecond() throws IOException {
        LOGGER.info("SwitchesAvailability.addSecond");
        List<String[]> addrList = new ArrayList<>();
        swAddr.forEach(x -> {
            String s = x.toString();
            s = s.replaceAll("/", "");
            String[] split = s.split("\\Q.\\E");
            addrList.add(split);
        });
        addrList.forEach(x -> {
            for (int i = 250; i < 254; i++) {
                try {
                    InetAddress ipAd = InetAddress.getByName(x[0] + "." + x[1] + "." + x[2] + "." + i);
                    byte[] ipBytes = ipAd.getAddress();
                    swAddr.add(InetAddress.getByAddress(ipBytes));
                } catch (UnknownHostException e) {
                    LOGGER.warn(e.getMessage());
                }
            }
        });
        testAddresses();
    }

    /**
     Проверяет пинги
     <p>

     @throws IOException если адрес недоступен.
     */
    private void testAddresses() throws IOException {
        LOGGER.info("SwitchesAvailability.testAddresses");
        List<String> okIP = new ArrayList<>();
        List<String> badIP = new ArrayList<>();
        for (InetAddress ipSW : swAddr) {
            if (ipSW.isReachable(500)) okIP.add(ipSW.toString());
            else badIP.add(ipSW.toString());
        }
        Collections.sort(okIP);
        Collections.sort(badIP);
        String okStr = new TForms().fromArray(okIP, false).replaceAll("/", "");
        String badStr = new TForms().fromArray(badIP, false).replaceAll("/", "");
        LOGGER.info(okStr);
        LOGGER.warn(badStr);
        String msg = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime) + " sec spend";
        LOGGER.warn(msg);
    }
}
