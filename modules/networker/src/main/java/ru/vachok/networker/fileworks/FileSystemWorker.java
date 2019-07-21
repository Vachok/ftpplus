// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.fileworks;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.*;
import java.nio.file.*;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


/**
 Вспомогательная работа с файлами.
 <p>
 
 @see ru.vachok.networker.fileworks.FileSystemWorkerTest
 @since 19.12.2018 (9:57) */
public abstract class FileSystemWorker extends SimpleFileVisitor<Path> {
    
    
    private static final String CLASS_NAME = FileSystemWorker.class.getSimpleName();
    
    private static MessageToUser messageToUser = new MessageLocal(FileSystemWorker.class.getSimpleName());
    
    private static Path pathToCopyFile = Paths.get(ConstantsFor.ROOT_PATH_WITH_SEPARATOR + "tmp" + ConstantsFor.FILESYSTEM_SEPARATOR);
    
    public static boolean writeFile(String fileName, @NotNull Stream<?> toFileRec) {
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
            Files.walkFileTree(Paths.get(ConstantsFor.ROOT_PATH_WITH_SEPARATOR), deleterTemp);
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
    public static @NotNull String copyOrDelFileWithPath(@NotNull File origFile, @NotNull Path absolutePathToCopy, boolean needDel) {
        pathToCopyFile = absolutePathToCopy;
        Path origPath = Paths.get(origFile.getAbsolutePath());
        StringBuilder stringBuilder = new StringBuilder();
    
        if (!absolutePathToCopy.getParent().toFile().exists()) {
            absolutePathToCopy = createDirs(absolutePathToCopy.getParent().toAbsolutePath().normalize());
            stringBuilder.append("Creating: ").append(absolutePathToCopy.toAbsolutePath().normalize()).append(ConstantsFor.STR_N);
        }
        if (origFile.exists()) {
            copyFile(origFile, absolutePathToCopy);
            stringBuilder.append("... copying ...");
        }
        else {
            stringBuilder.append("No original FILE! ").append(origFile.getName()).append(ConstantsFor.STR_N);
            stringBuilder.append("Exiting: ").append(new Date());
            return stringBuilder.toString();
        }
    
        if (needDel) {
            delOrig(origFile);
        }
        stringBuilder.append(origFile.getAbsolutePath()).append("->").append(absolutePathToCopy);
        long minusDelay = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY);
        stringBuilder.append(absolutePathToCopy.toAbsolutePath().normalize().toString());
        return stringBuilder.toString();
    }
    
    public static @NotNull String error(String classMeth, Exception e) {
        File fileClassMeth = new File(classMeth + "_" + LocalTime.now().toSecondOfDay() + ".err");
        
        try (OutputStream outputStream = new FileOutputStream(fileClassMeth)) {
            boolean printTo = printTo(outputStream, e);
            messageToUser.info(fileClassMeth.getAbsolutePath(), "print", String.valueOf(printTo));
        }
        catch (IOException exIO) {
            messageToUser.error(MessageFormat
                .format("FileSystemWorker.error: {1}\nParameters: [{3}, {0}]\nReturn: java.lang.String\nStack:\n{2}",
                    e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e), classMeth));
        }
    
