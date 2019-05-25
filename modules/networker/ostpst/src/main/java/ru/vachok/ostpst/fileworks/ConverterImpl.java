// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import com.pff.PSTException;
import com.pff.PSTFile;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.MakeConvertOrCopy;
import ru.vachok.ostpst.utils.CharsetEncoding;
import ru.vachok.ostpst.utils.FileSystemWorkerOST;
import ru.vachok.ostpst.utils.TFormsOST;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.concurrent.Callable;


/**
 @since 14.05.2019 (12:14) */
public class ConverterImpl implements MakeConvertOrCopy {
    
    
    private String fileName;
    
    public ConverterImpl(String fileName) {
        this.fileName = new CharsetEncoding(ConstantsOst.CP_WINDOWS_1251).getStrInAnotherCharset(fileName);
    }
    
    @Override public String convertToPST() {
        throw new IllegalComponentStateException("15.05.2019 (9:27)");
    }
    
    @Override public String saveFolders() throws IOException {
        ParserFolders parserFolders = new ParserFolders(fileName);
        Deque<String> deqFolderNamesWithIDAndWriteToDisk = parserFolders.getDeqFolderNamesWithIDAndWriteToDisk();
        File file = new File(ConstantsOst.FILENAME_FOLDERSTXT);
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        else {
            return "No file found";
        }
    }
    
    @Override public String saveContacts(String csvFileName) {
        StringBuilder stringBuilder = new StringBuilder();
        Path root = Paths.get(fileName).toAbsolutePath().getParent();
    
        if (csvFileName == null || csvFileName.isEmpty()) {
            csvFileName = root + ConstantsOst.SYSTEM_SEPARATOR + ConstantsOst.FILENAME_CONTACTSCSV;
        }
        File csvFile = createCSV(csvFileName);
    
        String csvFileAbsolutePath = csvFile.getAbsolutePath();
        Callable<String> contacts = new ParserContacts(fileName, Objects.requireNonNull(csvFileAbsolutePath, "No CSV fileName given!"));
        try {
            File file = new File(contacts.call());
            if (file.length() > 4096) {
                stringBuilder.append(file.toPath());
            }
            else {
                stringBuilder.append(csvFileAbsolutePath).append(" is ").append(csvFile.length());
            }
        }
        catch (Exception e) {
            stringBuilder.append(e.getMessage()).append("\n");
            stringBuilder.append(new TFormsOST().fromArray(e));
        }
    
        return stringBuilder.toString();
    }
    
    @Override public String cleanPreviousCopy() {
        RNDFileCopy rndFileCopy = new RNDFileCopy(fileName);
        return rndFileCopy.clearPositions();
    }
    
    @Override public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    @Override public String copyierWithSave(String newCP) {
        RNDFileCopy rndFileCopy = new RNDFileCopy(fileName);
        return rndFileCopy.copyFile(newCP);
    }
    
    @Override public String showListFolders() {
        ParserFolders parserFolders = new ParserFolders(fileName);
        try {
            return parserFolders.showFoldersIerarchy();
        }
        catch (IOException e) {
            return e.getMessage();
        }
    }
    
    @Override public Deque<String> getDequeFolderNamesAndWriteToDisk() throws IOException {
        ParserFolders parserFolders = new ParserFolders(fileName);
        return parserFolders.getDeqFolderNamesWithIDAndWriteToDisk();
    }
    
    @Override public String showContacts() {
        ParserContacts parserContacts = new ParserContacts(fileName);
        String callContacts = parserContacts.call();
        System.out.println(callContacts);
        FileSystemWorkerOST.writeStringToFile("contacts.txt", callContacts);
        return callContacts;
    }
    
    @Override public String getObjectItemsByID(long id) {
        try {
            ParserObjects parserObjects = new ParserObjects(new PSTFile(fileName), id);
            return parserObjects.getObjectDescriptorID();
        }
        catch (PSTException | IOException | NullPointerException e) {
            return e.getMessage() + "\n" + new TFormsOST().fromArray(e);
        }
        
    }
    
    @Override public List<String> getListMessagesSubjectWithID(long folderID) {
        List<String> retList = new ArrayList<>();
        ParserPSTMessages pstMessages = null;
        try {
            pstMessages = new ParserPSTMessages(fileName, folderID);
        }
        catch (Exception e) {
            this.fileName = new CharsetEncoding(ConstantsOst.CP_WINDOWS_1251).getStrInAnotherCharset(fileName);
        }
        try {
            Map<Long, String> messagesSubject = pstMessages.getMessagesSubject();
            messagesSubject.forEach((x, y)->retList.add(x + " : " + y));
        }
        catch (PSTException | IOException | NullPointerException e) {
            retList.add(e.getMessage());
        }
        Collections.sort(retList);
        return retList;
    }
    
    @Override public String searchMessages(long folderID, long msgID) {
        ParserPSTMessages pstMessages = new ParserPSTMessages(fileName, folderID);
        return pstMessages.searchMessage(msgID);
    }
    
    @Override public String searchMessages(long folderID, String msgSubject) {
        ParserPSTMessages pstMessages = new ParserPSTMessages(fileName, folderID);
        try {
            return pstMessages.searchMessage(msgSubject);
        }
        catch (PSTException | IOException e) {
            return e.getMessage() + " \n" + new TFormsOST().fromArray(e);
        }
    }
    
    @Override public String searchMessages(String someThing) throws PSTException, IOException {
        ParserPSTMessages pstMessages = new ParserPSTMessages(fileName, someThing);
        return pstMessages.searchMessage();
    }
    
    private File createCSV(String csvFileName) {
        try {
            Path path = Files.createFile(Paths.get(csvFileName));
            return new File(path.toAbsolutePath().toString());
        }
        catch (IOException e) {
            csvFileName = new CharsetEncoding(ConstantsOst.CP_WINDOWS_1251).getStrInAnotherCharset(csvFileName);
            return new File(csvFileName);
        }
    }
}
