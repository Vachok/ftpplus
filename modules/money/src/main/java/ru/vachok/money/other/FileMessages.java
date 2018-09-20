package ru.vachok.money.other;


import org.slf4j.Logger;
import org.thymeleaf.util.CharArrayWrapperSequence;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.money.ConstantsFor;

import java.io.*;
import java.util.concurrent.TimeUnit;


/**
 @since 14.09.2018 (23:17) */
public class FileMessages implements MessageToUser {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = FileMessages.class.getSimpleName();

    /**
     {@link }
     */
    private static final Logger LOGGER = ConstantsFor.getLogger();


    @Override
    public void errorAlert(String s, String s1, String s2) {
        CharSequence s1Seq = new CharArrayWrapperSequence((s1 + "\n").toCharArray());
        CharSequence s2Seq = new CharArrayWrapperSequence((s2 + "\n").toCharArray());
        File logFile = getLogFile(s);
        try(FileWriter fileW = new FileWriter(logFile)){
            BufferedWriter bufferedWriter = new BufferedWriter(fileW);
            fileW.flush();
            fileW.append(s1Seq);
            fileW.append(s2Seq);
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
    }

    private File getLogFile(String fileName) {
        File logFile = new File(fileName + ".log");
        if(logFile.exists() &&
            (logFile.lastModified() > (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2)))){
            logFile.delete();
            try{
                boolean newFile = logFile.createNewFile();
                LOGGER.info("create new log is " + newFile);
            }
            catch(IOException e){
                LOGGER.error(e.getMessage(), e);
            }
        }
        return logFile;
    }

    @Override
    public void info(String s, String s1, String s2) {
        errorAlert(s, s1, s2);
    }

    @Override
    public void infoNoTitles(String s) {
        errorAlert(SOURCE_CLASS, "NO TITLE", s);
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