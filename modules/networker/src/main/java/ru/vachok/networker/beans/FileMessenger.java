package ru.vachok.networker.beans;


import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.CharArrayWrapperSequence;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.config.AppComponents;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 @since 07.09.2018 (0:12) */
@Service
public class FileMessenger extends Thread implements MessageToUser {

    /*Fields*/

    private static final Logger LOGGER = AppComponents.getLogger();

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = FileMessenger.class.getSimpleName();

    private static File logFile = new File((System.currentTimeMillis() - ConstantsFor.START_STAMP) + ConstantsFor.APP_NAME + ".log");

    @Override
    public void errorAlert(String s, String s1, String s2) {
        CharSequence firstSeq = new CharArrayWrapperSequence((s + "\n").toCharArray());
        CharSequence secondSeq = new CharArrayWrapperSequence((s1 + "\n").toCharArray());
        CharSequence thirdSeq = new CharArrayWrapperSequence((s2 + "\n").toCharArray());
        String msg = writeLog(firstSeq) + " s writer";
        LOGGER.warn(msg);
        String msg1 = writeLog(secondSeq) + " s1 writer";
        LOGGER.warn(msg1);
        String msg2 = writeLog(thirdSeq) + " s2 writer";
        LOGGER.warn(msg2);
    }

    @Override
    public void info(String s, String s1, String s2) {
        CharSequence firstSeq = new CharArrayWrapperSequence((s + "\n").toCharArray());
        CharSequence secondSeq = new CharArrayWrapperSequence((s1 + "\n").toCharArray());
        CharSequence thirdSeq = new CharArrayWrapperSequence((s2 + "\n").toCharArray());
        String msg = writeLog(firstSeq) + " s writer";
        LOGGER.info(msg);
        String msg1 = writeLog(secondSeq) + " s1 writer";
        LOGGER.info(msg1);
        String msg2 = writeLog(thirdSeq) + " s2 writer";
        LOGGER.info(msg2);
    }

    @Override
    public void infoNoTitles(String s) {
        info(SOURCE_CLASS, "INFO", s);
    }

    @Override
    public void infoTimer(int i, String s) {
        throw new UnsupportedOperationException("");
    }

    @Override
    public String confirm(String s, String s1, String s2) {
        throw new UnsupportedOperationException("07.09.2018 (0:12)");
    }

    private static boolean writeLog(CharSequence charSequence) {
        String msg = logFile.getAbsolutePath() +
            " last mod: " +
            new Date(logFile.lastModified()) +
            " ; size = " +
            logFile.getUsableSpace() / ConstantsFor.KBYTE + " kbytes";
        try(FileWriter fileWriter = new FileWriter(logFile);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)){
            boolean newFile = logFile.createNewFile();
            bufferedWriter.flush();
            bufferedWriter.newLine();
            bufferedWriter.append(charSequence);
            bufferedWriter.flush();
            boolean b = logFile.setLastModified(System.currentTimeMillis());
            LOGGER.info(msg);
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
        if(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - logFile.lastModified()) <= 5){
            LOGGER.info(msg);
            return true;
        }
        else{
            LOGGER.warn(msg);
            return false;
        }
    }
}