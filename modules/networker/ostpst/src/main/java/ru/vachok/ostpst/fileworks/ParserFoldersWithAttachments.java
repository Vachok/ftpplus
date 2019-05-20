// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import com.pff.PSTException;
import com.pff.PSTFile;
import com.pff.PSTFolder;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.usermenu.MenuConsoleLocal;
import ru.vachok.ostpst.utils.FileSystemWorkerOST;

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
    
    private PSTFolder rootFolder;
    
    private int levelCounter;
    
    private int totalCounter;
    
    ParserFoldersWithAttachments(PSTFolder folder) {
        this.rootFolder = folder;
    }
    
    ParserFoldersWithAttachments(PSTFile pstFile) {
        this.pstFile = pstFile;
    }
    
    ParserFoldersWithAttachments(String fileName) {
        try {
            this.pstFile = new PSTFile(fileName);
        }
        catch (PSTException | IOException e) {
            messageToUser.error(FileSystemWorkerOST.error(getClass().getSimpleName() + ".PSTContentToFoldersWithAttachments", e));
            new MenuConsoleLocal(fileName).showMenu();
        }
    }
    
    String showFoldersIerarchy() throws IOException, NullPointerException {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            if (pstFile != null) {
                this.rootFolder = pstFile.getRootFolder();
            }
            stringBuilder.append(parseFolder(rootFolder, stringBuilder));
        }
        catch (PSTException | IOException | NullPointerException e) {
            stringBuilder.append(e.getMessage());
        }
        stringBuilder.append("totalCounter = " + totalCounter);
        return stringBuilder.toString();
    }
    
    Deque<String> getDeqFolderNamesWithIDAndWriteToDisk() throws IOException {
        Deque<String> retDeq = new ConcurrentLinkedDeque<>();
        String showFoldersIerarchy = showFoldersIerarchy();
        String[] split = showFoldersIerarchy.split("\n");
        
        for (String s : split) {
            retDeq.add(s.replaceAll("(\\Q|\\E)*(\\d\\Q: \\E)", ""));
        }
        messageToUser.info(FileSystemWorkerOST.writeStringToFile(ConstantsOst.FILENAME_FOLDERSTXT, showFoldersIerarchy));
        return retDeq;
    }
    
    private String getLevelCounterStr(int counter) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < counter; i++) {
            stringBuilder.append("|");
        }
        return stringBuilder.toString();
    }
    
    private String parseFolder(PSTFolder folder, final StringBuilder stringBuilder) throws PSTException, IOException, NullPointerException {
        Vector<PSTFolder> rootSubFolders = folder.getSubFolders();
        Iterator<PSTFolder> iteratorFolder = rootSubFolders.iterator();
        
        while (iteratorFolder.hasNext()) {
            PSTFolder nextFold = iteratorFolder.next();
            levelCounter++;
            totalCounter++;
            String nextFoldDisplayName = nextFold.getDisplayName();
            String levelCounterStr = getLevelCounterStr(levelCounter);
    
            stringBuilder.append(levelCounterStr).append(levelCounter).append(": ").append(nextFoldDisplayName);
            stringBuilder.append(" (items: ").append(nextFold.getUnreadCount()).append("/").append(nextFold.getContentCount()).append(")")
                .append(" id ").append(nextFold.getDescriptorNodeId())
                .append("\n");
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
