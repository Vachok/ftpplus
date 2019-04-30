package ru.vachok.ostpst;


import com.pff.*;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;

import java.io.IOException;
import java.util.UUID;
import java.util.Vector;
import java.util.stream.Stream;


/**
 @since 30.04.2019 (15:04) */
class PSTContent {
    
    
    private PSTFile pstFile;
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    public PSTContent(PSTFile pstFile) {
        this.pstFile = pstFile;
    }
    
    public void showStore(UUID itemID) {
        try {
            PSTMessageStore messageStore = pstFile.getMessageStore();
            boolean equals = itemID.equals(messageStore.getTagRecordKeyAsUUID());
            if (equals) {
                getContents(messageStore);
            }
            else {
                messageToUser.warn(equals + " is store read");
            }
        }
        catch (PSTException | IOException e) {
            messageToUser.error(e.getMessage());
        }
    }
    
    Stream<PSTFolder> showFolders() {
        try {
            PSTFolder rootFolder = pstFile.getRootFolder();
            Vector<PSTFolder> subFolders = rootFolder.getSubFolders();
            return subFolders.stream();
        }
        catch (PSTException | IOException e) {
            messageToUser.error(e.getMessage());
            Stream<PSTFolder> pstFolderStream = null;
            return pstFolderStream;
        }
    }
    
    private void getContents(PSTMessageStore store) {
        DescriptorIndexNode node = store.getDescriptorNode();
        String s = node.toString();
        messageToUser.info(getClass().getSimpleName() + ".getContents", "s", " = " + s);
    }
    
}
