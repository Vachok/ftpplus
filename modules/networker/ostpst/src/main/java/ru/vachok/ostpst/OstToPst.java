// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst;


import com.pff.*;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.utils.FileSystemWorker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;


/**
 @since 29.04.2019 (11:24) */
public class OstToPst implements MakeConvert {
    
    
    private String fileName;
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    private PSTContentToFoldersWithAttachments foldersWithAtt;
    
    private int deepCount = -1;
    
    public OstToPst(String fileName) {
        this.fileName = fileName;
        try {
            this.foldersWithAtt = new PSTContentToFoldersWithAttachments(new PSTFile(fileName));
        }
        catch (PSTException | IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".OstToPst", e));
        }
    }
    
    @Override public long copyierWithSave() {
        try {
            File file = new File(fileName);
            PSTRAFileContent pstraFileContent = new PSTRAFileContent(file);
            RNDFileread rndFileread = new RNDFileread(pstraFileContent);
            long retLong = file.length();
            while (retLong > 0) {
                retLong = file.length() - rndFileread.readRNDFileContentFromPosition();
                messageToUser.info(getClass().getSimpleName() + ".copyierWithSave", String.valueOf(file.length()), " = " + retLong);
            }
            return retLong;
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
            return -1;
        }
    }
    
    @Override public String folderContentItemsString() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(foldersWithAtt.getContents());
        return stringBuilder.toString();
    }
    
    @Override public String convertToPST() {
        try {
            PSTFile pstFile = new PSTFile(fileName);
            return pstFile.getContentHandle().toString();
        }
        catch (PSTException | IOException e) {
            return e.getMessage();
        }
    }
    
    @Override public void saveFolders() {
        foldersWithAtt.showFolders();
    }
    
    @Override public void saveContacts() {
        ParserContacts contacts = new ParserContacts(fileName);
        contacts.run();
    }
    
    @Override public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    private String convertToPst(PSTFile pstFile) throws PSTException, IOException {
        PSTFolder rootFolder = pstFile.getRootFolder();
        parseFolder(rootFolder);
        return "";
    }
    
    private void parseFolder(final PSTFolder folder) throws PSTException, IOException {
        this.deepCount++;
        if (deepCount > 0) {
            if (folder.getContentCount() > 0) {
                writeFolder(folder);
            }
        }
        if (folder.hasSubfolders()) {
            Vector<PSTFolder> pstFolders = folder.getSubFolders();
            for (PSTFolder pstFolder : pstFolders) {
                this.parseFolder(pstFolder);
                messageToUser.info(getClass().getSimpleName() + ".parseFolder", "deepCount", " = " + deepCount);
            }
        }
    }
    
    private void writeFolder(PSTFolder folder) throws PSTException, IOException {
        String pathName = folder.getDisplayName() + ".fldr";
        Path toPath = Paths.get(".");
        File pstOut = new File(toPath + "\\msg\\" + pathName);
        Vector<PSTObject> folderChildren = folder.getChildren(folder.getContentCount());
        messageToUser.info(getClass().getSimpleName() + ".writeFolder", "folderChildren", " = " + folderChildren.size());
    }
}
