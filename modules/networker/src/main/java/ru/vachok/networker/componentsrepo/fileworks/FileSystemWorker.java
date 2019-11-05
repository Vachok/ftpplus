// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.fileworks;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.data.OpenSource;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;


/**
 Вспомогательная работа с файлами.
 <p>
 
 @see ru.vachok.networker.componentsrepo.fileworks.FileSystemWorkerTest
 @since 19.12.2018 (9:57) */
@SuppressWarnings("ClassWithTooManyMethods")
public abstract class FileSystemWorker extends SimpleFileVisitor<Path> {
    
    
    private static final MessageToUser messageToUser = MessageToUser
            .getInstance(ru.vachok.networker.restapi.message.MessageToUser.LOCAL_CONSOLE, FileSystemWorker.class.getSimpleName());
    
    private static final Pattern PATT = Pattern.compile(" #");
    
    public static String delTemp() {
        DeleterTemp deleterTemp = new DeleterTemp();
        try {
            Files.walkFileTree(Paths.get(ConstantsFor.ROOT_PATH_WITH_SEPARATOR), deleterTemp);
        }
        catch (IOException e) {
            messageToUser.error(FileSystemWorker.class.getSimpleName(), e.getMessage(), AbstractForms.fromArray(e));
        }
        return deleterTemp.toString();
    }
    
    public static @NotNull String copyOrDelFileWithPath(@NotNull File origFile, @NotNull Path absolutePathToCopy, boolean needDel) {
        Path origPath = Paths.get(origFile.getAbsolutePath());
        StringBuilder stringBuilder = new StringBuilder();
    
        checkDirectoriesExists(absolutePathToCopy);
    
        stringBuilder.append("Creating: ").append(absolutePathToCopy.toAbsolutePath().normalize()).append(ConstantsFor.STR_N);
        
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
    
    public static @NotNull String readFile(File file) {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = new FileInputStream(file.getAbsolutePath())) {
            int available = inputStream.available();
            byte[] bytes = new byte[available];
            while (inputStream.available() > 0) {
                inputStream.read(bytes, 0, available);
            }
            stringBuilder.append(new String(bytes));
        }
        catch (IOException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(AbstractForms.fromArray(e));
        }
        return stringBuilder.toString();
    }
    