        boolean isCp = FileSystemWorker.copyOrDelFile(fileClassMeth, Paths.get(".\\err\\" + fileClassMeth.getName()).toAbsolutePath().normalize(), true);
        return MessageFormat
            .format("{0} threw Exception ({3}): {1}: <p>\n{2}", classMeth, e.getMessage(), new TForms().fromArray(e, true), e.getClass().getTypeName());
    }
    
    /**
     Чтение файла из файловой системы.
     <p>
     
     @param fileName путь к файлу.
     @return файл, построчно.
     */
    public static @NotNull String readFile(String fileName) {
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
                    .append("\n<br>");
                while (bufferedReader.ready()) {
                    stringBuilder
                        .append(bufferedReader.readLine())
                        .append("\n<br>");
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
    
    /**
     Запись файла@param fileName  имя файла
     
     @param toFileRec {@link List} строчек на запись.
     */
    public static boolean writeFile(String fileName, @NotNull List<?> toFileRec) {
        try (OutputStream outputStream = new FileOutputStream(fileName);
             PrintWriter printWriter = new PrintWriter(outputStream, true)
        ) {
            toFileRec.forEach(printWriter::println);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat
                .format("FileSystemWorker.writeFile\n{0}: {1}\nParameters: [fileName, toFileRec]\nReturn: boolean\nStack:\n{2}", e.getClass().getTypeName(), e
                    .getMessage(), new TForms().fromArray(e)));
        }
        messageToUser.info(FileSystemWorker.class.getSimpleName(), fileName, "is written");
        return new File(fileName).exists();
    }
    
    public static Queue<String> readFileToQueue(@NotNull Path filePath) {
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
    
    public static String writeFile(String fileName, String toWriteStr) {
        if (writeFile(fileName, Collections.singletonList(toWriteStr))) {
            return new File(fileName).getAbsolutePath();
        }
        else {
            return String.valueOf(false);
        }
    }
    
    public static boolean copyOrDelFile(@NotNull File originalFile, @NotNull Path pathToCopy, boolean isNeedDelete) {
        copyFile(originalFile, pathToCopy.toAbsolutePath().normalize());
        if (!originalFile.exists()) {
            return false;
        }
        if (isNeedDelete) {
            try {
                Files.deleteIfExists(originalFile.toPath().toAbsolutePath());
            }
            catch (IOException e) {
                messageToUser.error(MessageFormat
                    .format("FileSystemWorker.copyOrDelFile\n{0}: {1}\nParameters: [{3}, {4}, {5}]\nReturn: boolean\nStack:\n{2}", e
                        .getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e), originalFile, pathToCopy, isNeedDelete));
                originalFile.deleteOnExit();
            }
        }
        return !originalFile.exists();
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
    
    public static Set<String> readFileToSet(@NotNull Path file) {
        Set<String> retSet = new HashSet<>();
        try (InputStream inputStream = new FileInputStream(file.toFile());
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {
            bufferedReader.lines().forEach(line->{
                try {
                    retSet.add(line.split(" #")[0]);
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    retSet.add(line);
                }
            });
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        return retSet;
    }
    
    public static boolean writeFile(String path, @NotNull Map<?, ?> map) {
        List toWriteList = new ArrayList();
        map.forEach((k, v)->{
            String addToListString = new StringBuilder().append(k).append(" ").append(v).toString();
            toWriteList.add(addToListString);
        });
        return writeFile(path, toWriteList.stream());
    }
    
    public static @NotNull String appendObjectToFile(@NotNull File fileForAppend, Object objectToAppend) {
        StringBuilder stringBuilder = new StringBuilder();
        try (OutputStream outputStream = new FileOutputStream(fileForAppend, true);
             PrintStream printStream = new PrintStream(outputStream, true)
        ) {
            printStream.println(objectToAppend);
            stringBuilder.append(fileForAppend.getAbsolutePath());
        }
        catch (IOException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, false));
        }
        return stringBuilder.toString();
    }
    
    /**
     Подсчёт строк в файле
     <p>
     
     @param filePath путь к файлу
     @return кол-во строк
     
     @see ru.vachok.networker.fileworks.FileSystemWorkerTest#testCountStringsInFile()
     */
    public static int countStringsInFile(Path filePath) {
        final long nanoTime = System.nanoTime();
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
        System.out.println(MessageFormat.format("nanoTime FileSystemWorker.countStringsInFile is {0} nanos", System.nanoTime() - nanoTime));
        final long startLines = System.nanoTime();
        System.out.println(MessageFormat.format("nanoTime FileSystemWorker.countStringsInFileAsStream is {0} nanos", System.nanoTime() - startLines));
        return stringsCounter;
    }
    
    public static Stream<String> readFileAsStream(Path normalize, long stringsLimit) {
        try (InputStream inputStream = new FileInputStream(normalize.toFile());
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {
            if (stringsLimit <= 0) {
                stringsLimit = Long.MAX_VALUE;
            }
            return bufferedReader.lines().limit(stringsLimit);
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        throw new InvokeIllegalException("Can't read file");
    }
    
    public abstract String packFiles(List<File> filesToZip, String zipName);
    
    private static boolean copyFile(@NotNull File origFile, @NotNull Path absolutePathToCopy) {
        Path origPath = Paths.get(origFile.getAbsolutePath());
        Path copiedPath = Paths.get(new StringBuilder()
            .append(".").append(ConstantsFor.FILESYSTEM_SEPARATOR).append("tmp").toString());
        File copyPathDir = absolutePathToCopy.getParent().toFile();
        if (!copyPathDir.exists() || !copyPathDir.isDirectory()) {
            try {
                Files.createDirectories(copyPathDir.toPath().toAbsolutePath().normalize());
            }
            catch (IOException e) {
                messageToUser.error(MessageFormat
                    .format("FileSystemWorker.copyFile\n{0}: {1}\nParameters: [origFile, absolutePathToCopy]\nReturn: boolean\nStack:\n{2}", e.getClass()
                        .getTypeName(), e.getMessage(), new TForms().fromArray(e)));
            }
        }
        if (origPath.toFile().exists()) {
            try {
                copiedPath = Files.copy(origPath.toAbsolutePath().normalize(), absolutePathToCopy, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException e) {
                messageToUser.error(MessageFormat
                    .format("FileSystemWorker.copyFile threw away: {0}, ({1}).\n\n{2}", e.getMessage(), e.getClass().getName(), new TForms().fromArray(e)));
            }
        }
        else {
            messageToUser.warn("copyFile", origFile.getAbsolutePath(), "is " + false);
            return false;
        }
        
        long oneMinuteAgo = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1);
        boolean isCopied = absolutePathToCopy.toFile().exists() & (absolutePathToCopy.toFile().lastModified() > oneMinuteAgo);
        messageToUser.info(MessageFormat.format("Copy {0} -> {1} is {2} \n", origFile.getAbsolutePath(), copiedPath, isCopied));
        return isCopied;
    }
    
    private static void delOrig(final @NotNull File origFile) {
        try {
            if (!Files.deleteIfExists(origFile.toPath().toAbsolutePath().normalize())) {
                origFile.deleteOnExit();
                System.out.println(origFile.getAbsolutePath() + "<- X");
            }
        }
        catch (IOException e) {
            boolean isDelete = origFile.delete();
            messageToUser.error(MessageFormat
                .format("FileSystemWorker.delOrig says: {0}. Parameters: \n[origFile]: {1}\nisDelete: ", e.getMessage(), origFile.getAbsolutePath(), isDelete));
            origFile.deleteOnExit();
        }
    }
    
    private static Path createDirs(Path absNormPath) {
        Path directories = null;
        try {
            directories = Files.createDirectories(absNormPath);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("FileSystemWorker.createDirs says: {0}. Parameters: {1}", e.getMessage(), absNormPath));
        }
        return directories;
    }
    
    private static boolean printTo(OutputStream outputStream, @NotNull Exception e) {
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
