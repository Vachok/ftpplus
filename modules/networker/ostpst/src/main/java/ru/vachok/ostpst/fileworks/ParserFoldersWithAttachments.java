// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import com.pff.PSTException;
import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTObject;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.utils.CharsetEncoding;
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
class ParserFoldersWithAttachments {
    
    
    private static final String CONTENT = ".inboxFolder";
    
    private PSTFile pstFile;
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    private PSTFolder rootFolder;
    
    private int levelCounter;
    
    private int totalCounter;
    
    private String thing;
    
    public ParserFoldersWithAttachments(String fileName, String thing) throws PSTException, IOException {
        this.thing = thing;
        try {
            this.pstFile = new PSTFile(fileName);
        }
        catch (FileNotFoundException e) {
            fileName = new CharsetEncoding("windows-1251", "UTF-8").getStrInAnotherCharset(fileName);
            this.pstFile = new PSTFile(fileName);
        }
    }
    
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
            this.pstFile = getPSTFile(fileName);
            int fileType = pstFile.getPSTFileType();
            messageToUser.info(getClass().getSimpleName() + ".ParserFoldersWithAttachments", "fileType", " = " + fileType);
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
    
    protected PSTFile getPSTFile(String fileName) {
        try {
            String anotherCharset = new CharsetEncoding("windows-1251", "UTF-8").getStrInAnotherCharset(fileName);
            return new PSTFile(anotherCharset);
        }
        catch (PSTException | IOException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Cant load PST: " + fileName);
    }
    
    private String getLevelCounterStr(int counter) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < counter; i++) {
            stringBuilder.append("|");
        }
        return stringBuilder.toString();
    }
    
    protected String parseFolder(PSTFolder folder, final StringBuilder stringBuilder) throws PSTException, IOException, NullPointerException {
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
