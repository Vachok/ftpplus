package ru.vachok.networker.net;


import org.slf4j.Logger;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.util.Arrays;

/**
 Скан диапазона адресов

 @since 19.12.2018 (11:35) */
public class DiapazonedScan {

    private static final Logger LOGGER = AppComponents.getLogger();

    private static volatile DiapazonedScan ourInstance;

    private DiapazonedScan() {
    }

    public static DiapazonedScan getInstance() {
        ConstantsFor.showMem();
        if (ourInstance == null) {
            synchronized (DiapazonedScan.class) {
                if (ourInstance == null) {
                    LOGGER.info("DiapazonedScan.getInstance");
                    ourInstance = new DiapazonedScan();
                }
            }

        }
        return ourInstance;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DiapazonedScan{");
        sb.append("ourInstance=").append(ourInstance.getClass().getTypeName());
        sb.append(" Скан диапазона адресов since 19.12.2018 (11:35)").append("\n");
        sb.append(ConstantsFor.showMem());
        sb.append(Arrays.toString(KassaSBank.values()));
        sb.append('}');
        return sb.toString();
    }
}
