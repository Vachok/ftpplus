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
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

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
     {@link InetAddress} свчичей.
     */
    private List<String> swAddr;

    private Set<String> okIP = new HashSet<>();

    public SwitchesAvailability() {
        List<String> stringList = new ArrayList<>();
        try {
            stringList = DiapazonedScan.getInstance().pingSwitch();
        } catch (IllegalAccessException e) {
            LOGGER.error(getClass().getSimpleName(), e.getMessage(), e);
        }
        Collections.sort(stringList);
        this.swAddr = Collections.unmodifiableList(stringList);
    }

    Set<String> getOkIP() {
        return okIP;
    }

    private String okStr;

    private String badStr;

    @Override
    public void run() {
        LOGGER.warn("SwitchesAvailability.run");
        try {
            makeAddrQ();
        } catch (IOException e) {
            LOGGER.error(getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    private void makeAddrQ() throws IOException {
        Queue<InetAddress> inetAddressesQ = new ConcurrentLinkedQueue<>();
        for (String s : swAddr) {
            byte[] addressBytes = InetAddress.getByName(s).getAddress();
            InetAddress byAddress = InetAddress.getByAddress(addressBytes);
            inetAddressesQ.add(byAddress);
        }
        testAddresses(inetAddressesQ);
    }

    /**
     Проверяет пинги
     <p>

     @throws IOException если адрес недоступен.
     */
    private void testAddresses(Queue<InetAddress> inetAddressQueue) throws IOException {
        LOGGER.warn("SwitchesAvailability.testAddresses");
        List<String> badIP = new ArrayList<>();

        while (inetAddressQueue.iterator().hasNext()) {
            InetAddress poll = inetAddressQueue.poll();
            if (poll != null && poll.isReachable(500)) {
                okIP.add(poll.toString());
            } else {
                String ipStr = poll != null ? poll.toString() : null;
                badIP.add(ipStr);
                LOGGER.error(ipStr);
            }
        }
        this.okStr = new TForms().fromArray(okIP, false).replaceAll("/", "");
        this.badStr = new TForms().fromArray(badIP, false).replaceAll("/", "");
        writeToFile(okStr, badStr);
    }

    /**
     Запись в файл информации
     <p>
     Usages: {@link #testAddresses()} <br> Uses: 1.1 {@link TimeChecker#call()}

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
                .append("Online SwitchesWiFi: \n")
                .append(okIP)
                .append("\nOffline SwitchesWiFi: \n")
                .append(badIP).toString();
            outputStream.write(new String(toWrite.getBytes(), StandardCharsets.UTF_8).getBytes());
        } catch (IOException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SwitchesAvailability{");
        sb.append("\n Map=").append(new TForms().fromArray(DiapazonedScan.getNameAddr()));
        sb.append('}');
        return sb.toString();
    }
}
