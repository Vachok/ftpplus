// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import com.pff.*;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.api.MessageToUser;
import ru.vachok.ostpst.api.MessengerOST;
import ru.vachok.ostpst.utils.CharsetEncoding;
import ru.vachok.ostpst.utils.FileSystemWorkerOST;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.Callable;


/**
 @since 06.05.2019 (12:19) */
class ParserContacts implements Callable<String> {
    
    
    public static final String METHNAME_FOLDERSREAD = ".foldersRead";
    
    private final MessageToUser messageToUser = new MessengerOST(getClass().getSimpleName());
    
    private String fileName;
    
    private PSTFile pstFile;
    
    private int strCounter;
    
    private String fileContactsName;
    
    ParserContacts(PSTFile pstFile) {
        this.pstFile = pstFile;
    }
    
    ParserContacts(String fileName) {
        this.fileName = fileName;
        this.fileContactsName = "showContacts";
    }
    
    ParserContacts(String fileName, String fileContactsName) {
        this.fileContactsName = fileContactsName;
        try {
            this.pstFile = new PSTFile(fileName);
            Files.createFile(Paths.get(fileContactsName));
        }
        catch (PSTException | IOException e) {
            messageToUser.error(e.getMessage());
    
        }
    }
    
    @Override public String call() {
        if (fileContactsName.isEmpty()) {
            this.fileContactsName = ConstantsOst.FILENAME_CONTACTSCSV;
            return getPathAndWriteHeaderToDisk();
        }
        else if (fileContactsName.equals("showContacts")) {
            return showContacts();
        }
        else {
            return getPathAndWriteHeaderToDisk();
        }
    }
    
