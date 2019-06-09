package ru.vachok.networker.services;


import ru.vachok.networker.TForms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;


/**
 @since 22.05.2019 (11:50) */
public class SystemRuntime implements Callable<String> {
    
    
    private String commandToExecute;
    
    public SystemRuntime(String commandToExecute) {
        this.commandToExecute = commandToExecute;
    }
    
    @Override public String call() {
        return resultOfProcess();
    }
    
    private String resultOfProcess() {
        StringBuilder stringBuilder = new StringBuilder();
        Process exec = null;
        try {
            exec = Runtime.getRuntime().exec(commandToExecute);
        }
        catch (IOException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, true));
        }
        try (InputStream inputStream = exec.getInputStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {
            while (inputStreamReader.ready()) {
                stringBuilder.append(bufferedReader.readLine());
            }
        }
        catch (IOException e) {
            stringBuilder.append(e.getMessage() + "\n" + new TForms().fromArray(e, true));
        }
        return stringBuilder.toString();
    }
}
