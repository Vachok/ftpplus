// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import com.pff.*;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.usermenu.MenuConsoleLocal;
import ru.vachok.ostpst.usermenu.MenuItemsConsoleImpl;
import ru.vachok.ostpst.utils.FileSystemWorkerOST;
import ru.vachok.ostpst.utils.TFormsOST;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.*;


/**
 @since 14.05.2019 (14:16) */
class ParserPSTMessages extends ParserFolders {
    
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    private MemoryMXBean mxBean = ManagementFactory.getMemoryMXBean();
    
    private PSTFolder pstFolder;
    
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
            this.pstFolder = (PSTFolder) PSTObject.detectAndLoadPSTObject(new PSTFile(this.fileName), folderID);
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
            Future<String> callForSearch = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(new SearcherEverywhere());
            try {
                String getFutureStr = callForSearch.get();
                stringBuilder.append(getFutureStr);
            }
            catch (ArrayIndexOutOfBoundsException arr) {
                stringBuilder.append(arr.getMessage()).append("\n").append(new TFormsOST().fromArray(arr));
            }
            catch (OutOfMemoryError o) {
                stringBuilder.append(o.getMessage()).append("\n").append(new TFormsOST().fromArray(Collections.singleton(o)));
            }
            catch (InterruptedException | ExecutionException e) {
                messageToUser.error(e.getMessage());
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
                        new WriterMessageAndAttachments().saveAttachment(fileOutPath, message, stringBuilder);
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
                Thread.currentThread().setName(String.valueOf(mxBean.getHeapMemoryUsage()));
            }
        }
        ;
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
        Path pathRoot = Paths.get(".").normalize().toAbsolutePath();
        StringBuilder stringBuilder = new StringBuilder();
        String pathStr = pathRoot + ConstantsOst.SYSTEM_SEPARATOR + ConstantsOst.STR_ATTACHMENTS + ConstantsOst.SYSTEM_SEPARATOR;
        PSTMessage pstMessage = null;
        PSTObject objectLoaded = null;
    
        stringBuilder.append("\n***");
        stringBuilder.append("Searching by message ID: ").append(messageID).append("\n");
        try {
            objectLoaded = PSTObject.detectAndLoadPSTObject(new PSTFileNameConverter().getPSTFile(fileName), messageID);
        }
        catch (IOException | PSTException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TFormsOST().fromArray(e));
        }
        if (objectLoaded instanceof PSTMessage) {
            pstMessage = (PSTMessage) objectLoaded;
            stringBuilder.append(pstMessage.getTransportMessageHeaders());
            stringBuilder.append(pstMessage.hasAttachments()).append(" attached files");
            new WriterMessageAndAttachments().saveAttachment(pathStr, pstMessage, stringBuilder);
        }

        else {
            System.err.println(objectLoaded + " is not a PSTMessage!");
        }
    
        return stringBuilder.toString();
    }
    
    private String showMessage(long msgID) throws PSTException, IOException {
        PSTObject pstObject = PSTObject.detectAndLoadPSTObject(new PSTFileNameConverter().getPSTFile(fileName), msgID);
        StringBuilder stringBuilder = new StringBuilder();
        if (pstObject instanceof PSTMessage) {
            PSTMessage pstMessage = (PSTMessage) pstObject;
            System.out.println(pstMessage.getBodyPrefix());
            System.out.println(pstMessage.getTransportMessageHeaders());
            new WriterMessageAndAttachments()
                .saveAttachment(Paths.get(".").toAbsolutePath().normalize() + ConstantsOst.SYSTEM_SEPARATOR + ConstantsOst.STR_ATTACHMENTS, pstMessage, stringBuilder);
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
    
    
    /**
     @since 26.05.2019 (8:54)
     */
    class SearcherEverywhere implements Callable<String> {
        
        
        private ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        
        private String attachFolder = Paths.get(".").toAbsolutePath().normalize() + ConstantsOst.SYSTEM_SEPARATOR + ConstantsOst.STR_ATTACHMENTS;
        
        @Override public String call() {
            threadMXBean.resetPeakThreadCount();
            String searchByThing = searchByThing();
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                openPath(searchByThing);
            }
            return searchByThing;
        }
        
        private void delAttachments() {
            try {
                Files.walkFileTree(new File(attachFolder).toPath(), new DeleterAttachments());
            }
            catch (IOException e) {
                messageToUser.error(e.getMessage());
            }
        }
        
        private void openPath(String searchByThing) {
            String fileNameSrch = ConstantsOst.FILE_PREFIX_SEARCH_ + (System.currentTimeMillis() / 1000) + ".txt";
            Path writeStringToFile = FileSystemWorkerOST.writeStringToFile(fileNameSrch, searchByThing);
            this.attachFolder = writeStringToFile.getParent() + ConstantsOst.SYSTEM_SEPARATOR + ConstantsOst.STR_ATTACHMENTS;
            MessageToUser messageToUser = new MessageSwing();
            String confirm = messageToUser.confirm(getClass().getSimpleName(), "Search complete!", "Open folders?");
            if (confirm.equals("ok")) {
                try {
                    Runtime.getRuntime().exec("notepad \"" + fileNameSrch + "\"");
                    Runtime.getRuntime().exec("explorer \"" + attachFolder);
                }
                catch (IOException e) {
                    System.err.println(e.getMessage() + "\n" + new TFormsOST().fromArray(e));
                }
            }
            else {
                System.out.println(fileNameSrch);
            }
        }
        
        private String searchByThing() {
            final long start = System.currentTimeMillis();
            StringBuilder stringBuilder = new StringBuilder();
            ParserFolders parserFolders = new ParserFolders(fileName);
            try {
                pstFile = new PSTFileNameConverter().getPSTFile(fileName);
            }
            catch (Exception e) {
                messageToUser.error(FileSystemWorkerOST.error(getClass().getSimpleName() + ".searchByThing", e));
            }
            try {
                Deque<String> folderNamesWithIDAndWriteToDisk = parserFolders.getDeqFolderNamesWithIDAndWriteToDisk();
                for (String folderName : folderNamesWithIDAndWriteToDisk) {
                    if (folderName.toLowerCase().contains(thing)) {
                        stringBuilder.append(folderName).append("\n");
                    }
                    long folderID = Long.parseLong(folderName.split(" id ")[1]);
                    System.out.println(folderID + " = " + folderName);
                    stringBuilder.append(foldersSearch(folderName.split("\\Q (item\\E")[0], folderID));
                }
            }
            catch (IOException | PSTException e) {
                stringBuilder.append(e.getMessage()).append("\n").append(new TFormsOST().fromArray(e));
                Thread.currentThread().interrupt();
                new MenuItemsConsoleImpl(fileName).askUser();
            }
            catch (ArrayIndexOutOfBoundsException arr) {
                System.err.println(arr.toString());
            }
            final long stop = System.currentTimeMillis();
            
            System.out.println(getClass().getSimpleName() + ".searchByThing is end work");
            System.out.println("Search complete. It taken " + TimeUnit.MILLISECONDS.toSeconds(stop - start) + " seconds of human time, and " +
                TimeUnit.NANOSECONDS.toMillis(threadMXBean.getCurrentThreadCpuTime()) + " millis of cpu time.");
            
            return stringBuilder.toString();
        }
        
        private String foldersSearch(String folderName, long folderID) throws IOException, PSTException {
            StringBuilder stringBuilder = new StringBuilder();
            pstFolder = (PSTFolder) PSTObject.detectAndLoadPSTObject(pstFile, folderID);
            if (pstFolder instanceof PSTFolder) {
                stringBuilder.append(parseFolder(pstFolder, thing));
            }
            return stringBuilder.toString();
        }
    }
    
    
    
    class DeleterAttachments extends SimpleFileVisitor<Path> {
        
        
        @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            file.toFile().delete();
            return FileVisitResult.CONTINUE;
        }
        
        @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            dir.toFile().delete();
            return FileVisitResult.CONTINUE;
        }
    }
}
