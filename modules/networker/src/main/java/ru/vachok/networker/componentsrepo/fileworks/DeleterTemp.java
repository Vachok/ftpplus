// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.fileworks;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.*;


/**
 @see ru.vachok.networker.componentsrepo.fileworks.DeleterTempTest
 @since 19.12.2018 (11:05) */
@SuppressWarnings("ClassWithoutmessageToUser")
public class DeleterTemp extends SimpleFileVisitor<Path> implements Runnable {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, DeleterTemp.class.getSimpleName());
    
    /**
     Счётчик файлов
     */
    private int filesCounter;
    
    private Path pathToDel;
    
    private List<String> patternsToDelFromFile = new ArrayList<>();
    
    public DeleterTemp(Path patToDel) {
        this.pathToDel = patToDel;
    }
    
    public DeleterTemp(List<String> delPatterns) {
        this.patternsToDelFromFile.addAll(delPatterns);
        this.pathToDel = Paths.get(".");
    }
    
    DeleterTemp() {
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (dir.toFile().getName().contains(ConstantsFor.GRADLE)) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        if (dir.toFile().getName().contains("idea")) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public void run() {
        getList();
        deleteFiles();
    }
    
    private void getList() {
        try (InputStream inputStream = new FileInputStream(new File(DeleterTemp.class.getResource("/static/config/temp_pat.cfg").getFile()));
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            bufferedReader.lines().forEach(line->patternsToDelFromFile.add(line));
        }
        catch (IOException e) {
            messageToUser.warn(new TForms().fromArray(e, false));
        }
    }
    
    private void deleteFiles() {
        try {
            Files.walkFileTree(pathToDel, Collections.singleton(FileVisitOption.FOLLOW_LINKS), 2, this);
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(this.getClass().getSimpleName() + ".txt", true);
             PrintWriter printWriter = new PrintWriter(outputStream, true)) {
            if (checkZeroSize(attrs)) {
                printWriter.println(new StringBuilder()
                    .append(file.toAbsolutePath().normalize())
                    .append(",")
                    .append(new Date(attrs.lastAccessTime().toMillis())));
            }
            if (tempFile(file.toAbsolutePath())) {
                String fileAbs;
                try {
                    boolean isDel = Files.deleteIfExists(file.toAbsolutePath().normalize());
                    fileAbs = MessageFormat.format(" File: {1} is deleted: {2} ({0} total)", filesCounter, file, isDel);
                    printWriter.println(fileAbs);
                    filesCounter += 1;
                }
                catch (FileSystemException e) {
                    file.toFile().deleteOnExit();
                    fileAbs = MessageFormat.format("{0}) {1} must be deleted on exit\n{2}", filesCounter, file.toFile().getName(), e.getMessage());
                    printWriter.println(fileAbs);
                    return FileVisitResult.CONTINUE;
                }
                catch (IOException e) {
                    messageToUser.error(MessageFormat
                        .format("DeleterTemp.visitFile {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
                }
            }
        }
        return FileVisitResult.CONTINUE;
    }
    
    /**
     Usages: {@link #visitFile(Path, BasicFileAttributes)} <br> Uses: - <br>
     
     @param attrs {@link BasicFileAttributes}
     @return <b>true</b> = lastAccessTime - ONE_YEAR and size bigger MBYTE*2
     */
    private boolean checkZeroSize(@NotNull BasicFileAttributes attrs) {
        boolean retBool = false;
        if (attrs.isRegularFile() && attrs.size() <= 0) {
            retBool = true;
        }
        else if (attrs.isDirectory()) {
            retBool = false;
        }
        return retBool;
    }
    
    /**
     Проверка файлика на "временность".
     <p>
     ClassPath - /BOOT-INF/classes/static/config/temp_pat.cfg <br> .\resources\static\config\temp_pat.cfg
     
     @param filePath {@link Path} до файла
     @return удалять / не удалять
     */
    private boolean tempFile(Path filePath) {
        return patternsToDelFromFile.stream().anyMatch(stringPath->filePath.toString().toLowerCase().contains(stringPath));
    }
    
    @Override
    public String toString() {
        StringJoiner stringJoiner = new StringJoiner(",\n", DeleterTemp.class.getSimpleName() + "[\n", "\n]").add("filesCounter = " + filesCounter);
        try {
            stringJoiner.add("pathToDel = " + pathToDel.toAbsolutePath().normalize().toString())
                .add("patternsToDelFromFile = " + new TForms().fromArray(patternsToDelFromFile));
        }
        catch (RuntimeException e) {
            stringJoiner.add(e.getMessage());
        }
        return stringJoiner.toString();
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        ((MessageLocal) messageToUser).loggerFine(filesCounter + " files deleted.");
        return FileVisitResult.CONTINUE;
    }
}
