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

        ipBuild(lan10);
    }

    private void ipBuild(String lan10) {
        List<String> threeOctets = new ArrayList<>();
        for (int i = 10; i < 50; i++) {
            threeOctets.add(lan10 + i);
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
            char[] chars = new char[16];
            for(int i = 0; i < strings.length; i++){
                strings[i].getChars(0, 1, chars, i); //todo 12.11.2018 (22:25)
            }
            LOGGER.info(Arrays.toString(chars));
        });
    }

}
