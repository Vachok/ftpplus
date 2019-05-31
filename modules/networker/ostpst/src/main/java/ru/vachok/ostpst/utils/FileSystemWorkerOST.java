// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.utils;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.fileworks.RNDPSTFileCopy;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.stream.Stream;


/**
 @since 07.05.2019 (15:27) */
public abstract class FileSystemWorkerOST {
    
    
    private static MessageToUser messageToUser = new MessageCons(FileSystemWorkerOST.class.getSimpleName());
    
    public static String error(String fileName, Exception e) {
        Path rootPath = Paths.get(".").toAbsolutePath().normalize();
        String toString = rootPath.toAbsolutePath().normalize().toString();
        rootPath = Paths.get(toString + ConstantsOst.SYSTEM_SEPARATOR + fileName);
        String fromArray = new TFormsOST().fromArray(e);
        writeStringToFile(rootPath.toString(), fromArray);
        return e.getMessage() + " details: " + rootPath.toAbsolutePath();
    }
    
    public static String writeFile(String fileName, Stream<String> stream) {
        Path pathWritten = Paths.get(fileName);
        StringBuilder stringBuilder = new StringBuilder();
        try (OutputStream outputStream = new FileOutputStream(pathWritten.toAbsolutePath().toString())) {
            stream.forEach(x->{
                x = x + "\n";
                try {
                    outputStream.write(x.getBytes());
                }
                catch (IOException e) {
                    stringBuilder.append(e.getMessage()).append("\n").append(new TFormsOST().fromArray(e));
                }
            });
            stringBuilder.append("Strings written to: ").append(pathWritten.toAbsolutePath().normalize());
        }
        catch (IOException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TFormsOST().fromArray(e));
        }
        return stringBuilder.toString();
    }
    
    public static Path writeMapToFile(String fileName, Map<?, ?> map) {
        Path pathWrite = Paths.get(fileName).toAbsolutePath();
        StringBuilder stringBuilder = new StringBuilder();
        try (OutputStream outputStream = new FileOutputStream(pathWrite.toFile())) {
            map.forEach((x, y)->{
                try {
                    outputStream.write((x + " : " + y).getBytes());
                    outputStream.write("\n".getBytes());
                }
                catch (IOException e) {
                    stringBuilder.append(e.getMessage()).append("\n").append(new TFormsOST().fromArray(e));
                }
            });
            stringBuilder.append(pathWrite);
        }
        catch (IOException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TFormsOST().fromArray(e));
        }
        return pathWrite.toAbsolutePath();
    }
    
    public static String getTestPST() {
        String tmpFileName = Paths.get("tmp_t.p.magdich.pst").toAbsolutePath().normalize().toString();
        if (!new File(tmpFileName).exists()) {
            RNDPSTFileCopy RNDPSTFileCopy = new RNDPSTFileCopy("\\\\192.168.14.10\\IT-Backup\\Mailboxes_users\\t.p.magdich.pst");
            String copyStat = RNDPSTFileCopy.copyFile("n");
            System.out.println(copyStat);
        }
        return tmpFileName;
    }
    
    public static String appendStringToFile(String fileName, String toAppend) {
        try (OutputStream outputStream = new FileOutputStream(fileName, true);
             PrintStream printStream = new PrintStream(outputStream, true, "UTF-8")
        ) {
            printStream.println();
            printStream.println(toAppend);
            return "Check err: " + printStream.checkError() + ". File: " + fileName + ", string appended: " + toAppend;
        }
        catch (IOException e) {
            return e.getMessage() + "\n" + new TFormsOST().fromArray(e);
        }
    }
    
    public static String readFileToString(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = new FileInputStream(fileName);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {
            while (inputStreamReader.ready()) {
                stringBuilder.append(bufferedReader.readLine());
            }
        }
        catch (IOException e) {
            stringBuilder.append(e.getMessage() + " " + FileSystemWorkerOST.class.getSimpleName());
        }
        return stringBuilder.toString();
    }
    
    private static String getDelimiter() {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return "\\";
        }
        else {
            return "/";
        }
    }
    
    public static Path writeStringToFile(String fileName, String stringToWrite) {
        Path pathToWrite = Paths.get(fileName);
        try {
            pathToWrite = Files.write(pathToWrite, stringToWrite.getBytes(), StandardOpenOption.CREATE);
            return pathToWrite.normalize().toAbsolutePath();
        }
        catch (IOException e) {
            e.printStackTrace();
            return Paths.get(".").toAbsolutePath().normalize();
        }
    }
    
    public static String getSeparator() {
        if (System.getProperty("os.name").contains("indows")) {
            return "\\";
        }
        else {
            return "/";
        }
    }
}
