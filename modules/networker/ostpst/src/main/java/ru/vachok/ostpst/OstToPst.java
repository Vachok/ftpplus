package ru.vachok.ostpst;


import com.pff.*;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.UUID;
import java.util.Vector;


/**
 @since 29.04.2019 (11:24) */
public class OstToPst implements MakeConvert {
    
    
    private String fileName;
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    private int deepCount = -1;
    
    public OstToPst() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("app.properties"));
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        this.fileName = properties.getProperty("file");
        if (fileName == null) {
            fileName = "c:\\Users\\ikudryashov\\Desktop\\ksamarchenko@velkomfood.ru.ost";
        }
    }
    
    public static void main(String[] args) {
        throw new UnsupportedOperationException("Use Interface " + MakeConvert.class.getSimpleName());
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
    
    @Override public String convertToPST() {
        try {
            PSTFile pstFile = new PSTFile(fileName);
            return pstFile.getContentHandle().toString();
        }
        catch (PSTException | IOException e) {
            return e.getMessage();
        }
    }
    
    @Override public void showFileContent() {
        messageToUser.info(getClass().getSimpleName() + ".showFileContent", "fileName", " = " + fileName);
        try {
            PSTContent pstContent = new PSTContent(new PSTFile(fileName));
            pstContent.showStore(UUID.fromString("888054f3-95ff-4b10-a8a3-a3c3ec0c6dc1"));
        }
        catch (PSTException | IOException e) {
            messageToUser.error(e.getMessage());
        }
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
