package ru.vachok.networker;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.server.TelnetServer;
import ru.vachok.networker.componentsrepo.systray.SystemTrayHelper;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RejectedExecutionException;


/**
 @since 19.07.2019 (9:51) */
class ArgsReader implements Runnable {


    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, ArgsReader.class.getSimpleName());

    private static String[] appArgs;

    private static final ConcurrentMap<String, String> APP_ARGS = new ConcurrentHashMap<>();

    static ConcurrentMap<String, String> getAppArgs() {
        return APP_ARGS;
    }

    ArgsReader(String[] appArgs) {
        this.appArgs = appArgs;
    }

    @Override
    public void run() {
        fillArgsMap();
    }

    private static void fillArgsMap() {
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
                value = "true";
            }
            if (!value.contains("-")) {
                APP_ARGS.put(key, value);
            }
            else {
                if (!key.contains("-")) {
                    APP_ARGS.put("", "");
                }
                else {
                    APP_ARGS.put(key, "true");
                }
            }
        }
        for (Map.Entry<String, String> argValueEntry : APP_ARGS.entrySet()) {
            isTray = parseMapEntry(argValueEntry, exitApp);
            if (argValueEntry.getValue().equals("test")) {
                throw new RejectedExecutionException("TEST. VALUE");
            }
            if (argValueEntry.getKey().equals("test")) {
                throw new RejectedExecutionException("TEST. KEY");
            }
        }
        if (isTray && SystemTrayHelper.getI().isPresent()) {
            ((SystemTrayHelper) SystemTrayHelper.getI().get()).trayAdd();
        }
        readArgs();
    }

    private static boolean parseMapEntry(@NotNull Map.Entry<String, String> stringStringEntry, Runnable exitApp) {
        Properties localCopyProperties = InitProperties.getTheProps();
        boolean isTray = true;
        if (stringStringEntry.getKey().contains(PropertiesNames.TOTPC)) {
            localCopyProperties.setProperty(PropertiesNames.TOTPC, stringStringEntry.getValue());
        }
        if (stringStringEntry.getKey().equals("off")) {
            AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().execute(exitApp);
        }
        if (stringStringEntry.getKey().contains("notray")) {
            messageToUser.info("IntoApplication.readArgs", "key", " = " + stringStringEntry.getKey());
            isTray = false;
        }
        if (stringStringEntry.getKey().contains("ff")) {
            Map<Object, Object> objectMap = Collections.unmodifiableMap(InitProperties.getTheProps());
            localCopyProperties.clear();
            localCopyProperties.putAll(objectMap);
        }
        if (stringStringEntry.getKey().contains(TelnetServer.PR_LPORT)) {
            localCopyProperties.setProperty(TelnetServer.PR_LPORT, stringStringEntry.getValue());
        }

        return isTray;
    }

    private static void readArgs() {
        IntoApplication.setUTF8Enc();
        try {
            IntoApplication.checkTray();
        }
        catch (IllegalStateException e) {
            messageToUser.error(MessageFormat.format("ArgsReader.readArgs: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        finally {
            IntoApplication.appInfoStarter();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ArgsReader{");
        sb.append("appArgs=").append(AbstractForms.fromArray(appArgs));
        sb.append('}');
        return sb.toString();
    }
}