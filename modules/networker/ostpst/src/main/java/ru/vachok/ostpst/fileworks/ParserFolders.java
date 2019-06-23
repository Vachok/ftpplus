// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import com.pff.PSTException;
import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTObject;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.api.MessageToUser;
import ru.vachok.ostpst.api.MessengerOST;
import ru.vachok.ostpst.utils.FileSystemWorkerOST;
import ru.vachok.ostpst.utils.TFormsOST;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 @since 30.04.2019 (15:04) */
class ParserFolders {
    
    
    private static final String CONTENT = ".inboxFolder";
    
    private PSTFile pstFile;
    
    private MessageToUser messageToUser = new MessengerOST(getClass().getSimpleName());
    
    public MessageToUser getMessageToUser() {
        return messageToUser;
    }
    
    public void setMessageToUser(MessageToUser messageToUser) {
        this.messageToUser = messageToUser;
    }
    
    private PSTFolder rootFolder;
    
    private int levelCounter;
    
    private int totalCounter;
    
    private String thing;
    
    public ParserFolders(String fileName, String thing) throws PSTException, IOException {
        this.thing = thing;
        try {
            this.pstFile = new PSTFile(fileName);
        }
        catch (FileNotFoundException e) {
            this.pstFile = new PSTFileNameConverter().getPSTFile(fileName);
        }
    }
    
    ParserFolders(PSTFolder folder) {
        this.rootFolder = folder;
    }
    
    ParserFolders(PSTFile pstFile) {
        this.pstFile = pstFile;
    }
    
    ParserFolders(String fileName) {
        this.pstFile = new PSTFileNameConverter().getPSTFile(fileName);
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
        stringBuilder.append("totalCounter = ").append(totalCounter);
        return stringBuilder.toString();
    }
    
    Deque<String> getDeqFolderNamesWithIDAndWriteToDisk() throws IOException {
        Deque<String> retDeq = new ConcurrentLinkedDeque<>();
        String showFoldersIerarchy = showFoldersIerarchy();
        String[] split = showFoldersIerarchy.split("\n");
        
        for (String s : split) {
            retDeq.add(s.replaceAll("(\\Q|\\E)*(\\d\\Q: \\E)", ""));
        }
        messageToUser.info(String.valueOf(FileSystemWorkerOST.writeStringToFile(ConstantsOst.FILENAME_FOLDERSTXT, showFoldersIerarchy)));
        return retDeq;
    }
    
    protected String showFoldersIerarchy(String name) {
        long folderId = Long.parseLong(name.split(" id ")[1]);
        StringBuilder stringBuilder = new StringBuilder();
        try {
            PSTFolder pstFolder = (PSTFolder) PSTObject.detectAndLoadPSTObject(pstFile, folderId);
            String parseFolderStr = parseFolder(pstFolder, stringBuilder);
            if (parseFolderStr.contains(thing)) {
                stringBuilder.append(parseFolderStr);
            }
        }
        catch (IOException | PSTException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TFormsOST().fromArray(e));
        }
        return stringBuilder.toString();
    }
    
    private String getLevelCounterStr(int counter) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < counter; i++) {
            stringBuilder.append("|");
        }
        return stringBuilder.toString();
    }
    
    protected String parseFolder(PSTFolder folder, final StringBuilder stringBuilder) throws IOException {
        Vector<PSTFolder> rootSubFolders = null;
        try {
            rootSubFolders = folder.getSubFolders();
        }
        catch (PSTException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".parseFolder");
        }
        try {
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
        }
        catch (NullPointerException e) {
            System.err.println("rootSubFolders " + e.getMessage());
        }
        levelCounter--;
        return stringBuilder.toString();
    }
}