    private String showContacts() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            String contStr = new String("онтакт".getBytes(), "UTF-8");
            this.pstFile = new PSTFile(fileName);
            ParserFolders rootFolder = new ParserFolders(pstFile);
            Deque<String> folderNamesAndWriteToDisk = rootFolder.getDeqFolderNamesWithIDAndWriteToDisk();
            for (String s : folderNamesAndWriteToDisk) {
                String folderNameFromDeq = s.toLowerCase();
                folderNameFromDeq = new CharsetEncoding(ConstantsOst.CP_WINDOWS_1251).getStrInAnotherCharset(folderNameFromDeq);
    
                if (folderNameFromDeq.contains(contStr)) {
                    s = s.split("id ")[1];
                    long parseLong = Long.parseLong(s);
                    PSTFolder pstFolder = (PSTFolder) PSTObject.detectAndLoadPSTObject(pstFile, parseLong);
                    stringBuilder.append(folderRead(pstFolder));
                }
            }
        }
        catch (PSTException | IOException e) {
            messageToUser.error(FileSystemWorkerOST.error(getClass().getSimpleName() + ".showContacts", e));
            stringBuilder.append(e.getMessage());
        }
        return stringBuilder.toString();
    }
    
    private String folderRead(PSTFolder folder) throws PSTException, IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for (PSTObject folderChild : folder.getChildren(folder.getContentCount())) {
            PSTContact pstContact = (PSTContact) folderChild;
            String emailAddress = pstContact.getEmail1EmailAddress();
            String printStr = emailAddress.replace("'", "") + " (" + pstContact.getDisplayName() + ") id " + pstContact.getDescriptorNodeId();
            stringBuilder.append(printStr).append("\n");
        }
        
        return stringBuilder.toString();
    }
    
    private void foldersRead(PSTFolder pstFolder, PrintStream printStream) throws IOException {
        Vector<PSTFolder> folders = null;
        try {
            folders = pstFolder.getSubFolders();
        }
        catch (PSTException e) {
            messageToUser.error(e.getMessage());
        }
    
        Iterator<PSTFolder> pstFolderIterator = Objects.requireNonNull(folders, "No FOLDERS " + getClass().getSimpleName() + METHNAME_FOLDERSREAD).iterator();
        
        while (pstFolderIterator.hasNext()) {
            PSTFolder folder = pstFolderIterator.next();
            String folderDisplayName = new String(folder.getDisplayName().getBytes(), Charset.forName(ConstantsOst.CP_WINDOWS_1251));
            String strCont = new String("онтакт".getBytes(), "UTF-8");
            boolean nameContacts = folderDisplayName.contains(strCont);
            boolean hasSubs = folder.hasSubfolders();
    
            if (hasSubs && !nameContacts) {
                foldersRead(folder, printStream);
            }
            if (nameContacts) {
                try {
                    writeContactsToFile(folder, printStream);
                }
                catch (PSTException e) {
                    messageToUser.error(FileSystemWorkerOST.error(getClass().getSimpleName() + METHNAME_FOLDERSREAD, e));
                }
            }
        }
    }
    
    private void writeContactsToFile(PSTFolder folder, PrintStream printStream) throws IOException, PSTException {
        Vector<PSTObject> folderChildren = new Vector<>();
        long objID = 0;
        try {
            int folderContentCount = folder.getContentCount();
            folderChildren = folder.getChildren(folderContentCount);
        }
        catch (PSTException e) {
            messageToUser.error(e.getMessage());
            objID = folder.getDescriptorNodeId();
        }
        
        Iterator<PSTObject> pstObjectIterator = folderChildren.iterator();
        
        if (objID != 0) {
            PSTObject object = PSTObject.detectAndLoadPSTObject(pstFile, objID);
            ParserObjects parserObjects = new ParserObjects(object);
        }
        else {
            while (pstObjectIterator.hasNext()) {
                writeContact(pstObjectIterator, printStream);
            }
        }
    }
    
    private void writeContact(Iterator<PSTObject> pstObjectIterator, PrintStream printStream) {
        PSTContact pstContact = (PSTContact) pstObjectIterator.next();
        printStream.print("\"\",\"");
        printStream.print(pstContact
            .getDisplayName() + "\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",,,\"\",\"\",\"\",\"\",\"\",,,\"\",\"\",\"\",\"\",\"\",,,\"\",\"\",\"\",\"\",\"\",,\"\",\"\",\"\",\"\",\"\",,\"\",\"\",\"\",\"\",,\"\",\"\",\"\",\"\",\"\",\"\",\"Обычная\",\"\",\"0.0.00\",\"0.0.00\",,,\"\",\"\",,,,,,\"Не определен\",,,,,\"Обычная\",,,,,,\"\",,\"\",,,,,,,\"Ложь\",\"" + pstContact
            .getEmail1EmailAddress().replace("'", "") + "\",");
        printStream.print("\"SMTP\",");
        printStream.print("\"" + pstContact.getEmail1EmailAddress().replace("'", "") + "\"");
        printStream.println();
    }
    
    private void showContact(PSTContact pstContact) {
        strCounter++;
        System.out.println(strCounter + ") " + pstContact.getDisplayName() + " is " + pstContact.getEmail1EmailAddress());
    }
    
    private String getPathAndWriteHeaderToDisk() {
        String csvHeader = FileSystemWorkerOST.readFileToString(getClass().getResource("csvheader.txt").getFile());
        PSTFolder rootFolder = null;
        try {
            rootFolder = pstFile.getRootFolder();
        }
        catch (PSTException | IOException e) {
            messageToUser.error(e.getMessage());
        }
        try (OutputStream outputStream = new FileOutputStream(fileContactsName);
             PrintStream printStream = new PrintStream(outputStream, true, ConstantsOst.CP_WINDOWS_1251)
        ) {
            printStream.println(new CharsetEncoding("UTF-8", ConstantsOst.CP_WINDOWS_1251).getStrInAnotherCharset(csvHeader));
            foldersRead(Objects.requireNonNull(rootFolder), printStream);
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        
        return Paths.get(fileContactsName).toAbsolutePath().toString();
    }
}
