package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 Сортировщик файлов.
 @since 22.11.2018 (14:53) */
@Service
public class ArchivesSorter extends SimpleFileVisitor<Path> implements Callable<String> {

    /*Fields*/
    private static final Logger LOGGER = AppComponents.getLogger();

    /**
     Размер <b>BLU RAY</b> диска в байтах.
     */
    private static final long BLURAY_SIZE_BYTES = ConstantsFor.GBYTE * 49;

    private PrintWriter printWriter;

    private String fileName = "files_2.5_years_old_25mb.csv";

    private String fileNameNoAccess;

    private boolean yearNoAccess;
  
    private String date;

    private String startPath;

    private long dirsCounter = 0L;

    private long filesCounter = 0L;

    private long failCounter = 0L;

    private StringBuilder msgBuilder = new StringBuilder();

    public String getFileNameNoAccess() {
        return fileNameNoAccess;
    }

    public void setFileNameNoAccess(String fileNameNoAccess) {
        this.fileNameNoAccess = fileNameNoAccess;
        this.yearNoAccess = true;
    }

    public String getStartPath() {
        return startPath;
    }

    public void setStartPath(String startPath) {
        this.startPath = startPath;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    /*Instances*/
    public ArchivesSorter() {
    }

    ArchivesSorter(String startPath) {
        super();
        this.startPath = startPath;
    }

    {
        try{
            if(!yearNoAccess){
                OutputStream outputStream = new FileOutputStream(fileName);
                printWriter = new PrintWriter(outputStream, true);
            }
            else{
                OutputStream outputStream = new FileOutputStream(fileNameNoAccess);
                printWriter = new PrintWriter(outputStream, true);
            }
        }
        catch(FileNotFoundException e){
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public String call() {
        LOGGER.warn("ArchivesSorter.call");
        try{
            Files.walkFileTree(Paths.get(startPath), AppComponents.archivesSorter());
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
        return fileRead();
    }

    private String fileRead() {
        try(InputStream inputStream = new FileInputStream(fileName);
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader)){
            while(bufferedReader.ready()){
                msgBuilder
                    .append("<br>")
                    .append(bufferedReader.readLine());
            }
            return msgBuilder.toString();
        }
        catch(IOException e){
            return e.getMessage();
        }
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        this.dirsCounter = dirsCounter + 1;
        String toString = dir.toString() + " " + dirsCounter + " dirs\n" + attrs.fileKey();
        LOGGER.info(toString);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        this.filesCounter = filesCounter + 1;
        if(more2MBOld(attrs)){
            Files.setAttribute(file, "dos:archive", true);
            printWriter.println(file.toAbsolutePath()
                + ","
                + ( float ) file.toFile().length() / ConstantsFor.MBYTE + ""
                + ","
                + new Date(attrs.lastAccessTime().toMillis()) +
                "," +
                Files.readAttributes(file, "dos:*"));
        }
        if(commonArch(file)){
            String msgS = file.toString() + " " + attrs.lastAccessTime();
            LOGGER.warn(msgS);
        }
        if(file.toString().toLowerCase().contains("eatmeat") ||
            file.toString().contains("HOME.log")){
            String msg = file + " DELETED";
            Files.delete(file);
            LOGGER.warn(msg);
        }
        return FileVisitResult.CONTINUE;
    }

    private boolean more2MBOld(BasicFileAttributes attrs) {
        return attrs
            .lastAccessTime().toMillis() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(ConstantsFor.ONE_YEAR) &&
            attrs
                .size() > ConstantsFor.MBYTE * 2;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        this.failCounter = failCounter + 1;
        String msgStr = file.toString() + " " + failCounter;
        LOGGER.warn(msgStr);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
  
    /**
     Условие: <i>файл более 25 мб</i><b> ии </b><i>last modified: более чем</i><b> 3650 суток делить на 3</b>.

     @param file {@link Path} файла, из {@link #visitFile(Path, BasicFileAttributes)}
     @return true - если файл старый.
     */
    private boolean commonArch(Path file) {
        return file.toFile().length() > ConstantsFor.MBYTE * 25 &&
            file.toFile().lastModified() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3650 / 3);
    }

    private boolean jpegFiles(Path file, BasicFileAttributes attrs) {
        return file.getFileName().endsWith(".jpg") || file.endsWith(".nef") && attrs.isRegularFile() || attrs.isSymbolicLink();
    }

    private long parseDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        long timeInMillis = 0;
        try{
            timeInMillis = simpleDateFormat.parse(date).getTime();
        }
        catch(ParseException e){
            LOGGER.error(e.getMessage(), e);
        }
        return timeInMillis;
    }
}

