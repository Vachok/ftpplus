// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import com.pff.PSTException;
import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTObject;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.utils.FileSystemWorker;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


/**
 @since 30.04.2019 (15:04) */
public class ParserFoldersWithAttachments {
    
    
    private static final String CONTENT = ".inboxFolder";
    
    private PSTFile pstFile;
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    private String fileName;
    
    public ParserFoldersWithAttachments(PSTFile pstFile) {
        this.pstFile = pstFile;
    }
    
    public ParserFoldersWithAttachments(String fileName) {
        this.fileName = fileName;
        try {
            this.pstFile = new PSTFile(fileName);
        }
        catch (PSTException | IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".PSTContentToFoldersWithAttachments", e));
        }
    }
    
    public String getContents() {
        StringBuilder stringBuilder = new StringBuilder();
        Collection<PSTObject> pstFolders = new Vector<>();
        int contentCount = 0;
        PSTFolder pstFileRootFolder = null;
        try {
            pstFileRootFolder = pstFile.getRootFolder();
            inboxFolder();
        }
        catch (PSTException | IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
    
    private void parseFolder(final PSTFolder rootFolder) throws PSTException, IOException {
        if (rootFolder.hasSubfolders()) {
            Vector<PSTFolder> rootSubFolders = rootFolder.getSubFolders();
            Iterator<PSTFolder> iteratorFolder = rootSubFolders.iterator();
            
            while (iteratorFolder.hasNext()) {
                PSTFolder nextFold = iteratorFolder.next();
                String foldDisplayName = nextFold.getDisplayName();
                parseFolder(Objects.requireNonNull(nextFold, "No folder"));
            }
        }
    }
    
    private void inboxFolder() throws PSTException, IOException {
        PSTFolder rootFolder = pstFile.getRootFolder();
        LinkedList<Integer> descriptorNodes = rootFolder.getChildDescriptorNodes();
        try {
            for (PSTFolder pstFolder : rootFolder.getSubFolders()) {
                Vector<PSTFolder> inSub = pstFolder.getSubFolders();
                inSub.forEach(x->{
                    try {
                        Path normalize = Paths.get(fileName).getParent().normalize();
                        String objPath = normalize.toAbsolutePath() + FileSystemWorker.SYSTEM_DELIMITER + x.getDisplayName();
                        
                        new ParserPSTMessages(x).saveMessageToDisk(x.getChildren(x.getContentCount()), x.getDisplayName(), Paths.get(objPath));
                    }
                    catch (PSTException | IOException e) {
                        messageToUser.error(e.getMessage());
                    }
                });
                
            }
        }
        catch (PSTException | IOException e) {
            messageToUser.error(e.getMessage());
        }
    }
    
}
