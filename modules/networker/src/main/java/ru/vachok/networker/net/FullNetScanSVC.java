package ru.vachok.networker.net;


import org.slf4j.Logger;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 Полный сетевой ip-сканер

 @since 12.11.2018 (15:49) */
public class FullNetScanSVC implements Runnable {

    private static final Logger LOGGER = AppComponents.getLogger();

    private List<String> addressList = new ArrayList<>();

    @Override
    public void run() {
        ipScanner();
    }

    private void ipScanner() {
        String lan10 = "10.10.";
        String lan200 = "10.200.";
        String lan192 = "192.168.";
        String lan172 = "172.";

        ipBuild(lan192);
    }

    private void ipBuild(String vLAN) {
        List<String> threeOctets = new ArrayList<>();
        for (int i = 10; i < 50; i++) {
            threeOctets.add(vLAN + i);
        }
        buildOct4(threeOctets);
    }

    private void buildOct4(List<String> threeOctets) {
        threeOctets.forEach(x -> {
            for (int i = 0; i < 255; i++) {
                String s = x + "." + i;
                addressList.add(s);
            }
        });
        addressList.forEach(x -> {
            String[] strings = x.split("\\Q.\\E");
            byte[] bytes = new byte[4];
            char[] oct1Chs = new char[3];
            char[] oct2Chs = new char[3];
            char[] oct3Chs = new char[3];
            strings[0].getChars(0, 2, oct1Chs, 0);
            strings[1].getChars(3, 6, oct2Chs, 0);
            strings[2].getChars(7, 10, oct2Chs, 0);
            StringBuilder stringBuilder = new StringBuilder()
                .append(Arrays.toString(oct1Chs)).append(" 1 \n")
                .append(Arrays.toString(oct2Chs)).append(" 2 \n")
                .append(Arrays.toString(oct3Chs)).append(" 3");
            LOGGER.warn(stringBuilder.toString());
        });
    }

}
