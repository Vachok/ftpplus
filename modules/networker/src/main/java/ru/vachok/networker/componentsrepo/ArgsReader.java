// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo;


import org.jetbrains.annotations.NotNull;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.Lifecycle;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.server.TelnetServer;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RejectedExecutionException;


/**
 @since 19.07.2019 (9:51) */
public class ArgsReader extends IntoApplication implements Runnable {
    
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private String[] appArgs;
    
    private Lifecycle context;
    
    private ConcurrentMap<String, String> argsMap = new ConcurrentHashMap<>();
    
    public ArgsReader(Lifecycle context, String[] appArgs) {
        this.appArgs = appArgs;
        this.context = context;
    }
    
    @Override
    public void run() {
        fillArgsMap();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ArgsReader{");
        sb.append("appArgs=").append(new TForms().fromArray(appArgs));
        sb.append('}');
        return sb.toString();
    }
    
    private void readArgs(boolean isTray) {
        beforeSt(isTray);
        try {
            context.start();
        }
        catch (IllegalStateException e) {
            messageToUser.warn(MessageFormat.format("ArgsReader.readArgs: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            ((ConfigurableApplicationContext) context).refresh();
        }
        afterSt();
    }
    
    private void fillArgsMap() {
        List<@NotNull String> argsList = Arrays.asList(appArgs);
        Runnable exitApp = new ExitApp(IntoApplication.class.getSimpleName());
        boolean isTray = true;
        
        for (int i = 0; i < argsList.size(); i++) {
            String key = argsList.get(i);
            String value;
            try {
                value = argsList.get(i + 1);
            }
            catch (ArrayIndexOutOfBoundsException ignore) {
                value= "true";
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
                throw new RejectedExecutionException("TEST. VALUE");
            }
            if (argValueEntry.getKey().equals("test")) {
                throw new RejectedExecutionException("TEST. KEY");
            }
        }
        readArgs(isTray);
    }
    
    private boolean parseMapEntry(@NotNull Map.Entry<String, String> stringStringEntry, Runnable exitApp) {
        boolean isTray = true;
        if (stringStringEntry.getKey().contains(PropertiesNames.PR_TOTPC)) {
            localCopyProperties.setProperty(PropertiesNames.PR_TOTPC, stringStringEntry.getValue());
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
        if (stringStringEntry.getKey().contains(TelnetServer.PR_LPORT)) {
            localCopyProperties.setProperty(TelnetServer.PR_LPORT, stringStringEntry.getValue());
        }
        
        return isTray;
    }
}
