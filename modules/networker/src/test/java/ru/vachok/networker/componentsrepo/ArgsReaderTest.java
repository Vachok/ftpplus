package ru.vachok.networker.componentsrepo;


import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.net.TestServer;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;


/**
 @see ArgsReader
 @since 19.07.2019 (9:51) */
public class ArgsReaderTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private ConfigurableApplicationContext context;
    
    private ArgsReader argsReader;
    
    private String[] args = new String[2];
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    public ArgsReaderTest() {
        try {
            context = IntoApplication.getConfigurableApplicationContext();
        }
        catch (Exception e) {
            context = new SpringApplication().run();
        }
        finally {
            context.stop();
            context.close();
        }
    }
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
        try (ConfigurableApplicationContext ct = context) {
            this.argsReader = new ArgsReader(ct, new String[]{"test"});
        }
        catch (Exception e) {
            messageToUser.error(MessageFormat.format("ArgsReaderTest.setUp: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
        Assert.assertFalse(context.isActive());
        Assert.assertFalse(context.isRunning());
    }
    
    @Test
    public void testRealRun() {
        this.args = new String[]{"-r test"};
        try {
            argsReader.run();
            Assert.assertTrue(argsReader.toString().contains("-r"));
        }
        catch (RejectedExecutionException e) {
            Assert.assertNotNull(e);
        }
    }
    
    @Test
    public void testToString1() {
        Assert.assertTrue(argsReader.toString().contains("test"));
    }
    
    @Test
    public void testFillArgs() {
    
        this.args = new String[]{"-test"};
        Assert.assertTrue(testFill());
    
        this.args = new String[]{"-r", "test"};
    
        Assert.assertFalse(testFill());
    
        this.args = new String[]{"-test", "-y"};
        Assert.assertTrue(testFill());
    
        Assert.assertFalse(context.isRunning());
    }
    
    private boolean testFill() {
        List<@NotNull String> argsList = Arrays.asList(args);
        Map<String, String> argsMap = new ConcurrentHashMap<>();
        
        boolean isTray = false;
        
        for (int i = 0; i < argsList.size(); i++) {
            String key = argsList.get(i);
            String value;
            try {
                value = argsList.get(i + 1);
            }
            catch (ArrayIndexOutOfBoundsException ignore) {
                value = "true";
            }
            if (!value.contains("-")) {
                argsMap.put(key, value);
            }
            else {
                if (!key.contains("-")) {
                    argsMap.put("", "");
                }
                else {
                    argsMap.put(key, "true");
                }
            }
        }
        for (Map.Entry<String, String> argValueEntry : argsMap.entrySet()) {
            boolean keyEqualsTest = argValueEntry.getKey().equals("-test");
            boolean valEqualsTest = argValueEntry.getValue().equals("true");
            isTray = keyEqualsTest & valEqualsTest;
        }
        return isTray;
    }
    
    private void parseMapEntry(@NotNull Map.Entry<String, String> keyValueEntryArg, Runnable exitApp) {
        boolean isTray = true;
        this.args = new String[]{"-notray"};
        Properties localCopyProperties = new Properties();
        if (keyValueEntryArg.getKey().contains(ConstantsFor.PR_TOTPC)) {
            localCopyProperties.setProperty(ConstantsFor.PR_TOTPC, keyValueEntryArg.getValue());
        }
        if (keyValueEntryArg.getKey().equals("off")) {
            AppComponents.threadConfig().execByThreadConfig(exitApp);
        }
        if (keyValueEntryArg.getKey().contains("notray")) {
            messageToUser.info("IntoApplication.readArgs", "key", " = " + keyValueEntryArg.getKey());
            isTray = false;
        }
        if (keyValueEntryArg.getKey().contains("ff")) {
            Map<Object, Object> objectMap = Collections.unmodifiableMap(AppComponents.getProps());
            localCopyProperties.clear();
            localCopyProperties.putAll(objectMap);
        }
        if (keyValueEntryArg.getKey().contains(TestServer.PR_LPORT)) {
            localCopyProperties.setProperty(TestServer.PR_LPORT, keyValueEntryArg.getValue());
        }
        Assert.assertTrue(isTray, Arrays.toString(args));
    }
}