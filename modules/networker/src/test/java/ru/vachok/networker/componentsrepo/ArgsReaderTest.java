package ru.vachok.networker.componentsrepo;


import org.jetbrains.annotations.NotNull;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.net.TestServer;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;


/**
 @see ArgsReader
 @since 19.07.2019 (9:51) */
public class ArgsReaderTest {
    
    
    private String[] args;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    @Test
    public void testReadArgs() {
        this.args = new String[]{"-r test"};
        ConfigurableApplicationContext applicationContext = IntoApplication.getConfigurableApplicationContext();
        ArgsReader argsReader = new ArgsReader(applicationContext, args);
        try {
            argsReader.run();
            System.out.println(applicationContext.toString());
        }
        catch (RejectedExecutionException e) {
            System.out.println("argsReader.toString() = " + argsReader.toString());
        }
    }
    
    @Test
    public void testToString1() {
        this.args = new String[]{"test"};
        String toString = new ArgsReader(IntoApplication.getConfigurableApplicationContext(), args).toString();
        Assert.assertTrue(toString.contains("=test"), toString);
    }
    
    @Test
    public void testFillArgs() {
        this.args = new String[]{"test"};
        testFill();
    }
    
    private void testFill() {
        List<@NotNull String> argsList = Arrays.asList(args);
        Runnable exitApp = new ExitApp(IntoApplication.class.getSimpleName());
        Map<String, String> argsMap = new ConcurrentHashMap<>();
        
        boolean isTray = true;
        
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
            isTray = parseMapEntry(argValueEntry, exitApp);
            if (argValueEntry.getValue().equals("test")) {
                messageToUser.warn("throw new RejectedExecutionException(\"TEST\");");
            }
            if (argValueEntry.getKey().equals("test")) {
                messageToUser.warn("throw new RejectedExecutionException(\"TEST\");");
            }
        }
        Assert.assertTrue(argsList.size() >= 1);
    }
    
    private boolean parseMapEntry(@NotNull Map.Entry<String, String> stringStringEntry, Runnable exitApp) {
        boolean isTray = true;
        Properties localCopyProperties = new Properties();
        if (stringStringEntry.getKey().contains(ConstantsFor.PR_TOTPC)) {
            localCopyProperties.setProperty(ConstantsFor.PR_TOTPC, stringStringEntry.getValue());
        }
        if (stringStringEntry.getKey().equals("off")) {
            AppComponents.threadConfig().execByThreadConfig(exitApp);
        }
        if (stringStringEntry.getKey().contains("notray")) {
            messageToUser.info("IntoApplication.readArgs", "key", " = " + stringStringEntry.getKey());
            isTray = false;
        }
        if (stringStringEntry.getKey().contains("ff")) {
            Map<Object, Object> objectMap = Collections.unmodifiableMap(AppComponents.getProps());
            localCopyProperties.clear();
            localCopyProperties.putAll(objectMap);
        }
        if (stringStringEntry.getKey().contains(TestServer.PR_LPORT)) {
            localCopyProperties.setProperty(TestServer.PR_LPORT, stringStringEntry.getValue());
        }
        
        return isTray;
    }
}