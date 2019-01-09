package ru.vachok.networker.fileworks;


import org.slf4j.Logger;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.accesscontrol.common.CommonScan2YOlder;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.io.*;
import java.nio.file.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;


/**
 Вспомогательная работа с файлами.

 @since 19.12.2018 (9:57) */
public abstract class FileSystemWorker extends SimpleFileVisitor<Path> {

    /**
     {@link AppComponents#getLogger()}
     */
    static final Logger LOGGER = AppComponents.getLogger();

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

    public static synchronized void recFile(String fileName, Stream<String> toFileRec) {
        try (OutputStream outputStream = new FileOutputStream(fileName);
             PrintWriter printWriter = new PrintWriter(outputStream, true)) {
            toFileRec.forEach(printWriter::println);
        } catch (IOException e) {
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

    /**
     Поиск в \\srv-fs\common_new
     <p>

     @param folderPath папка, откуда начать искать
     @return список файлов или {@link Exception}
     @see FileSearcher
     */
    public static String searchInCommon(String[] folderPath) {
        FileSearcher fileSearcher = new FileSearcher(folderPath[0]);
        try{
            String folderToSearch = folderPath[1];
            folderToSearch = "\\\\srv-fs.eatmeat.ru\\common_new\\" + folderToSearch;
            Files.walkFileTree(Paths.get(folderToSearch), fileSearcher);
            String oneAddress = "netvisor@velkomfood.ru";
            if(ConstantsFor.thisPC().toLowerCase().contains("home") || ConstantsFor.thisPC().contains("NO0027")){
                oneAddress = ConstantsFor.GMAIL_COM;
            }
            MessageToUser resSend = new ESender(oneAddress);
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

     @param fileName путь к файлу.
     @return файл, построчно.
     */
    public static String readFile(String fileName) {
        final long stArt = System.currentTimeMillis();

        StringBuilder stringBuilder = new StringBuilder();
        boolean exists = new File(fileName).exists();
        if(exists){
            try(InputStream inputStream = new FileInputStream(fileName);
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
                .append(fileName)
                .append(" does not exists!");
        }
        String msgTimeSp = new StringBuilder()
            .append("FileSystemWorker.readFile: ")
            .append(( float ) (System.currentTimeMillis() - stArt) / 1000)
            .append(ConstantsFor.STR_SEC_SPEND)
            .toString();
        LOGGER.info(msgTimeSp);
        return stringBuilder.toString();
    }

    /**
     Простое копирование файла.

     @param origFile файл, для копирования
     @param s        строка путь
     @return удача/нет
     */
    public static boolean copyOrDelFile(File origFile, String s, boolean needDel) {
        File toCpFile = new File(s);
        try{
            Path targetPath = toCpFile.toPath();
            Path directories = Files.createDirectories(targetPath.getParent());
            toCpFile = targetPath.toFile();
            Path copy = Files.copy(origFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            if(needDel){
                Files.deleteIfExists(origFile.toPath());
            }
            String msg = directories + " getParent directory. " + copy.toString() + " " + toCpFile.exists();
            LOGGER.info(msg);
        }
        catch(IOException | NullPointerException e){
            LOGGER.warn(e.getMessage(), e);
            return toCpFile.exists();
        }
        return toCpFile.exists();
    }
}
