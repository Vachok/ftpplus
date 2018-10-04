package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.vachok.networker.componentsrepo.ADUser;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.io.File;
import java.util.*;

/**
 @since 02.10.2018 (17:32) */
@Service
public class PCUserResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PCUserResolver.class.getSimpleName());

    public static Map<String, Boolean> lastScanMap = AppComponents.lastNetScan().getNetWork();

    private static PCUserResolver pcUserResolver = new PCUserResolver();

    private PCUserResolver() {

    }

    public static PCUserResolver getPcUserResolver() {
        return pcUserResolver;
    }

    public ADUser adPCSetter() {
        ADSrv adSrv = AppComponents.adSrv();
        ADUser adUser = adSrv.getAdUser();
        try {
            adUser.setUserName(getResolvedName());
        } catch (NullPointerException e) {
            LOGGER.warn("I cant set User for");
        }
        return adUser;
    }

    private String getResolvedName() throws NullPointerException {
        List<String> onlineNow = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        if (!lastScanMap.isEmpty()) {
            lastScanMap.forEach((x, y) -> {
                if (y) onlineNow.add(x);
            });
        } else NetScannerSvc.getI().getPCsAsync();
        onlineNow.forEach(x -> {
            x = x.replaceAll("<br><b>", "").split("</b><br>")[0];
            File[] files = new File("\\\\" + x + "\\c$\\Users\\").listFiles();
            Map<Long, String> lastMod = new HashMap<>();

            if (files != null) {
                for (File file : files) {
                    lastMod.put(file.lastModified(), file.getName() + " user " + x + " comp\n");
                }
            } else lastMod.put(System.currentTimeMillis(), "Can't set user for: " + x + "\n");

            Optional<Long> max = lastMod.keySet().stream().max(Long::compareTo);
            boolean aLongPresent = max.isPresent();
            if (aLongPresent) {
                Long aLong = max.get();
                stringBuilder.append(lastMod.get(aLong));
            }
        });
        return stringBuilder.toString();
    }
}
