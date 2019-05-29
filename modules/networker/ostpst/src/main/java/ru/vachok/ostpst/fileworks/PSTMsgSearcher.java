// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import com.pff.*;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.utils.FileSystemWorkerOST;
import ru.vachok.ostpst.utils.TFormsOST;

import java.awt.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
//            ForkJoinTask<String> stringForkJoinTask = ForkJoinTask.adapt(new SearcherEverywhere());
//            ForkJoinTask<String> fork = stringForkJoinTask.fork();
            String getFutureStr = null;
            try {
                getFutureStr = callForSearch.get();
                if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    openPath(getFutureStr);
                }
    
            }
            catch (InterruptedException | ExecutionException e) {
                stringBuilder.append(e.getMessage()).append("\n").append(new TFormsOST().fromArray(e));
            }
            stringBuilder.append(getFutureStr);
        }
        return stringBuilder.toString();
    }
    
    public String byEmail() {
        PSTMsgSearcher.SearcherByEmail searcherByEmail = new PSTMsgSearcher.SearcherByEmail(srcThing);
        StringBuilder stringBuilder = new StringBuilder();
        Future<String> stringFuture = Executors.newSingleThreadExecutor().submit(searcherByEmail);
        try {
            stringBuilder.append(stringFuture.get());
        }
        catch (InterruptedException | ExecutionException | ArrayIndexOutOfBoundsException e) {
            stringBuilder.append(e.getMessage());
        }
        return stringBuilder.toString();
    }
    
    String searchMessage(String searchKey) throws PSTException, IOException {
        StringBuilder stringBuilder = new StringBuilder();
        int indexSch = 0;
        try {
            Map<Long, String> subjectsMap = new ParserPSTMessages(fileName, srcThing).getMessagesSubject();
            List<String> subjectsList = new ArrayList<>(subjectsMap.values());
            Collections.sort(subjectsList);
            indexSch = Collections.binarySearch(subjectsList, searchKey);
            String foundStr = subjectsList.get(indexSch);
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
            stringBuilder.append("Key: ").append(searchKey).append(" not found... Index =").append(indexSch).append("\n").append(new TFormsOST().fromArray(e));
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
    
    
    /**
     @since 26.05.2019 (8:54)
     */
    class SearcherEverywhere implements Callable<String> {
        
        
        private ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        
        private PSTFile pstFile;
        
        private ParserFolders parserFolders;
        
        @Override public String call() {
            threadMXBean.resetPeakThreadCount();
            return searchByThing();
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
                }
                while (!folderNamesWithIDAndWriteToDisk.isEmpty()) {
                    ForkJoinTask<String> forkJoinTask = ForkJoinTask.adapt(()->foldersSearch(folderNamesWithIDAndWriteToDisk.poll()));
                    forkJoinTask = forkJoinTask.fork();
                    stringBuilder.append(forkJoinTask.get());
                }
            }
            catch (IOException | InterruptedException | ExecutionException e) {
                stringBuilder.append(e.getMessage()).append("\n").append(new TFormsOST().fromArray(e));
                Thread.currentThread().interrupt();
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
            folderID = Long.parseLong(folderName.split(" id ")[1]);
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
    
        private String searchMessage(PSTFolder pstFolder) throws IOException {
            StringBuilder stringBuilder = new StringBuilder();
            ParserPSTMessages pstMessages = new ParserPSTMessages(fileName, folderID);
            Map<Long, String> messagesSubject = null;
            try {
                messagesSubject = pstMessages.getMessagesSubject();
            }
            catch (PSTException e) {
                stringBuilder.append(e.getMessage()).append("\n").append(new TFormsOST().fromArray(e));
            }
            Set<Map.Entry<Long, String>> entrySet = Objects.requireNonNull(messagesSubject).entrySet();
            entrySet.forEach(x->{
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
            return stringBuilder.toString();
        }
    }
    
    
    /**
     @since 29.05.2019 (13:16)
     */
    class SearcherByEmail implements Callable<String> {
        
        
        private ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        
        private String userEmail;
        
        public SearcherByEmail(String userEmail) {
            this.userEmail = userEmail;
        }
        
        @Override public String call() {
            threadMXBean.resetPeakThreadCount();
            String searchEmail = null;
            try {
                searchEmail = searchEmail();
            }
            catch (PSTException | IOException e) {
                return new TFormsOST().fromArray(e);
            }
            return searchEmail;
        }
        
        private String searchEmail() throws PSTException, IOException {
            throw new IllegalComponentStateException("29.05.2019 (15:40)");
        }
    }
    
}
