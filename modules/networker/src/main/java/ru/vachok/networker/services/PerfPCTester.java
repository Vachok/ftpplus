package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.networker.ConstantsFor;


/**
 Мини-тест на производительность

 @since 23.12.2018 (20:39) */
public class PerfPCTester implements Runnable {

    /**
     {@link LoggerFactory}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PerfPCTester.class.getSimpleName());

    /**
     new {@link PerfPCTester}
     */
    private static PerfPCTester thisI = new PerfPCTester();

    /**
     @return {@link PerfPCTester}
     */
    public static PerfPCTester getI() {
        return thisI;
    }

    /**
     Singleton
     */
    private PerfPCTester() {
    }

    /**
     @deprecated 23.12.2018 (20:47)
     */
    @Deprecated
    public static void main(String[] args) {
        new PerfPCTester().run();

    }

    @Override
    public void run() {
        final long stArt = System.currentTimeMillis();
        String msgTimeSp = new StringBuilder()
            .append("PerfPCTester.run: ")
            .append(( float ) (System.currentTimeMillis() - stArt) / 1000)
            .append(ConstantsFor.STR_SEC_SPEND)
            .toString();
        long res = testMe();
        LOGGER.info(msgTimeSp);
        throw new UnsupportedOperationException("// TODO: 23.12.2018  " + res);
    }

    private long testMe() {
        final long stArt = System.currentTimeMillis();

        String msgTimeSp = new StringBuilder()
            .append("PerfPCTester.testMe: ")
            .append(( float ) (System.currentTimeMillis() - stArt) / 1000)
            .append(ConstantsFor.STR_SEC_SPEND)
            .toString();
        LOGGER.info(msgTimeSp);
        return System.currentTimeMillis();
    }
}
