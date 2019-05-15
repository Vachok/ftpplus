// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import com.pff.PSTException;
import com.pff.PSTFile;
import com.pff.PSTFolder;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.utils.FileSystemWorker;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import java.util.Vector;


/**
 @since 30.04.2019 (15:04) */
class ParserFoldersWithAttachments {
    
    
    private static final String CONTENT = ".inboxFolder";
    
    private PSTFile pstFile;
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    private String fileName;
    
    private int levelCounter;
    
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
    
    String getListFolders() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            PSTFolder rootFolder = pstFile.getRootFolder();
            stringBuilder.append(parseFolder(rootFolder, stringBuilder));
        }
        catch (PSTException | IOException e) {
            stringBuilder.append(e.getMessage());
        }
        return stringBuilder.toString();
    }
    
    private String parseFolder(PSTFolder rootFolder, final StringBuilder stringBuilder) throws PSTException, IOException {
        Vector<PSTFolder> rootSubFolders = rootFolder.getSubFolders();
        Iterator<PSTFolder> iteratorFolder = rootSubFolders.iterator();
        
        while (iteratorFolder.hasNext()) {
            PSTFolder nextFold = iteratorFolder.next();
            levelCounter++;
            stringBuilder.append(getLevelCounterStr(levelCounter)).append(levelCounter).append(": ").append(nextFold.getDisplayName());
            stringBuilder.append(" (items: ").append(nextFold.getUnreadCount()).append("/").append(nextFold.getContentCount()).append(")\n");
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
    
    private String getLevelCounterStr(int counter) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < counter; i++) {
            stringBuilder.append("|");
        }
        return stringBuilder.toString();
    }
}
