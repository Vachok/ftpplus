package ru.vachok.ostpst.fileworks;


import com.pff.*;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.usermenu.MenuItemsConsoleImpl;
import ru.vachok.ostpst.utils.FileSystemWorkerOST;
import ru.vachok.ostpst.utils.TFormsOST;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;


/**
 @since 27.05.2019 (9:10) */
class PSTMsgSearcher {
    
    
    private String srcThing;
    
    private String fileName;
    
    private long folderID;
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    public PSTMsgSearcher(String fileName, String srcThing) {
        this.srcThing = srcThing;
        this.fileName = fileName;
    }
    
    public PSTMsgSearcher(String fileName, long folderID) {
        this.fileName = fileName;
        this.folderID = folderID;
    }
    
    public String everywhereSearch() {
        StringBuilder stringBuilder = new StringBuilder();
        if (srcThing == null) {
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
                System.err.println(e.getMessage() + "\n" + new TFormsOST().fromArray(e));
                Thread.currentThread().checkAccess();
                Thread.currentThread().interrupt();
                new MenuItemsConsoleImpl(fileName).askUser();
            }
        }
        return stringBuilder.toString();
    }
    
    String searchMessage(String searchKey) throws PSTException, IOException {
        StringBuilder stringBuilder = new StringBuilder();
        int indexSrch = 0;
        try {
            Map<Long, String> subjectsMap = new ParserPSTMessages(fileName, srcThing).getMessagesSubject();
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
            new ParserPSTMessages(fileName, srcThing).showMessage(msgID);
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
    
    
    /**
     @since 26.05.2019 (8:54)
     */
    class SearcherEverywhere implements Callable<String> {
        
        
        private ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        
        private PSTFile pstFile;
        
        private ParserFolders parserFolders;
        
        @Override public String call() {
            threadMXBean.resetPeakThreadCount();
            String searchByThing = searchByThing();
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                openPath(searchByThing);
            }
            return searchByThing;
        }
        
        private void openPath(String searchByThing) {
            String fileNameSrch = ConstantsOst.FILE_PREFIX_SEARCH_ + (System.currentTimeMillis() / 1000) + ".txt";
            Path writeStringToFile = FileSystemWorkerOST.writeStringToFile(fileNameSrch, searchByThing);
            String attachFolder = writeStringToFile.getParent() + ConstantsOst.SYSTEM_SEPARATOR + ConstantsOst.STR_ATTACHMENTS;
            messageToUser = new MessageSwing();
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
                messageToUser = new MessageCons(getClass().getSimpleName());
                System.out.println(fileNameSrch);
            }
        }
        
        private String searchByThing() {
            final long start = System.currentTimeMillis();
            StringBuilder stringBuilder = new StringBuilder();
            pstFile = new PSTFileNameConverter().getPSTFile(fileName);
            try {
                this.parserFolders = new ParserFolders(fileName, srcThing);
            }
            catch (Exception e) {
                messageToUser.error(FileSystemWorkerOST.error(getClass().getSimpleName() + ".searchByThing", e));
            }
            try {
                Deque<String> folderNamesWithIDAndWriteToDisk = parserFolders.getDeqFolderNamesWithIDAndWriteToDisk();
                for (String folderName : folderNamesWithIDAndWriteToDisk) {
                    if (folderName.toLowerCase().contains(srcThing)) {
                        stringBuilder.append(folderName).append("\n");
                    }
                    folderID = Long.parseLong(folderName.split(" id ")[1]);
                    foldersSearch(folderName.split("\\Q (item\\E")[0]);
                }
            }
            catch (IOException e) {
                stringBuilder.append(e.getMessage()).append("\n").append(new TFormsOST().fromArray(e));
                Thread.currentThread().interrupt();
                new MenuItemsConsoleImpl(fileName).askUser();
            }
            catch (ArrayIndexOutOfBoundsException arr) {
                System.err.println(arr);
            }
            final long stop = System.currentTimeMillis();
            System.out.println(getClass().getSimpleName() + ".searchByThing is end work");
            System.out.println("Search complete. It taken " + TimeUnit.MILLISECONDS.toSeconds(stop - start) + " seconds of human time, and " +
                TimeUnit.NANOSECONDS.toMillis(threadMXBean.getCurrentThreadCpuTime()) + " millis of cpu time.");
            
            return stringBuilder.toString();
        }
        
        private String foldersSearch(String folderName) throws IOException {
            StringBuilder stringBuilder = new StringBuilder();
            PSTFolder pstFolder = null;
            try {
                pstFolder = (PSTFolder) PSTObject.detectAndLoadPSTObject(pstFile, folderID);
            }
            catch (PSTException e) {
                stringBuilder.append(e.getMessage()).append("\n").append(new TFormsOST().fromArray(e));
            }
            if (pstFolder instanceof PSTFolder) {
                stringBuilder.append(searchMessage(pstFolder));
            }
            return stringBuilder.toString();
        }
        
        private String searchMessage(PSTFolder pstFolder) {
            StringBuilder stringBuilder = new StringBuilder();
            
            try {
                ParserPSTMessages pstMessages = new ParserPSTMessages(fileName, srcThing);
                Map<Long, String> messagesSubject = pstMessages.getMessagesSubject();
                Set<Map.Entry<Long, String>> entrySet = messagesSubject.entrySet();
                entrySet.stream().forEach(x->{
                    if (x.getValue().toLowerCase().contains(srcThing)) {
                        try {
                            String showMessage = pstMessages.showMessage(x.getKey());
                            stringBuilder.append(showMessage);
                        }
                        catch (PSTException | IOException e) {
                            System.err.println(e.getMessage());
                        }
                    }
                });
            }
            catch (PSTException | IOException e) {
                System.err.println(e.getMessage());
            }
            
            return stringBuilder.toString();
        }
    }
    
}
