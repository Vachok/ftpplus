// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import com.pff.*;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.usermenu.MenuConsoleLocal;
import ru.vachok.ostpst.utils.FileSystemWorkerOST;
import ru.vachok.ostpst.utils.TFormsOST;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;


/**
 @since 14.05.2019 (14:16) */
class ParserPSTMessages extends ParserFolders {
    
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    private PSTFolder pstFolder;
    
    private long folderID;
    
    private String fileName;
    
    private String thing;
    
    private PSTFile pstFile;
    
    ParserPSTMessages(String fileName, String thing) throws PSTException, IOException {
        super(fileName, thing);
        this.thing = thing.toLowerCase();
        this.fileName = fileName;
    }
    
    ParserPSTMessages(String fileName, long folderID) {
        super(fileName);
        this.fileName = fileName;
        try {
            this.pstFolder = (PSTFolder) PSTObject.detectAndLoadPSTObject(new PSTFile(fileName), folderID);
        }
        catch (IOException | PSTException e) {
            e.printStackTrace();
            new MenuConsoleLocal(fileName).showMenu();
        }
    }
    
    private ParserPSTMessages(PSTFile pstFile) {
        super(pstFile);
        this.pstFile = pstFile;
    }
    
    private ParserPSTMessages(String fileName) {
        super(fileName);
    }
    
    public String searchMessage() {
        StringBuilder stringBuilder = new StringBuilder();
        if (thing == null) {
            throw new IllegalArgumentException("Sorry, parameter to search is null. (c) Vachok 22.05.2019 (13:17)");
        }
        else {
            Future<String> stringFuture = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(new SearcherEverywhere());
            try {
                String getFutureStr = stringFuture.get(600, TimeUnit.SECONDS);
                stringBuilder.append(getFutureStr);
            }
            catch (InterruptedException | ExecutionException | TimeoutException e) {
                stringBuilder.append(e.getMessage()).append("\n").append(new TFormsOST().fromArray(e));
            }
            catch (ArrayIndexOutOfBoundsException a) {
                stringBuilder.append(a.getMessage() + "\n" + new TFormsOST().fromArray(a));
            }
        }
        return stringBuilder.toString();
    }
    
