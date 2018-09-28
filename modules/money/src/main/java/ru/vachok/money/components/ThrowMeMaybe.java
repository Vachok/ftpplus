package ru.vachok.money.components;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.money.services.TForms;

/**
 @since 28.09.2018 (16:59) */
public class ThrowMeMaybe {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThrowMeMaybe.class.getSimpleName());

    private Throwable databaseErr;

    public Throwable getDatabaseErr() {
        return databaseErr;
    }

    public void setDatabaseErr(Throwable databaseErr) {
        Thread.getAllStackTraces().forEach((x, y) -> {
            String s = x.getName() + " " + TForms.toStringFromArray(y);
            LOGGER.error(s);
            databaseErr.setStackTrace(y);
        });
        this.databaseErr = databaseErr;
    }
}
