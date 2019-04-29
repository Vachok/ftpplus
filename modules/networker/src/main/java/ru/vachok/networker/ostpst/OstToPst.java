package ru.vachok.networker.ostpst;


import com.pff.PSTException;
import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTObject;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;


/**
 @since 29.04.2019 (11:24) */
public class OstToPst implements Runnable {
    
    
    private static String fileName = "c:\\Users\\ikudryashov\\Desktop\\ksamarchenko@velkomfood.ru.ost";
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    private int deepCount = -1;
    
    public static void main(String[] args) {
        new OstToPst().run();
    }
    
    @Override public void run() {
        try {
            PSTFile pstFile = new PSTFile(fileName);
            convertToPst(pstFile);
        }
        catch (PSTException | IOException e) {
            messageToUser.error(e.getMessage());
        }
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
