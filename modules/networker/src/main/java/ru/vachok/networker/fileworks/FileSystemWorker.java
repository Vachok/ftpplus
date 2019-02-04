package ru.vachok.networker.fileworks;


import org.slf4j.Logger;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.accesscontrol.common.CommonScan2YOlder;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.services.TimeChecker;
import ru.vachok.networker.systray.SystemTrayHelper;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;


/**
 Вспомогательная работа с файлами.

 @since 19.12.2018 (9:57) */
public abstract class FileSystemWorker extends SimpleFileVisitor<Path> {

    private static final String CLASS_NAME = "FileSystemWorker";

    /**
     {@link AppComponents#getLogger()}
     */
    static final Logger LOGGER = AppComponents.getLogger();

    public static synchronized void recFile(String fileName, Stream<String> toFileRec) {
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
     Usages: {@link SystemTrayHelper#addTray(String)}, {@link ru.vachok.networker.controller.ServiceInfoCtrl#closeApp()},
     {@link ru.vachok.networker.net.MyServer#reconSock()}. <br>
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
    @SuppressWarnings ("MethodWithMultipleReturnPoints")
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
     Простое копирование файла.

     @param origFile файл, для копирования
     @param s        строка путь
     @return удача/нет
     */
    @SuppressWarnings ("MethodWithMultipleReturnPoints")
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

    public static ConcurrentMap<String, String> readFiles(List<File> filesToRead) {
        Collections.sort(filesToRead);
        ConcurrentMap<String, String> readiedStrings = new ConcurrentHashMap<>();
        for(File f : filesToRead){
            String s = readFile(f.getAbsolutePath());
            readiedStrings.put(f.getAbsolutePath(), s);
        }
        return readiedStrings;
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

    public static void delFilePatterns(String[] patToDelArr) {
        File file = new File(".");
        for(String patToDel : patToDelArr){
            FileVisitor<Path> deleterTemp = new DeleterTemp(patToDel);
            try{
                Path walkFileTree = Files.walkFileTree(file.toPath(), deleterTemp);
                new MessageCons().infoNoTitles("walkFileTree = " + walkFileTree);
            }
            catch(IOException e){
                new MessageCons().errorAlert(CLASS_NAME, "delFilePatterns", e.getMessage());
            }
        }
    }

    public static void recFile(String fileNameLOG, String toWriteStr) {
        recFile(fileNameLOG + ConstantsFor.LOG, Collections.singletonList(toWriteStr));
    }

    /**
     Запись файла

     @param fileName  имя файла
     @param toFileRec {@link List} строчек на запись.
     */
    public static synchronized void recFile(String fileName, List<String> toFileRec) {
        try(OutputStream outputStream = new FileOutputStream(fileName);
            PrintWriter printWriter = new PrintWriter(outputStream, true)){
            printWriter.println(new Date(ConstantsFor.getAtomicTime()).toString());
            toFileRec.forEach(printWriter::println);
        }
        catch(IOException e){
            LOGGER.info(e.getMessage());
        }
    }

    public static List<String> readFileToList(String absolutePath) {
        LOGGER.warn("FileSystemWorker.readFileToList");
        List<String> retList = new ArrayList<>();
        try(InputStream inputStream = new FileInputStream(absolutePath);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader)){
            while(reader.ready()){
                retList.add(reader.readLine());
            }
        }
        catch(IOException e){
            new MessageCons().errorAlert(CLASS_NAME, "readFileToList", e.getMessage());
            FileSystemWorker.error("FileSystemWorker.readFileToList", e);
            retList.add(e.getMessage());
            retList.add(new TForms().fromArray(e, true));
        }
        new MessageCons().info("absolutePath = [" + absolutePath + "]", " input parameters.\nReturns:", "java.util.List<java.lang.String>");
        return retList;
    }

    public static void error(String classMeth, Exception e) {
        File f = new File(classMeth + ConstantsFor.LOG);
        try(OutputStream outputStream = new FileOutputStream(f);
            PrintStream printStream = new PrintStream(outputStream, true)){
            printStream.println(new Date(new TimeChecker().call().getReturnTime()));
            printStream.println();
            printStream.println();
            printStream.println(e.getMessage());
            printStream.println();
            printStream.println(new TForms().fromArray(e, false));
            printStream.println();
            printStream.println("Suppressed:");
            printStream.println();
            if(e.getSuppressed().length > 0){
                for(Throwable throwable : e.getSuppressed()){
                    printStream.println(throwable.getMessage());
                    printStream.println(new TForms().fromArray(throwable, false));
                }
            }
        }
        catch(IOException ex){
            LOGGER.error("FileSystemWorker.error", ex.getMessage(), ex);
        }
    }

}
