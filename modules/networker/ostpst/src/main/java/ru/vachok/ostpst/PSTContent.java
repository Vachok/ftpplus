// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst;


import com.pff.*;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.stats.files.FileSystemWorker;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;


/**
 @since 30.04.2019 (15:04) */
class PSTContent {
    
    
    private static final String CONTENT = ".getSubFolderContent";
    
    private int fCount;
    
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
        Vector<PSTFolder> subFolders;
        try {
            PSTFolder rootFolder = pstFile.getRootFolder();
            subFolders = rootFolder.getSubFolders();
            showSubFolders(rootFolder);
        }
        catch (PSTException | IOException e) {
            messageToUser.error(e.getMessage());
            Stream<PSTFolder> pstFolderStream = null;
            return pstFolderStream;
        }
        return subFolders.stream();
    }
    
    private void showSubFolders(final PSTFolder rootFolder) throws PSTException, IOException {
        messageToUser.info(getClass().getSimpleName() + ".showSubFolders", "rootFolder", " = " + rootFolder.getDisplayName());
        if (rootFolder.hasSubfolders()) {
            Vector<PSTFolder> rootSubFolders = rootFolder.getSubFolders();
            ++fCount;
            Iterator<PSTFolder> iteratorFolder = rootSubFolders.iterator();
            while (iteratorFolder.hasNext()) {
                PSTFolder nextFold = iteratorFolder.next();
                messageToUser.info(getClass().getSimpleName() + ".showSubFolders", "iteratorFolder", " = " + nextFold.getDisplayName());
                if (nextFold.getDisplayName().toLowerCase().contains("входящие")) {
                    getSubFolderContent(nextFold);
                }
                this.showSubFolders(nextFold);
            }
        }
        messageToUser.info(getClass().getSimpleName() + ".showSubFolders", "fCount", " = " + fCount);
    }
    
    private void getSubFolderContent(PSTFolder folder) throws PSTException, IOException {
        Vector<PSTFolder> folders = folder.getSubFolders();
        try {
            for (PSTFolder pstFolder : folders) {
                ++fCount;
                String name = pstFolder.getDisplayName();
                String aClass = pstFolder.getContainerClass();
                Vector<PSTFolder> inSub = pstFolder.getSubFolders();
                inSub.forEach(x->{
                    try {
                        messageToUser.info(getClass().getSimpleName(), "PARSING:", " = " + x.getDisplayName());
                        parseFld(x.getChildren(x.getContentCount()), x.getDisplayName());
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
    
    private void parseFld(Vector<PSTObject> pstObjs, String name) throws IOException {
        Map<DescriptorIndexNode, String> nodes = new HashMap<>();
        Path msgPath = Paths.get(".");
        String path = msgPath.toAbsolutePath().toString().replace(".", "obj\\" + name + "\\");
        for (PSTObject object : pstObjs) {
            PSTMessage message = (PSTMessage) object;
            Files.createDirectories(Paths.get(path));
            try (OutputStream outputStream = new FileOutputStream(path + object.getDescriptorNodeId() + ".pstparser")) {
                if (object.getMessageClass().toLowerCase().contains("note")) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(message.getItemsString());
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
        for (PSTObject object : pstObjs) {
            if (object.getMessageClass().toLowerCase().contains("contact")) {
                parseContact(object, path);
            }
        }
    }
    
    private void parseContact(PSTObject object, String path) {
        PSTContact contact = (PSTContact) object;
        try (OutputStream outputStream = new FileOutputStream(path + contact.getDescriptorNodeId() + ".contact")) {
            outputStream.write(contact.toString().getBytes());
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
    }
    
    private void hasAtt(String path, PSTMessage message, StringBuilder stringBuilder) throws PSTException, IOException {
        for (int i = 0; i < message.getNumberOfAttachments(); i++) {
            stringBuilder.append(i).append(" attachment\n\n\n");
            PSTAttachment attachment = message.getAttachment(i);
            try (InputStream attStream = attachment.getFileInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(attStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                 OutputStream attOut = new FileOutputStream(path + message.getDescriptorNodeId() + "_" + attachment.getFilename());
            ) {
                byte[] bytes = new byte[attachment.getSize()];
                int readB = attStream.read(bytes);
                stringBuilder.append(readB).append(" bts ").append(attachment.getMimeTag()).append(" ").append(attachment.getMessageClass());
                attOut.write(bytes);
            }
            catch (IOException e) {
                messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".parseFld", e));
            }
        }
    }
    
    private void getContents(PSTMessageStore store) {
        DescriptorIndexNode node = store.getDescriptorNode();
        String s = node.toString();
        messageToUser.info(getClass().getSimpleName() + ".getContents", "s", " = " + s);
    }
}
