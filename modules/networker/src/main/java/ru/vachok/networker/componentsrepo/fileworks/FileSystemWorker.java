// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.fileworks;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.data.OpenSource;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


/**
 Вспомогательная работа с файлами.
 <p>
 
 @see ru.vachok.networker.componentsrepo.fileworks.FileSystemWorkerTest
 @since 19.12.2018 (9:57) */
@SuppressWarnings("ClassWithTooManyMethods")
public abstract class FileSystemWorker extends SimpleFileVisitor<Path> {
    
    
    private static MessageToUser messageToUser = MessageToUser.getInstance(ru.vachok.networker.restapi.message.MessageToUser.LOCAL_CONSOLE, FileSystemWorker.class.getSimpleName());
    
    public static String delTemp() {
        DeleterTemp deleterTemp = new DeleterTemp();
        try {
            Files.walkFileTree(Paths.get(ConstantsFor.ROOT_PATH_WITH_SEPARATOR), deleterTemp);
        }
        catch (IOException e) {
            messageToUser.error(FileSystemWorker.class.getSimpleName(), e.getMessage(), new TForms().fromArray(e, false));
        }
        return deleterTemp.toString();
    }
    
    public static @NotNull String copyOrDelFileWithPath(@NotNull File origFile, @NotNull Path absolutePathToCopy, boolean needDel) {
        Path pathToCopyFile = absolutePathToCopy;
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
    
    private static boolean copyFile(@NotNull File origFile, @NotNull Path absolutePathToCopy) {
        Path originalPath = Paths.get(origFile.getAbsolutePath());
        checkDirectoriesExists(absolutePathToCopy.toAbsolutePath().normalize());
        try {
            Path copyOkPath = Files.copy(originalPath, absolutePathToCopy, StandardCopyOption.REPLACE_EXISTING);
            File copiedFile = copyOkPath.toFile();
            copiedFile.setLastModified(System.currentTimeMillis());
            return copiedFile.exists();
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage() + " see line: 96");
            return false;
        }
    }
    
    private static boolean delOrig(final @NotNull File origFile) {
        try {
            if (!Files.deleteIfExists(origFile.toPath().toAbsolutePath().normalize())) {
                origFile.deleteOnExit();
            }
        }
        catch (IOException e) {
            boolean isDelete = origFile.delete();
            messageToUser.error(MessageFormat
                    .format("FileSystemWorker.delOrig says: {0}. Parameters: \n[origFile]: {1}\nisDelete: ", e.getMessage(), origFile.getAbsolutePath(), isDelete));
            origFile.deleteOnExit();
        }
        return origFile.exists();
    }
    
