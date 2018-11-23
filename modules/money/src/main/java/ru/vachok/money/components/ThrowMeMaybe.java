package ru.vachok.money.components;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.money.services.TForms;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 @since 28.09.2018 (16:59) */
public class ThrowMeMaybe {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThrowMeMaybe.class.getSimpleName());

    private Throwable databaseErr;

    public Throwable getDatabaseErr() {
        return databaseErr;
    }

    /*Get&Set*/
    public void setDatabaseErr(Throwable databaseErr) {
        ConcurrentMap<String, StackTraceElement[]> traceElementConcurrentMap = new ConcurrentHashMap<>();
        Thread.getAllStackTraces().forEach((x, y) -> {
            traceElementConcurrentMap.put(x.getName(), y);
            databaseErr.setStackTrace(y);
        });
        LOGGER.error(new TForms().toStringFromArray(traceElementConcurrentMap, false));
        this.databaseErr = databaseErr;
    }
}
