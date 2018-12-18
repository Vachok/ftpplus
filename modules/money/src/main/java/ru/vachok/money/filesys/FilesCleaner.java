package ru.vachok.money.filesys;


import org.slf4j.Logger;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.config.AppComponents;
import ru.vachok.money.services.TForms;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 Очистилка файлов

 @since 24.11.2018 (8:48) */
public class FilesCleaner extends SimpleFileVisitor<Path> implements Callable<String> {

    /*Fields*/
    private static final Logger LOGGER = AppComponents.getLogger();

    private String startDir = "\\\\10.10.111.1\\Torrents-FTP";

    private PrintWriter printWriter;

    private long bytesCount = 0;

    private int filesCount = 0;

    private File fileR = new File("111.1.del");

    public String getStartDir() {
        return startDir;
    }

    /*Instances*/
    public FilesCleaner(String startDir) {
        this.startDir = startDir;
    }

    public FilesCleaner() {
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

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        boolean oldFilesToFile = attrs.lastAccessTime().toMillis() < (System.currentTimeMillis() - TimeUnit
            .DAYS.toMillis(ConstantsFor.ONE_YEAR));
        String semiCol = ",";
        Thread.currentThread().setPriority(1);
        Thread.currentThread().setName(startDir);
        long length = file.toFile().length();
        bytesCount = bytesCount + length;
        String toString = new StringBuilder()
            .append("File (")
            .append(filesCount)
            .append(") ")
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
            .append(file.toFile().getTotalSpace() / ConstantsFor.MEGABYTE)
            .append(" MBYTES on disk free")
            .append(semiCol)
            .append("\n")
            .append(( float ) bytesCount / ConstantsFor.MEGABYTE)
            .append(" Mbytes total deleted")
            .append("\n").toString();
        if(attrs.isRegularFile()){
            this.bytesCount = bytesCount + length;
            try{
                Files.deleteIfExists(file.toAbsolutePath());
                this.filesCount = this.filesCount + 1;
            }
            catch(AccessDeniedException e){
                file.toFile().deleteOnExit();
                this.filesCount = this.filesCount + 1;
                if(file.toFile().exists()){
                    file.toFile().deleteOnExit();
                }
            }
        }
        else{
            if(oldFilesToFile){
                String msg = true + " oldFilesToFile\n" + file.toString();
                LOGGER.info(msg);
                printWriter.println(filesCount + ") " + file.toAbsolutePath().toString() + semiCol + attrs.size() / ConstantsFor.MEGABYTE + semiCol +
                    attrs.lastAccessTime());
            }
        }
        printWriter.println(toString);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        int filesInDir = dir.toFile().listFiles().length;
        String logMsg = "Scanning dir - " + dir.toAbsolutePath().toString() + ". Files: " + filesInDir +
            "\nmodified at " + attrs.lastModifiedTime();
        LOGGER.info(logMsg);
        return FileVisitResult.CONTINUE;
    }

    private String readFile() {
        Deque<String> stringArrayDeque = new ArrayDeque<>();
        try(InputStream inputStream = new FileInputStream(fileR);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader)){
            while(bufferedReader.ready()){
                stringArrayDeque.add(bufferedReader.readLine());
            }
            String retStr = "<a href=\"/cleandir\">" + startDir + "</a><br>" + new TForms().toStringFromArray(stringArrayDeque, true);
            LOGGER.info(retStr);
            return retStr;
        }
        catch(IOException e){
            return new TForms().toStringFromArray(e, true);
        }
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        String msg = file + "," + exc.getMessage();
        LOGGER.warn(msg);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        try{
            if(Objects.requireNonNull(dir.toFile().listFiles()).length <= 0){
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        }
        catch(FileSystemException e){
            printWriter.println(e.getMessage());
        }
        return FileVisitResult.CONTINUE;
    }
}