package ru.vachok.ostpst.fileworks;


import ru.vachok.ostpst.MakeConvert;
import ru.vachok.ostpst.utils.CharsetEncoding;
import ru.vachok.ostpst.utils.FileSystemWorker;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 @since 14.05.2019 (12:14) */
public class ConverterImpl implements MakeConvert {
    
    
    private String fileName;
    
    public ConverterImpl(String fileName) {
        this.fileName = new CharsetEncoding("windows-1251").getStrInAnotherCharset(fileName);
    }
    
    @Override public String convertToPST() {
        throw new IllegalComponentStateException("15.05.2019 (9:27)");
    }
    
    @Override public void saveFolders() {
        ParserFoldersWithAttachments parserFoldersWithAttachments = new ParserFoldersWithAttachments(fileName);
        System.out.println("pstContentToFoldersWithAttachments = " + parserFoldersWithAttachments.getListFolders());
    }
    
    @Override public String saveContacts(String csvFileName) {
        if (csvFileName == null) {
            Path root = Paths.get(fileName).toAbsolutePath().getParent();
            csvFileName = root + FileSystemWorker.SYSTEM_DELIMITER + "contacts.csv";
        }
        File csvFile = new File(csvFileName);
        Runnable contacts = new ParserContacts(fileName, Objects.requireNonNull(csvFileName, "No CSV fileName given!"));
        contacts.run();
    
        if (csvFile.length() > 4096) {
            return csvFile.getAbsolutePath();
        }
        else {
            return csvFileName + " is " + csvFile.length();
        }
    }
    
    @Override public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    @Override public String copyierWithSave() {
        RNDFileCopy rndFileCopy = new RNDFileCopy(fileName);
        return rndFileCopy.copyFile();
    }
    
    @Override public String showListFolders() {
        ParserFoldersWithAttachments parserFoldersWithAttachments = new ParserFoldersWithAttachments(fileName);
        return parserFoldersWithAttachments.getListFolders();
    }
    
    @Override public Deque<String> getDequeFolderNames() {
        Deque<String> retDeq = new ConcurrentLinkedDeque<>();
        String[] split = showListFolders().split(": ");
        for (String s : split) {
            retDeq.add(s.replaceAll("(\\Q|\\E)*(\\d)", "").split("\\Q (\\E")[0].replace("/)", ""));
        }
        return retDeq;
    }
    
    @Override public String clearCopy() {
        String clearPositions = new RNDFileCopy(fileName).clearPositions();
        return clearPositions;
    }
}