    void saveMessageToDisk(Vector<PSTObject> pstObjs, String name, Path fldPath) {
        
        for (PSTObject object : pstObjs) {
            PSTMessage message = (PSTMessage) object;
            String fileOutPath = fldPath.toAbsolutePath() + ConstantsOst.SYSTEM_SEPARATOR + message.getDescriptorNodeId() + ".msg";
            
            try (OutputStream outputStream = new FileOutputStream(fileOutPath)) {
                if (object.getMessageClass().toLowerCase().contains("note")) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(message.getItemsString());
                    if (message.hasAttachments()) {
                        new ParserAttachment().saveAttachment(fileOutPath, message, stringBuilder);
                    }
                    outputStream.write(stringBuilder.toString().getBytes());
                }
            }
            catch (IOException e) {
                messageToUser.error(e.getMessage());
            }
        }
    }
    
    Map<Long, String> getMessagesSubject() throws PSTException, IOException {
        Map<Long, String> retMap = new ConcurrentHashMap<>();
        for (PSTObject folderChild : pstFolder.getChildren(pstFolder.getContentCount())) {
            if (folderChild instanceof PSTMessage) {
                PSTMessage pstMessage = (PSTMessage) folderChild;
                retMap.put(pstMessage.getDescriptorNodeId(), pstMessage.getSubject() + " (from: " + pstMessage.getSenderName() + " sent: " + pstMessage.getMessageDeliveryTime() + ")");
            }
        }
        ;
        Path mapToFile = FileSystemWorkerOST.writeMapToFile(pstFolder.getDisplayName() + ".txt", retMap);
        return retMap;
    }
    
    String searchMessage(String searchKey) throws PSTException, IOException {
        StringBuilder stringBuilder = new StringBuilder();
        int indexSrch = 0;
        try {
            Map<Long, String> subjectsMap = getMessagesSubject();
            List<String> subjectsList = new ArrayList<>(subjectsMap.values());
            Collections.sort(subjectsList);
            indexSrch = Collections.binarySearch(subjectsList, searchKey);
            String foundStr = subjectsList.get(indexSrch);
            stringBuilder.append(foundStr);
            long msgID = 0;
            for (Map.Entry<Long, String> entry : subjectsMap.entrySet()) {
                if (entry.getValue().equals(foundStr)) {
                    msgID = entry.getKey();
                }
            }
            showMessage(msgID);
        }
        catch (NullPointerException | IndexOutOfBoundsException e) {
            stringBuilder.append("Key: ").append(searchKey).append(" not found... Index =").append(indexSrch).append("\n").append(new TFormsOST().fromArray(e));
        }
        return stringBuilder.toString();
    }
    
    String searchMessage(long messageID) {
        StringBuilder stringBuilder = new StringBuilder();
        Path pathRoot = Paths.get(".").normalize().toAbsolutePath();
        String pathStr = pathRoot + ConstantsOst.SYSTEM_SEPARATOR + "attachments" + ConstantsOst.SYSTEM_SEPARATOR;
        
        stringBuilder.append("\n***");
        stringBuilder.append("Searching by message ID: ").append(messageID).append("\n");
        PSTMessage pstMessage = null;
    
        PSTObject objectLoaded = null;
        try {
            objectLoaded = PSTObject.detectAndLoadPSTObject(getPSTFile(fileName), messageID);
        }
        catch (IOException | PSTException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TFormsOST().fromArray(e));
        }
        if (objectLoaded instanceof PSTMessage) {
            pstMessage = (PSTMessage) objectLoaded;
            stringBuilder.append(pstMessage.getTransportMessageHeaders());
            stringBuilder.append(pstMessage.hasAttachments()).append(" attached files");
        }
        String nameOut = pathStr + pstMessage.getDescriptorNodeId() + ConstantsOst.SYSTEM_SEPARATOR + "message.txt";
        try (OutputStream outputStream = new FileOutputStream(nameOut)) {
            System.out.println("outputStream = " + nameOut);
            try (PrintStream printStream = new PrintStream(outputStream, true, "Windows-1251")) {
                printStream.println(pstMessage);
            }
        }
        catch (IOException e) {
            stringBuilder.append(e.getMessage()).append("\n");
            stringBuilder.append(new TFormsOST().fromArray(e));
        }
        new ParserAttachment().saveAttachment(pathStr, pstMessage, stringBuilder);
        return stringBuilder.toString();
    }
    
    private String showMessage(long msgID) throws PSTException, IOException {
        PSTMessage pstMessage = (PSTMessage) PSTObject.detectAndLoadPSTObject(new PSTFile(fileName), msgID);
        StringBuilder stringBuilder = new StringBuilder();
        System.out.println(pstMessage.getBodyPrefix());
        System.out.println(pstMessage.getTransportMessageHeaders());
        if (pstMessage.hasAttachments()) {
            List<PSTAttachment> attachmentList = new ArrayList<>();
            int attachmentsNum = pstMessage.getNumberOfAttachments();
            for (int i = 0; i < attachmentsNum; i++) {
                PSTAttachment attachment = pstMessage.getAttachment(i);
                attachmentList.add(attachment);
            }
            for (PSTAttachment x : attachmentList) {
                System.out.println(x.getSize() + " " + x.getFilename());
            }
        }
        return stringBuilder.toString();
    }
    
    private String parseFolder(PSTFolder folder, String thing) {
        StringBuilder stringBuilder = new StringBuilder();
        this.pstFolder = folder;
        try {
            Map<Long, String> messagesSubject = getMessagesSubject();
            Set<Map.Entry<Long, String>> stringEntry = messagesSubject.entrySet();
            stringEntry.stream().forEach((entry)->{
                if (entry.getValue().toLowerCase().contains(thing)) {
                    stringBuilder.append(searchMessage(entry.getKey()));
                }
            });
        }
        catch (PSTException | IOException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TFormsOST().fromArray(e));
        }
        return stringBuilder.toString();
    }
    
    class SearcherEverywhere implements Callable<String> {
        
        
        @Override public String call() {
            String searchByThing = searchByThing();
            openPath(searchByThing);
            return searchByThing;
        }
    
        private void openPath(String searchByThing) {
            Path writeStringToFile = FileSystemWorkerOST.writeStringToFile("search_" + (System.currentTimeMillis() / 1000) + ".txt", searchByThing);
            try {
                Runtime.getRuntime().exec(writeStringToFile.toString());
            }
            catch (IOException e) {
                System.err.println(e.getMessage() + "\n" + new TFormsOST().fromArray(e));
            }
        }
        
        private String searchByThing() {
            StringBuilder stringBuilder = new StringBuilder();
            ParserFolders parserFolders = new ParserFolders(fileName);
            pstFile = getPSTFile(fileName);
            try {
                Deque<String> folderNamesWithIDAndWriteToDisk = parserFolders.getDeqFolderNamesWithIDAndWriteToDisk();
                for (String folderName : folderNamesWithIDAndWriteToDisk) {
                    if (folderName.toLowerCase().contains(thing)) {
                        stringBuilder.append(folderName).append("\n");
                    }
                    folderID = Long.parseLong(folderName.split(" id ")[1]);
                    System.out.println(folderID + " = " + folderName);
                    stringBuilder.append(foldersSearch(folderName.split("\\Q (item\\E")[0], folderID));
                }
            }
            catch (IOException | PSTException | ArrayIndexOutOfBoundsException e) {
                stringBuilder.append(e.getMessage()).append("\n").append(new TFormsOST().fromArray(e));
            }
            return stringBuilder.toString();
        }
        
        private String foldersSearch(String folderName, long folderID) throws IOException, PSTException {
            StringBuilder stringBuilder = new StringBuilder();
            pstFolder = (PSTFolder) PSTObject.detectAndLoadPSTObject(pstFile, folderID);
            if (pstFolder instanceof PSTFolder) {
                stringBuilder.append(parseFolder(pstFolder, thing));
            }
            else {
                System.out.println(pstFolder.getDisplayName() + " isn't a PSTFolder");
            }
            return stringBuilder.toString();
        }
    }
    
}