    public static @NotNull String error(String classMeth, @NotNull Exception e) {
        Path rootFldr = Paths.get(".").toAbsolutePath().normalize();
        String errFldr = rootFldr.toString() + ConstantsFor.FILESYSTEM_SEPARATOR + "err";
        File fileClassMeth = new File(errFldr + ConstantsFor.FILESYSTEM_SEPARATOR + classMeth + "_" + LocalTime.now().toSecondOfDay() + ".err");
        String result = fileClassMeth.getAbsolutePath();
        try (OutputStream outputStream = new FileOutputStream(fileClassMeth);
             PrintStream printStream = new PrintStream(outputStream, true)) {
            printStream.println(e.getMessage());
            printStream.println();
            printStream.println(AbstractForms.exceptionNetworker(e.getStackTrace()));
            printStream.println("****");
            printStream.println(new Date());
            printStream.println(AbstractForms.exceptionNetworker(Thread.currentThread().getStackTrace()));
            messageToUser.info(FileSystemWorker.class.getSimpleName(), "printed error: ", String.valueOf(printStream.checkError()));
        }
        catch (IOException exIO) {
            result = MessageFormat.format("FileSystemWorker.error:\n{0}, {1}", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace()));
        }
        return result;
    }
    
    public static boolean copyOrDelFile(@NotNull File originalFile, @NotNull Path pathToCopy, boolean isNeedDelete) {
        boolean retBool = false;
        
        if (!originalFile.exists()) {
            throw new InvokeIllegalException(MessageFormat.format("Can''t copy! Original file not found : {0}\n{1}", originalFile.getAbsolutePath(), AbstractForms
                    .exceptionNetworker(Thread.currentThread().getStackTrace())));
        }
        if (isNeedDelete) {
            if (copyFile(originalFile, pathToCopy)) {
                delOrig(originalFile);
                retBool = pathToCopy.toFile().exists();
            }
        }
        else {
            retBool = copyFile(originalFile, pathToCopy.toAbsolutePath().normalize());
        }
        return retBool;
    }
    
    @Contract("_ -> new")
    public static @NotNull String readRawFile(@NotNull String file) {
        byte[] bytes;
        try (InputStream inputStream = new FileInputStream(file)) {
            bytes = new byte[inputStream.available()];
            int readBytes = 0;
            while (inputStream.available() > 0) {
                readBytes = inputStream.read(bytes, 0, inputStream.available());
            }
            messageToUser.info(FileSystemWorker.class.getSimpleName(), "readRawFile", MessageFormat
                .format("{0} readied {1} kilobytes.", file, readBytes / ConstantsFor.KBYTE));
        }
        catch (IOException e) {
            messageToUser.error(FileSystemWorker.class.getSimpleName(), e.getMessage(), " see line: 136 ***");
            bytes = AbstractForms.exceptionNetworker(e.getStackTrace()).getBytes();
        }
        return new String(bytes);
    }
    
    private static boolean copyFile(@NotNull File origFile, @NotNull Path absolutePathToCopy) {
        Path originalPath = Paths.get(origFile.getAbsolutePath());
        checkDirectoriesExists(absolutePathToCopy.toAbsolutePath().normalize());
        Path copyOkPath = Paths.get("null");
        try {
            copyOkPath = Files.copy(originalPath, absolutePathToCopy, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage() + " see line: 96");
        }
        File copiedFile = copyOkPath.toFile();
        copiedFile.setLastModified(System.currentTimeMillis());
        return copiedFile.exists();
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
            stringBuilder.append(e.getMessage()).append("\n").append(AbstractForms.fromArray(e));
        }
        return stringBuilder.toString();
    }
    
    private static void delOrig(final @NotNull File origFile) {
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
        origFile.exists();
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
    
    public static @NotNull Set<String> readFileToSet(@NotNull Path file) {
        return readFileToEncodedSet(file, "UTF-8");
    }
    
    public static @NotNull Set<String> readFileToEncodedSet(@NotNull Path file, String encoding) {
        Thread.currentThread().checkAccess();
        Thread.currentThread().setPriority(2);
        Thread.currentThread().setName(MessageFormat.format("{1}ToSet:{0}", file.getFileName(), Thread.currentThread().getPriority()));
        Set<String> retSet = new HashSet<>();
        try (InputStream inputStream = new FileInputStream(file.toFile())) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName(encoding))) {
                try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    bufferedReader.lines().forEach(line->{
                        try {
                            retSet.add(line.split(PATT.pattern())[0]);
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
    
    public static boolean writeFile(String path, @NotNull Map<?, ?> map) {
        List toWriteList = new ArrayList();
        map.forEach((k, v)->{
            String addToListString = new StringBuilder().append(k).append(" ").append(v).toString();
            toWriteList.add(addToListString);
        });
        return writeFile(path, toWriteList.stream());
    }
    
    public static boolean writeFile(String fileName, @NotNull Stream<?> toFileRec) {
        Path normalPath = Paths.get(fileName).toAbsolutePath().normalize();
    
        checkDirectoriesExists(normalPath);
        
        try (OutputStream outputStream = new FileOutputStream(fileName);
             PrintStream printStream = new PrintStream(outputStream, true)
        ) {
            toFileRec.forEach(printStream::println);
            return true;
        }
        catch (IOException e) {
            messageToUser.warn(FileSystemWorker.class.getSimpleName(), "writeFile", e.getMessage() + " see line: 347");
            return false;
        }
    }
    
    private static void checkDirectoriesExists(@NotNull Path absolutePathToCopy) {
        try {
            Path parentPath = absolutePathToCopy.getParent();
            if (!parentPath.toFile().exists() || !parentPath.toFile().isDirectory()) {
                Files.createDirectories(parentPath);
            }
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage() + " see line: 124 " + AbstractForms.exceptionNetworker(e.getStackTrace()));
        }
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
    
    public abstract String findBiggestFile(Path inThePath);
}
