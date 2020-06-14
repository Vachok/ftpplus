package ru.vachok.networker.data.synchronizer;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.OtherKnownDevices;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.util.concurrent.TimeoutException;


/**
 @see TimeOnActualizer
 @since 21.11.2019 (13:49) */
public class TimeOnActualizerTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(TimeOnActualizer.class
        .getSimpleName(), System.nanoTime());

    private final String dbToSync = ConstantsFor.DB_VELKOMVELKOMPC;

    private TimeOnActualizer timeOnActualizer;

    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
        this.timeOnActualizer = new TimeOnActualizer(OtherKnownDevices.DO0213_KUDR, false);
    }

    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }

    @Test
    public void testRun() {
        try {
            AppConfigurationLocal.getInstance().executeBlock(timeOnActualizer, 1);
        }
        catch (TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }

        try {
            AppConfigurationLocal.getInstance().executeBlock(timeOnActualizer, 50);
        }
        catch (TimeoutException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
}