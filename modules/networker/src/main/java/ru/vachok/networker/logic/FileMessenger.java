package ru.vachok.networker.logic;


import org.slf4j.Logger;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.config.AppComponents;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 @since 29.08.2018 (22:22) */
public class FileMessenger implements MessageToUser {

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = FileMessenger.class.getSimpleName();

    /**
     {@link }
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    private static final File log = new File("app.log");

    static {
        try{
            if(log.createNewFile() && !log.exists()){
                long lastMod = log.lastModified();
                if((System.currentTimeMillis() - lastMod) > TimeUnit.DAYS.toMillis(1)){
                    String msg = log.getAbsolutePath() + " is more then 1 day!";
                    LOGGER.warn(msg);
                }
            }
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void errorAlert(String s, String s1, String s2) {
        char[] toWrite = (s + " " + s1 + "\n" + s2).toCharArray();
        try(FileWriter fileWriter = new FileWriter(log);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)){
            bufferedWriter.flush();
            bufferedWriter.newLine();
            for(char c : toWrite){
                bufferedWriter.append(c);
            }
            bufferedWriter.flush();
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void info(String s, String s1, String s2) {
        errorAlert(s, s1, s2);
    }

    @Override
    public void infoNoTitles(String s) {
        errorAlert(SOURCE_CLASS, "INFO:", s);
    }

    @Override
    public void infoTimer(int i, String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String confirm(String s, String s1, String s2) {
        throw new UnsupportedOperationException();
    }
}