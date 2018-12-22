package ru.vachok.networker.fileworks;


import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.Logger;
import ru.vachok.messenger.email.ESender;
import ru.vachok.networker.AppInfoOnLoad;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.accesscontrol.common.CommonScan2YOlder;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.services.MyCalen;

import java.io.*;
import java.nio.file.*;
import java.util.Date;
import java.util.List;


/**
 Вспомогательная работа с файлами.

 @since 19.12.2018 (9:57) */
public abstract class FileSystemWorker extends SimpleFileVisitor<Path> {

    /*Fields*/

    /**
     {@link AppInfoOnLoad#getConstTxt()}
     */
    private static final File CONST_TXT = AppInfoOnLoad.getConstTxt();

    /**
     {@link ThreadConfig}
     */
    private static final ThreadConfig THREAD_CONFIG = new ThreadConfig();

    /**
     {@link AppComponents#getLogger()}
     */
    static final Logger LOGGER = AppComponents.getLogger();

    /*METHODS*/

    /**
     @param atHome дома / не дома
     */
    public static synchronized void cpConstTxt(boolean atHome) {
        if(atHome){
            fileMake();
        }
    }

    /**
     Пишет {@link #CONST_TXT}
     <p>
     <code>
     printWriter.println(new Date(timeInfo.getReturnTime())); printWriter.println(ConstantsFor.toStringS() + "\n\n" + MyCalen.toStringS());
     </code> <br>
     Копирует в {@code G:\My_Proj\FtpClientPlus\modules\networker\src\main\resources\static\texts\}, если дома.
     */
    private static synchronized void fileMake() {
        synchronized(CONST_TXT) {
            try(OutputStream outputStream = new FileOutputStream(CONST_TXT);
                PrintWriter printWriter = new PrintWriter(outputStream, true)){
                TimeInfo timeInfo = MyCalen.getTimeInfo();
                timeInfo.computeDetails();
                printWriter.println(new Date(timeInfo.getReturnTime()));
                printWriter.println(ConstantsFor.toStringS() + "\n\n" + MyCalen.toStringS());
                THREAD_CONFIG.threadPoolTaskExecutor().execute(new FilesCP());
            }
            catch(IOException e){
                LOGGER.warn(e.getMessage());
            }
        }
    }

    /**
     Запись файла

     @param fileName  имя файла
     @param toFileRec {@link List} строчек на запись.
     */
    public static synchronized void recFile(String fileName, List<String> toFileRec) {
        try(OutputStream outputStream = new FileOutputStream(fileName);
            PrintWriter printWriter = new PrintWriter(outputStream, true)){
            toFileRec.forEach(printWriter::println);
        }
        catch(IOException e){
            LOGGER.info(e.getMessage());
        }
    }

    /**
     Удаление временных файлов.
     <p>
     Usages: {@link ru.vachok.networker.SystemTrayHelper#addTray(String)}, {@link ru.vachok.networker.controller.ServiceInfoCtrl#closeApp()}, {@link ru.vachok.networker.net.MyServer#reconSock()}. <br>
     Uses: {@link CommonScan2YOlder} <br>
     */
    public static void delTemp() {
        try{
            Files.walkFileTree(Paths.get("."), new DeleterTemp());
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static String searchInCommon(String[] folderPath) {
        FileSearcher fileSearcher = new FileSearcher(folderPath[0]);
        try{
            String folderToSearch = folderPath[1];
            folderToSearch = "\\\\srv-fs.eatmeat.ru\\common_new\\" + folderToSearch;
            Files.walkFileTree(Paths.get(folderToSearch), fileSearcher);
            String oneAddress = "netvisor@velkomfood.ru";
            if(ConstantsFor.thisPC().contains("NO0027")){
                oneAddress = "143500@gmail.com";
            }
            ESender resSend = new ESender(oneAddress);
            List<String> fileSearcherResList = fileSearcher.getResList();
            String resTo = new TForms().fromArray(fileSearcherResList, true);
            if(fileSearcherResList.size() > 0){
                resSend.info(FileSearcher.class.getSimpleName(), "SEARCHER RESULTS " + new Date().getTime(), fileSearcher.toString());
            }
            return resTo;
        }
        catch(Exception e){
            return e.getMessage();
        }
    }

    /**
     Чтение файла из файловой системы.
     <p>

     @param s путь к файлу.
     @return файл, построчно.
     */
    public static String readFile(String s) {
        final long stArt = System.currentTimeMillis();

        StringBuilder stringBuilder = new StringBuilder();
        boolean exists = new File(s).exists();
        if(exists){
            try(InputStream inputStream = new FileInputStream(s);
                InputStreamReader reader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(reader)){
                int avaBytes = inputStream.available();
                stringBuilder
                    .append("Bytes in stream: ")
                    .append(avaBytes)
                    .append("<br\n>");
                while(bufferedReader.ready()){
                    stringBuilder
                        .append(bufferedReader.readLine())
                        .append("<br>\n");
                }
            }
            catch(IOException e){
                stringBuilder.append(e.getMessage());
            }
        }
        else{
            stringBuilder
                .append("File: ")
                .append(s)
                .append(" does not exists!");
        }
        String msgTimeSp = new StringBuilder()
            .append("FileSystemWorker.readFile: ")
            .append(( float ) (System.currentTimeMillis() - stArt) / 1000)
            .append(" sec spend")
            .toString();
        LOGGER.info(msgTimeSp);
        return stringBuilder.toString();
    }

    public static boolean copyFile(File oldLANFile, String s) {
        try{
            Path copy = Files.copy(oldLANFile.toPath(), Paths.get(s));
            String msg = copy.toString();
            LOGGER.info(msg);
            return true;
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     Метод для копирования.

     @see #fileMake()
     */
    synchronized void cpConstTxt(String s) {
        Path toCopy = Paths.get(s);
        try{
            boolean canWrite = CONST_TXT.canWrite();
            if(canWrite){
                do{
                    wait();
                } while(CONST_TXT.canWrite());
            }
            Files.deleteIfExists(toCopy);
            Files.copy(CONST_TXT.toPath(), toCopy);
        }
        catch(IOException | InterruptedException e){
            LOGGER.warn(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
