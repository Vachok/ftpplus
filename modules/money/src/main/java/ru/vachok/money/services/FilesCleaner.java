package ru.vachok.money.services;


import org.slf4j.Logger;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.config.AppComponents;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 Очистилка файлов

 @since 24.11.2018 (8:48) */
public class FilesCleaner extends SimpleFileVisitor<Path> implements Callable<String> {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = FilesCleaner.class.getSimpleName();

    private static final Logger LOGGER = AppComponents.getLogger();

    private String startDir = "\\\\10.10.111.1\\Torrents-FTP";

    private PrintWriter printWriter;

    private boolean delFiles;

    private long bytesCount = 0;

    private File fileR = new File("torrents.csv");

    public String getStartDir() {
        return startDir;
    }


    /*Instances*/
    public FilesCleaner(String startDir, boolean delFiles) {
        this.startDir = startDir;
        this.delFiles = delFiles;
    }

    public FilesCleaner() {
        this.delFiles = false;
    }

    /*Itinial Block*/ {
        try{
            OutputStream outputStream = new FileOutputStream(fileR);
            printWriter = new PrintWriter(outputStream, true);
        }
        catch(FileNotFoundException e){
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public String call() throws Exception {
        try{
            Files.walkFileTree(Paths.get(startDir), this);
        }
        catch(IOException e){
            LOGGER.warn(e.getMessage(), e);
        }
        return readFile();
    }

    private String readFile() {
        Stack<String> stringStack = new Stack<>();
        try(InputStream inputStream = new FileInputStream(fileR);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader)){
            while(bufferedReader.ready()){
                stringStack.add(bufferedReader.readLine());
            }
            String retStr = "<a href=\"/cleandir\">" + startDir + "</a><br>" + new TForms().stackToString(stringStack, true);
            LOGGER.info(retStr);
            return retStr;
        }
        catch(IOException e){
            return new TForms().toStringFromArray(e, true);
        }
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        String logMsg = "Scanning dir - " + dir.toAbsolutePath().toString() + " modified at " + attrs.lastModifiedTime();
        LOGGER.info(logMsg);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        boolean oldFilesToFile = attrs.lastAccessTime().toMillis() < (System.currentTimeMillis() - TimeUnit
            .DAYS.toMillis(ConstantsFor.ONE_YEAR));
        String semiCol = ",";
        if(delFiles){
            Thread.currentThread().setPriority(1);
            Thread.currentThread().setName(startDir);
            if(attrs.isRegularFile() && oldFilesToFile){
                long length = file.toFile().length();
                this.bytesCount = bytesCount + length;
                String toString = new StringBuilder()
                    .append("File: ")
                    .append(semiCol)
                    .append(file.toAbsolutePath())
                    .append(semiCol)
                    .append("size in kbytes")
                    .append(semiCol)
                    .append(( float ) length / ConstantsFor.KILOBYTE)
                    .append(semiCol)
                    .append("Created")
                    .append(semiCol)
                    .append(attrs.creationTime())
                    .append(semiCol)
                    .append(attrs.fileKey()).append(" file key?")
                    .append(( float ) length / ConstantsFor.MEGABYTE)
                    .append(" Mbytes total")
                    .append("\n").toString();
                try{
                    Files.delete(file);
                    printWriter.println(toString);
                }
                catch(AccessDeniedException e){
                    printWriter.println("Denied. Reason: " + e.getReason() + ": " + file.toAbsolutePath());
                    return FileVisitResult.CONTINUE;
                }
            }
        }
        else{
            if(oldFilesToFile){
                printWriter.println(file.toAbsolutePath().toString() + semiCol + attrs.size() / ConstantsFor.MEGABYTE + semiCol +
                    attrs.lastAccessTime());
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        printWriter.println(file + "," + exc.getMessage());
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Iterable<FileStore> fileStoreIter = dir.getFileSystem().getFileStores();
        if(delFiles && !fileStoreIter.iterator().hasNext()){
            Files.delete(dir);
        }
        return FileVisitResult.CONTINUE;
    }
}