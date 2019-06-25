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
import java.util.stream.Stream;

import static ru.vachok.networker.ConstantsFor.FILEEXT_LOG;


/**
 Вспомогательная работа с файлами.
 <p>
 @see ru.vachok.networker.fileworks.FileSystemWorkerTest
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
     Простое копирование файла.
 
     @param origFile файл, для копирования
     @param absolutePathToCopy строка путь
     @param needDel удалить или нет исходник
     @return удача/нет
     */
    public static boolean copyOrDelFile(File origFile, Path absolutePathToCopy, boolean needDel) {
        File toCpFile = absolutePathToCopy.toAbsolutePath().normalize().toFile();
        
        try {
            Path parentPath = absolutePathToCopy.getParent();
            Path directories;
            if (!parentPath.toFile().exists()) {
                directories = Files.createDirectories(parentPath);
            }
            else {
                directories = parentPath;
            }
            Path copy = Files.copy(origFile.toPath().toAbsolutePath().normalize(), absolutePathToCopy, StandardCopyOption.REPLACE_EXISTING);
    
            if (needDel && !Files.deleteIfExists(origFile.toPath().toAbsolutePath().normalize())) {
                origFile.deleteOnExit();
            }
            String msg = directories + " getParent directory. " + copy + " " + toCpFile.exists();
            messageToUser.info(msg);
        }
        catch (IOException | NullPointerException e) {
            messageToUser.error(e.getMessage());
        }
        return toCpFile.exists();
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
                    .append("<p>\n");
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
        messageToUser.info(FileSystemWorker.class.getSimpleName(), fileName, "is written");
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
        File fileClassMeth = new File(classMeth + "_" + LocalTime.now().toSecondOfDay() + FILEEXT_LOG);
    
        try (OutputStream outputStream = new FileOutputStream(fileClassMeth)) {
            boolean printTo = printTo(outputStream, e);
            messageToUser.info(fileClassMeth.getAbsolutePath(), "print", String.valueOf(printTo));
        }
        catch (IOException exIO) {
            messageToUser.errorAlert(CLASS_NAME, ConstantsFor.RETURN_ERROR, exIO.getMessage());
        }
        boolean isCp = copyOrDelFile(fileClassMeth, Paths.get(".\\err\\" + fileClassMeth.getName()).toAbsolutePath().normalize(), true); //todo check 24.06.2019 (11:01)
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
    
    public static boolean writeFile(String path, Map<?, ?> map) {
        List toWriteList = new ArrayList();
        map.forEach((k, v)->{
            String addToListString = new StringBuilder().append(k).append(" ").append(v).toString();
            toWriteList.add(addToListString);
        });
        return writeFile(path, toWriteList.stream());
    }
    
    public static void appendObjToFile(File fileForAppend, Object objectToAppend) {
        try (OutputStream outputStream = new FileOutputStream(fileForAppend, true);
             PrintStream printStream = new PrintStream(outputStream, true)
        ) {
            printStream.println(objectToAppend);
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
    }
    
    /**
     Подсчёт строк в файле
     <p>
     
     @param filePath путь к файлу
     @return кол-во строк
     
     @see ru.vachok.networker.fileworks.FileSystemWorkerTest#testCountStringsInFile()
     */
    public static int countStringsInFile(Path filePath) {
        int stringsCounter = 0;
        try (InputStream is = new BufferedInputStream(new FileInputStream(filePath.toAbsolutePath().normalize().toString()))) {
            byte[] bufferBytes = new byte[ConstantsFor.KBYTE];
            int readChars = is.read(bufferBytes);
            if (readChars == -1) {
                // bail out if nothing to read
                return 0;
            }
            
            // make it easy for the optimizer to tune this loop
            while (readChars == ConstantsFor.KBYTE) {
                for (int i = 0; i < ConstantsFor.KBYTE; ) {
                    if (bufferBytes[i++] == '\n') {
                        ++stringsCounter;
                    }
                }
                readChars = is.read(bufferBytes);
            }
            
            // stringsCounter remaining characters
            while (readChars != -1) {
                System.out.println(readChars);
                for (int i = 0; i < readChars; ++i) {
                    if (bufferBytes[i] == '\n') {
                        ++stringsCounter;
                    }
                }
                readChars = is.read(bufferBytes);
            }
            
            return stringsCounter == 0 ? 1 : stringsCounter;
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        return stringsCounter;
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
