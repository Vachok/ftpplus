package ru.vachok.networker.net;


import org.slf4j.Logger;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.services.TimeChecker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
     */
    private long startTime;

    /**
     {@link InetAddress} свчичей.
     */
    private List<InetAddress> swAddr = new ArrayList<>();

    private Set<String> okIP = new HashSet<>();

    Set<String> getOkIP() {
        return okIP;
    }

    private String okStr;

    private String badStr;

    @Override
    public void run() {
        LOGGER.warn("SwitchesAvailability.run");
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SwitchesAvailability{");
        sb.append(", okStr='").append(okStr).append('\'');
        sb.append(", startTime=").append(new Date(startTime));
        sb.append('}');
        return sb.toString();
    }

    /**
     Проверяет пинги
     <p>

     @throws IOException если адрес недоступен.
     */
    private void testAddresses() throws IOException {
        LOGGER.warn("SwitchesAvailability.testAddresses");
        List<String> badIP = new ArrayList<>();
        for (InetAddress ipSW : swAddr) {
            if (ipSW.isReachable(500)) {
                okIP.add(ipSW.toString());

            }
            else badIP.add(ipSW.toString());
        }
        Collections.sort(badIP);
        this.okStr = new TForms().fromArray(okIP, false).replaceAll("/", "");
        this.badStr = new TForms().fromArray(badIP, false).replaceAll("/", "");
        writeToFile(okStr, badStr);
    }

    /**
     Запись в файл информации
     <p>
     Usages: {@link #testAddresses()} <br>
     Uses: 1.1 {@link TimeChecker#call()}

     @param okIP  лист он-лайн адресов
     @param badIP лист офлайн адресов
     */
    private void writeToFile(String okIP, String badIP) {
        LOGGER.warn("SwitchesAvailability.writeToFile");
        File file = new File("sw.list.log");
        try (OutputStream outputStream = new FileOutputStream(file)) {
            String toWrite = new StringBuilder()
                .append(new TimeChecker().call().getMessage().toString())
                .append("\n\n")
                .append("Online Switches: \n")
                .append(okIP)
                .append("\nOffline Switches: \n")
                .append(badIP).toString();
            outputStream.write(new String(toWrite.getBytes(), StandardCharsets.UTF_8).getBytes());
        } catch (IOException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }
}
