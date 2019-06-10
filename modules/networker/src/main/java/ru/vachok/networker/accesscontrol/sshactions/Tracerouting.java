// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.sshactions;


import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.util.concurrent.*;
import java.util.regex.Pattern;


/**
 @since 24.05.2019 (9:30) */
public class Tracerouting implements Callable<String> {
    
    
    private static final Pattern COMPILE = Pattern.compile(";");
    
    private final String SRV_NEEDED = new AppComponents().sshActs().whatSrvNeed();
    
    @Override public String call() throws Exception {
        return getProviderTraceStr();
    }
    
    /**
     Traceroute
     <p>
     Соберём {@link SSHFactory} - {@link SSHFactory.Builder#build()} ({@link ConstantsFor#IPADDR_SRVGIT}, "traceroute ya.ru;exit") <br>
     Вызовем в строку {@code callForRoute} - {@link SSHFactory#call()}
     <p>
     Переопределим {@link SSHFactory} - {@link SSHFactory.Builder#build()} ({@link ConstantsFor#IPADDR_SRVNAT}, "sudo cat /home/kudr/inet.log") <br>
     Переобределим {@code callForRoute} - {@code callForRoute} + {@code "LOG: "} + {@link SSHFactory#call()}
     <p>
     Если {@code callForRoute.contains("91.210.85.")} : добавим в {@link StringBuilder} - {@code "FORTEX"} <br>
     Else if {@code callForRoute.contains("176.62.185.129")} : добавим {@code "ISTRANET"} <br>
     Если {@code callForRoute.contains("LOG: ")} добавим {@link String#split(String)}[1] по {@code "LOG: "}
     
     @return {@link StringBuilder#toString()} собравший инфо из строки с сервера.
     
     @throws ArrayIndexOutOfBoundsException при разборе строки
     */
    private String getProviderTraceStr() throws ArrayIndexOutOfBoundsException, InterruptedException, ExecutionException, TimeoutException {
        StringBuilder stringBuilder = new StringBuilder();
        SSHFactory sshFactory = new SSHFactory.Builder(SRV_NEEDED, "traceroute velkomfood.ru && exit", getClass().getSimpleName()).build();
        Future<String> curProvFuture = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(sshFactory);
        String callForRoute = curProvFuture.get(ConstantsFor.DELAY, TimeUnit.SECONDS);
        stringBuilder.append("<br><a href=\"/makeok\">");
        if (callForRoute.contains("91.210.85.")) {
            stringBuilder.append("<h3>FORTEX</h3>");
        }
        else {
            if (callForRoute.contains("176.62.185.129")) {
                stringBuilder.append("<h3>ISTRANET</h3>");
            }
        }
        stringBuilder.append("</a></br>");
        String logStr = "LOG: ";
        callForRoute = callForRoute + "<br>LOG: " + getInetLog();
        if (callForRoute.contains(logStr)) {
            try {
                stringBuilder.append("<br><font color=\"gray\">").append(COMPILE.matcher(callForRoute.split(logStr)[1]).replaceAll("<br>")).append("</font>");
            }
            catch (ArrayIndexOutOfBoundsException e) {
                stringBuilder.append(FileSystemWorker.error("SshActs.getProviderTraceStr", e));
            }
        }
        return stringBuilder.toString();
    }
    
    /**
     Лог переключений инета.
     
     @return {@code "sudo cat /home/kudr/inet.log"} or {@link InterruptedException}, {@link ExecutionException}, {@link TimeoutException}
     */
    private String getInetLog() {
        AppComponents.threadConfig().thrNameSet("iLog");
        SSHFactory sshFactory = new SSHFactory.Builder(ConstantsFor.IPADDR_SRVNAT, "sudo cat /home/kudr/inet.log", getClass().getSimpleName()).build();
        Future<String> submit = AppComponents.threadConfig().getTaskExecutor().submit(sshFactory);
        try {
            return submit.get(10, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            return e.getMessage() + " inet switching log. May be connection error. Keep calm - it's ok";
        }
    }
    
}
