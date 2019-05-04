// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst;


import com.pff.*;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.stats.files.FileSystemWorker;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.stream.Stream;


/**
 @since 30.04.2019 (15:04) */
class PSTContent {
    
    
    private static final String CONTENT = ".getSubFolderContent";
    
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
            for (PSTFolder folder : subFolders) {
                getSubFolderContent(folder);
            }
            return subFolders.stream();
        }
        catch (PSTException | IOException e) {
            messageToUser.error(e.getMessage());
            Stream<PSTFolder> pstFolderStream = null;
            return pstFolderStream;
        }
    }
    
    private void getSubFolderContent(PSTFolder folder) throws PSTException, IOException {
        Vector<PSTFolder> folders = folder.getSubFolders();
        
        try {
            for (PSTFolder pstFolder : folders) {
                String name = pstFolder.getDisplayName();
                if (name.contains("Входящие")) {
                    String aClass = pstFolder.getContainerClass();
                    Vector<PSTFolder> inSub = pstFolder.getSubFolders();
                    messageToUser.info(getClass().getSimpleName() + CONTENT, "name", " = " + inSub.size());
                    inSub.forEach(x->{
                        try {
                            parseInbox(x.getChildren(x.getContentCount()));
                        }
                        catch (PSTException | IOException e) {
                            messageToUser.error(e.getMessage());
                        }
                    });
                }
            }
        }
        catch (PSTException | IOException e) {
            messageToUser.error(e.getMessage());
        }
    }
    
    private void parseInbox(Vector<PSTObject> pstObjs) {
        Map<DescriptorIndexNode, String> nodes = new HashMap<>();
        for (PSTObject object : pstObjs) {
            PSTMessage message = (PSTMessage) object;
            Path msgPath = Paths.get(".");
            String path = msgPath.toAbsolutePath().toString().replace(".", "obj\\");
            try (OutputStream outputStream = new FileOutputStream(path + object.getDescriptorNodeId() + ".pstparser")) {
                if (object.getMessageClass().toLowerCase().contains("note")) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(message.toString());
                    if (message.hasAttachments()) {
                        hasAtt(path, message, stringBuilder);
                    }
                    outputStream.write(stringBuilder.toString().getBytes());
                }
            }
            catch (IOException | PSTException e) {
                messageToUser.error(e.getMessage());
            }
        }
    }
    
    private void hasAtt(String path, PSTMessage message, StringBuilder stringBuilder) throws PSTException, IOException {
        for (int i = 0; i < message.getNumberOfAttachments(); i++) {
            stringBuilder.append(i).append(" attachment\n\n\n");
            PSTAttachment attachment = message.getAttachment(i);
            try (InputStream attStream = attachment.getFileInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(attStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                 OutputStream attOut = new FileOutputStream(path + attachment.getFilename());
            ) {
                byte[] bytes = new byte[attachment.getSize()];
                int readB = attStream.read(bytes);
                stringBuilder.append(readB).append(" bts");
                attOut.write(bytes);
            }
            catch (IOException e) {
                messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".parseInbox", e));
            }
        }
    }
    
    private void getContents(PSTMessageStore store) {
        DescriptorIndexNode node = store.getDescriptorNode();
        String s = node.toString();
        messageToUser.info(getClass().getSimpleName() + ".getContents", "s", " = " + s);
    }
}
