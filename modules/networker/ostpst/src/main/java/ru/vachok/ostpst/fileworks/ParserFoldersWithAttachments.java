// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import com.pff.PSTException;
import com.pff.PSTFile;
import com.pff.PSTFolder;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.utils.FileSystemWorker;

import java.io.IOException;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 @since 30.04.2019 (15:04) */
class ParserFoldersWithAttachments {
    
    
    private static final String CONTENT = ".inboxFolder";
    
    private PSTFile pstFile;
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    private String fileName;
    
    private int levelCounter;
    
    private int totalCounter;
    
    ParserFoldersWithAttachments(PSTFile pstFile) {
        this.pstFile = pstFile;
    }
    
    ParserFoldersWithAttachments(String fileName) {
        this.fileName = fileName;
        try {
            this.pstFile = new PSTFile(fileName);
        }
        catch (PSTException | IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".PSTContentToFoldersWithAttachments", e));
        }
    }
    
    String showFoldersIerarchy() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            PSTFolder rootFolder = pstFile.getRootFolder();
            stringBuilder.append(parseFolder(rootFolder, stringBuilder));
        }
        catch (PSTException | IOException e) {
            stringBuilder.append(e.getMessage());
        }
        System.out.println("totalCounter = " + totalCounter);
        return stringBuilder.toString();
    }
    
    Deque<String> getDeqFolderNamesAndWriteToDisk() {
        Deque<String> retDeq = new ConcurrentLinkedDeque<>();
        String showFoldersIerarchy = showFoldersIerarchy();
        String[] split = showFoldersIerarchy.split(": ");
        
        for (String s : split) {
            retDeq.add(s.replaceAll("(\\Q|\\E)*(\\d)", "").split("\\Q (\\E")[0].replace("/)", ""));
        }
        messageToUser.info(FileSystemWorker.writeStringToFile("folders.txt", showFoldersIerarchy));
        return retDeq;
    }
    
    private String getLevelCounterStr(int counter) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < counter; i++) {
            stringBuilder.append("|");
        }
        return stringBuilder.toString();
    }
    
    private String parseFolder(PSTFolder rootFolder, final StringBuilder stringBuilder) throws PSTException, IOException {
        Vector<PSTFolder> rootSubFolders = rootFolder.getSubFolders();
        Iterator<PSTFolder> iteratorFolder = rootSubFolders.iterator();
        
        while (iteratorFolder.hasNext()) {
            PSTFolder nextFold = iteratorFolder.next();
            levelCounter++;
            totalCounter++;
            String nextFoldDisplayName = nextFold.getDisplayName();
            String levelCounterStr = getLevelCounterStr(levelCounter);
            stringBuilder.append(levelCounterStr).append(levelCounter).append(": ").append(nextFoldDisplayName);
            stringBuilder.append(" (items: ").append(nextFold.getUnreadCount()).append("/").append(nextFold.getContentCount()).append(")\n");
            System.out.println(levelCounterStr + " :" + levelCounter + ": " + nextFoldDisplayName);
            if (nextFold.hasSubfolders()) {
                parseFolder(Objects.requireNonNull(nextFold, "No folder"), stringBuilder);
            }
            else {
                levelCounter--;
            }
        }
        levelCounter--;
        return stringBuilder.toString();
    }
}
