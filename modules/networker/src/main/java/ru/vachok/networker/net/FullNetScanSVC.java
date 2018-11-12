package ru.vachok.networker.net;


import org.slf4j.Logger;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.util.ArrayList;
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

        ipBuild(lan10);
    }

    private void ipBuild(String lan10) {
        List<String> threeOctets = new ArrayList<>();
        for (int i = 10; i < 50; i++) {
            threeOctets.add(lan10 + i);
        }
        buildOct4(threeOctets);
    }

    private void buildOct4(List<String> threeOctets) { // TODO: 12.11.2018 resolve ip
        threeOctets.forEach(x -> {
            for (int i = 0; i < 255; i++) {
                String s = x + "." + i;
                addressList.add(s);
            }
        });
        addressList.forEach(x -> {
            byte[] ipBytes = new byte[4];
            String[] split = x.split("\\Q.\\E");
            char oc1 = split[0].charAt(0);
            char oc2 = split[1].charAt(0);
            char oc3 = split[2].charAt(0);
            char oc4 = split[3].charAt(0);
            ipBytes[0] = (byte) oc1;
            ipBytes[1] = (byte) oc2;
            ipBytes[2] = (byte) oc3;
            ipBytes[3] = (byte) oc4;
        });
    }

}
