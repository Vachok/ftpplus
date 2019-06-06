// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.fileworks;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;

import java.io.*;
import java.nio.file.*;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import static ru.vachok.networker.ConstantsFor.FILEEXT_LOG;


/**
 Вспомогательная работа с файлами.

 @since 19.12.2018 (9:57) */
public abstract class FileSystemWorker extends SimpleFileVisitor<Path> {
    
    
    private static final String CLASS_NAME = FileSystemWorker.class.getSimpleName();

    private static MessageToUser messageToUser = new MessageCons(FileSystemWorker.class.getSimpleName());
    
    
    public static boolean writeFile(String fileName, Stream<?> toFileRec) {
        try (OutputStream outputStream = new FileOutputStream(fileName);
             PrintStream printStream = new PrintStream(outputStream, true)
        ) {
            printStream.println();
            toFileRec.forEach(printStream::println);
            return true;
        }
        catch (IOException e) {
            messageToUser.errorAlert("FileSystemWorker", "writeFile", e.getMessage());
            return false;
        }
    }
    
    
    public static String delTemp() {
        DeleterTemp deleterTemp = new DeleterTemp();
        try {
            Files.walkFileTree(Paths.get("."), deleterTemp);
        }
        catch (IOException e) {
            messageToUser.error(FileSystemWorker.class.getSimpleName(), e.getMessage(), new TForms().fromArray(e, false));
        }
        return new TForms().fromArray(deleterTemp.getEventList(), false);
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
        String folderToSearch = "";
        try {
            folderToSearch = folderPath[1];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            folderToSearch = "";
        }
        folderToSearch = "\\\\srv-fs.eatmeat.ru\\common_new\\" + folderToSearch;
        try {
            Files.walkFileTree(Paths.get(folderToSearch), fileSearcher);
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        List<String> fileSearcherResList = fileSearcher.getResList();
        fileSearcherResList.add("Searched: " + new Date() + "\n");
        String resTo = new TForms().fromArray(fileSearcherResList, true);
        if (fileSearcherResList.size() > 0) {
            writeFile("search_" + LocalTime.now().toSecondOfDay() + ".res", fileSearcherResList.stream());
        }
        return resTo;
    }
    
    
    /**
     Простое копирование файла.

     @param origFile файл, для копирования
     @param pathToCopyWithFileName строка путь
     @param needDel удалить или нет исходник
     @return удача/нет
     */
    public static boolean copyOrDelFile(File origFile, String pathToCopyWithFileName, boolean needDel) {
        File toCpFile = new File(pathToCopyWithFileName);
        try {
            Path targetPath = toCpFile.toPath();
            Path directories = Files.createDirectories(targetPath.getParent());
            toCpFile = targetPath.toFile();
            Path copy = Files.copy(origFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            if (needDel && !Files.deleteIfExists(origFile.toPath())) {
                origFile.deleteOnExit();
            }
            String msg = directories + " getParent directory. " + copy + " " + toCpFile.exists();
            messageToUser.info(msg);
        }
        catch (IOException | NullPointerException e) {
            if (toCpFile.exists()) {
                toCpFile.deleteOnExit();
                messageToUser.warn(toCpFile.getName(), "will be delete On Exit", " = " + e.getMessage());
            }
        }
        return toCpFile.exists();
    }
    
    
    public static ConcurrentMap<String, String> readFiles(List<File> filesToRead) {
        Collections.sort(filesToRead);
        ConcurrentMap<String, String> readiedStrings = new ConcurrentHashMap<>();
        for (File f : filesToRead) {
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
        if (exists) {
            try (InputStream inputStream = new FileInputStream(fileName);
                 InputStreamReader reader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(reader)
            ) {
                int avaBytes = inputStream.available();
                stringBuilder
                    .append("Bytes in stream: ")
                    .append(avaBytes)
                    .append("<br\n>");
                while (bufferedReader.ready()) {
                    stringBuilder
                        .append(bufferedReader.readLine())
                        .append("<br>\n");
                }
            }
            catch (IOException e) {
                stringBuilder.append(e.getMessage());
            }
        }
        else {
            stringBuilder
                .append("File: ")
                .append(fileName)
                .append(" does not exists!");
        }
        String msgTimeSp = new StringBuilder()
            .append("FileSystemWorker.readFile: ")
            .append((float) (System.currentTimeMillis() - stArt) / 1000)
            .append(ConstantsFor.STR_SEC_SPEND)
            .toString();
        messageToUser.info(msgTimeSp);
        return stringBuilder.toString();
    }
    
    
    public static void delFilePatterns(String[] patToDelArr) {
        File file = new File(".");
        for (String patToDel : patToDelArr) {
            FileVisitor<Path> deleterTemp = new DeleterTemp(patToDel);
            try {
                Path walkFileTree = Files.walkFileTree(file.toPath(), deleterTemp);
                messageToUser.infoNoTitles("walkFileTree = " + walkFileTree);
            }
            catch (IOException e) {
                messageToUser.errorAlert(CLASS_NAME, "delFilePatterns", e.getMessage());
            }
        }
    }
    
    
    public static String writeFile(String fileName, String toWriteStr) {
        if (writeFile(fileName, Collections.singletonList(toWriteStr))) {
            return new File(fileName).getAbsolutePath();
        }
        else {
            return String.valueOf(false);
        }
    }
    
    /**
     Запись файла@param fileName  имя файла
     
     @param toFileRec {@link List} строчек на запись.
     */
    public static boolean writeFile(String fileName, List<?> toFileRec) {
        try (OutputStream outputStream = new FileOutputStream(fileName);
             PrintWriter printWriter = new PrintWriter(outputStream, true)
        ) {
            toFileRec.forEach(printWriter::println);
        }
        catch (IOException e) {
            messageToUser.error(FileSystemWorker.class.getSimpleName(), e.getMessage(), new TForms().fromArray(e, false));
        }
        return new File(fileName).exists();
    }
    
    
    public static List<String> readFileToList(String absolutePath) {
        List<String> retList = new ArrayList<>();
    
        if (!new File(absolutePath).exists()) {
            System.err.println(absolutePath + " does not exists...");
        }
        else {
            try (InputStream inputStream = new FileInputStream(absolutePath);
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader reader = new BufferedReader(inputStreamReader)
            ) {
                while (reader.ready()) {
                    retList.add(reader.readLine());
                }
            }
            catch (IOException e) {
                messageToUser.errorAlert(CLASS_NAME, "readFileToList", e.getMessage());
                retList.add(e.getMessage());
                retList.add(new TForms().fromArray(e, true));
            }
        }
        return retList;
    }
    
    public static Queue<String> readFileToQueue(Path filePath) {
        Queue<String> retQueue = new LinkedList<>();
        try (InputStream inputStream = new FileInputStream(filePath.toFile());
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {
            bufferedReader.lines().forEach(retQueue::add);
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        return retQueue;
    }
    
    public static String error(String classMeth, Exception e) {
        File f = new File(classMeth + "_" + LocalTime.now().toSecondOfDay() + FILEEXT_LOG);
        
        try (OutputStream outputStream = new FileOutputStream(f)) {
            boolean printTo = printTo(outputStream, e);
            messageToUser.info(f.getAbsolutePath(), "print", String.valueOf(printTo));
        }
        catch (IOException exIO) {
            messageToUser.errorAlert(CLASS_NAME, "error", exIO.getMessage());
        }
        boolean isCp = copyOrDelFile(f, ".\\err\\" + f.getName(), true);
        return classMeth + " threw Exception: " + e.getMessage() + ": <p>\n\n" + new TForms().fromArray(e, true);
    }
    
    public static Set<String> readFileToSet(Path file) {
        Set<String> retSet = new HashSet<>();
        try (InputStream inputStream = new FileInputStream(file.toFile());
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {
            bufferedReader.lines().forEach(x->{
                try {
                    retSet.add(x.split(" #")[0]);
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    retSet.add(x);
                }
            });
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        return retSet;
    }
    
    private static boolean printTo(OutputStream outputStream, Exception e) {
        try (PrintStream printStream = new PrintStream(outputStream, true)) {
            printStream.println(new Date());
            printStream.println();
            printStream.println(e.getMessage());
            printStream.println();
            printStream.println(new TForms().fromArray(e, false));
            return printStream.checkError();
        }
    }
}
