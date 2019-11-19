package ru.vachok.networker.exe;


import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.text.MessageFormat;
import java.util.concurrent.*;


public class ThreadTimeout extends Thread {
    
    
    private Future<?> submit;
    
    private long timeOutSeconds;
    
    public ThreadTimeout(Future<?> submit, long timeOutSeconds) {
        this.submit = submit;
        this.timeOutSeconds = timeOutSeconds;
    }
    
    @Override
    public void run() {
        try {
            submit.get(timeOutSeconds, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.err.println(MessageFormat.format("{0}.execute = {1}, {2}",
                    AppConfigurationLocal.class.getSimpleName(), e.getMessage(), Thread.currentThread().getState().name()));
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ThreadTimeout{");
        sb.append("timeOutSeconds=").append(timeOutSeconds);
        sb.append(", submit=").append(submit);
        sb.append('}');
        return sb.toString();
    }
}