    private static void checkDirectoriesExists(@NotNull Path absolutePathToCopy) {
        try {
            Path parentPath = absolutePathToCopy.getParent();
            Files.createDirectories(parentPath);
            Files.deleteIfExists(absolutePathToCopy);
            messageToUser.warn(FileSystemWorker.class.getSimpleName(), "Copy to directory: ", absolutePathToCopy.toAbsolutePath().normalize().toString());
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage() + " see line: 124 " + new TForms().exceptionNetworker(e.getStackTrace()));
        }
    }
    
    public static @NotNull String error(String classMeth, Exception e) {
        File fileClassMeth = new File(classMeth + "_" + LocalTime.now().toSecondOfDay() + ".err");
        
        try (OutputStream outputStream = new FileOutputStream(fileClassMeth)) {
            boolean printTo = printTo(outputStream, e);
        }
        catch (IOException exIO) {
            messageToUser.error(MessageFormat
                    .format("FileSystemWorker.error: {1}\nParameters: [{3}, {0}]\nReturn: java.lang.String\nStack:\n{2}",
                            e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e), classMeth));
        }
    
        boolean isCp = FileSystemWorker.copyOrDelFile(fileClassMeth, Paths.get(".\\err\\" + fileClassMeth.getName()).toAbsolutePath().normalize(), true);
        return MessageFormat
                .format("{4} | {0} threw Exception ({3}): {1}: <p>\n{2}",
                        classMeth, e.getMessage(), new TForms().fromArray(e, true), e.getClass()
                                .getTypeName(), ConstantsFor.ROOT_PATH_WITH_SEPARATOR + "err" + ConstantsFor.FILESYSTEM_SEPARATOR + fileClassMeth);
    }
    
    private static boolean printTo(OutputStream outputStream, @NotNull Exception e) {
        try (PrintStream printStream = new PrintStream(outputStream, true)) {
            printStream.println(new Date());
            printStream.println();
            printStream.println(e.getClass().getSimpleName());
            if (e instanceof SQLException) {
                printStream.println(((SQLException) e).getErrorCode() + " " + ((SQLException) e).getSQLState() + " " + ((SQLException) e).getSQLState());
            }
            printStream.println(e.getMessage());
            printStream.println();
            printStream.println(new TForms().fromArray(e, false));
            return printStream.checkError();
        }
    }
    
    public static boolean copyOrDelFile(@NotNull File originalFile, @NotNull Path pathToCopy, boolean isNeedDelete) {
        boolean retBool = false;
        
        if (!originalFile.exists()) {
            throw new InvokeIllegalException("Can't copy! Original file not found : " + originalFile.getAbsolutePath());
        }
        if (isNeedDelete) {
            if (copyFile(originalFile, pathToCopy)) {
                retBool = delOrig(originalFile);
            }
        }
        else {
            retBool = copyFile(originalFile, pathToCopy.toAbsolutePath().normalize());
        }
        
        return retBool;
    }
    
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
    
    public static boolean writeFile(String fileName, @NotNull List<?> toFileRec) {
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
        try (OutputStream outputStream = new FileOutputStream(fileName);
             PrintStream printStream = new PrintStream(outputStream, true)
        ) {
            toFileRec.forEach(printStream::println);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("FileSystemWorker.writeFile: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        return file.exists();
    }
    
    public static @NotNull Queue<String> readFileToQueue(@NotNull Path filePath) {
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
    
    public static @NotNull Set<String> readFileToSet(@NotNull Path file) {
        return readFileToEncodedSet(file, "UTF-8");
    }
    
    public static @NotNull List<String> readFileToList(String absolutePath) {
        List<String> retList = new ArrayList<>();
        
        if (!new File(absolutePath).exists()) {
            System.err.println(absolutePath + " does not exists...");
        }
        else {
            try (InputStream inputStream = new FileInputStream(absolutePath);
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader reader = new BufferedReader(inputStreamReader)) {
                reader.lines().forEach(retList::add);
            }
            catch (IOException e) {
                messageToUser.errorAlert(FileSystemWorker.class.getSimpleName(), "readFileToList", e.getMessage());
                retList.add(e.getMessage());
                retList.add(new TForms().fromArray(e, true));
            }
        }
        return retList;
    }
    
    public static @NotNull Set<String> readFileToEncodedSet(Path file, String encoding) {
        Set<String> retSet = new HashSet<>();
        try (InputStream inputStream = new FileInputStream(file.toFile())) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName(encoding))) {
                try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    bufferedReader.lines().forEach(line->{
                        try {
                            retSet.add(line.split(" #")[0]);
                        }
                        catch (ArrayIndexOutOfBoundsException e) {
                            retSet.add(line);
                        }
                    });
                }
            }
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
    
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @OpenSource
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
    
    public static @NotNull Queue<String> readFileEncodedToQueue(@NotNull Path pathToFile, String encoding) {
        Queue retQueue = new LinkedBlockingQueue();
        if (!pathToFile.toFile().exists()) {
            retQueue.add(pathToFile.toFile().getAbsolutePath() + " is not exists");
            return retQueue;
        }
        else {
            try (InputStream inputStream = new FileInputStream(pathToFile.toAbsolutePath().normalize().toString());
                 InputStreamReader streamReader = new InputStreamReader(inputStream, encoding);
                 BufferedReader bufferedReader = new BufferedReader(streamReader)) {
                bufferedReader.lines().forEach(retQueue::add);
            }
            catch (IOException e) {
                retQueue.add(e.getMessage());
            }
        }
        return retQueue;
    }
    
    public abstract String packFiles(List<File> filesToZip, String zipName);
}
