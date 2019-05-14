// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import com.pff.*;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.utils.FileSystemWorker;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 @since 30.04.2019 (15:04) */
public class PSTContentToFoldersWithAttachments {
    
    
    private static final String CONTENT = ".getSubFolderContent";
    
    private int fCount;
    
    private PSTFile pstFile;
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    public PSTContentToFoldersWithAttachments(PSTFile pstFile) {
        this.pstFile = pstFile;
    }
    
    public PSTContentToFoldersWithAttachments(String fileName) {
        try {
            this.pstFile = new PSTFile(fileName);
        }
        catch (PSTException | IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".PSTContentToFoldersWithAttachments", e));
        }
    }
    
    public Stream<PSTFolder> showFolders() {
        Vector<PSTFolder> subFolders;
        try {
            PSTFolder rootFolder = pstFile.getRootFolder();
            subFolders = rootFolder.getSubFolders();
        }
        catch (PSTException | IOException e) {
            messageToUser.error(e.getMessage());
            Stream<PSTFolder> pstFolderStream = null;
            return pstFolderStream;
        }
        return subFolders.stream();
    }
    
    void saveSubFolders(final PSTFolder rootFolder) throws PSTException, IOException {
        messageToUser.info(getClass().getSimpleName() + ".saveSubFolders", "rootFolder", " = " + rootFolder.getDisplayName());
        if (rootFolder.hasSubfolders()) {
            Vector<PSTFolder> rootSubFolders = rootFolder.getSubFolders();
            ++fCount;
            Iterator<PSTFolder> iteratorFolder = rootSubFolders.iterator();
            while (iteratorFolder.hasNext()) {
                PSTFolder nextFold = iteratorFolder.next();
                messageToUser.info(getClass().getSimpleName() + ".saveSubFolders", "iteratorFolder", " = " + nextFold.getDisplayName());
                if (nextFold.getDisplayName().toLowerCase().contains("входящие")) {
                    getSubFolderContent(nextFold);
                }
                this.saveSubFolders(nextFold);
            }
        }
        messageToUser.info(getClass().getSimpleName() + ".saveSubFolders", "fCount", " = " + fCount);
    }
    
    public String getContents() {
        StringBuilder stringBuilder = new StringBuilder();
        Collection<PSTFolder> pstFolders = showFolders().collect(Collectors.toList());
        for (PSTFolder folder : pstFolders) {
            String folderDisplayName = folder.getDisplayName();
            stringBuilder.append(folderDisplayName).append("\n");
            try {
                stringBuilder.append(folder.getAssociateContentCount()).append(" count");
                stringBuilder.append(" subfolders: ").append(folder.hasSubfolders());
                stringBuilder.append("\nItems: \n").append(folder.getItemsString());
            }
            catch (NullPointerException e) {
                stringBuilder.append(e.getMessage());
            }
        }
        return stringBuilder.toString();
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
                        parseFldAndSaveToDisk(x.getChildren(x.getContentCount()), x.getDisplayName());
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
    
    private void parseFldAndSaveToDisk(Vector<PSTObject> pstObjs, String name) throws IOException {
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
                messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".parseFldAndSaveToDisk", e));
            }
        }
    }
}
