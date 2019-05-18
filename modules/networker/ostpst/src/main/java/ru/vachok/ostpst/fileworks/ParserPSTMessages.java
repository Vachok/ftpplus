// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import com.pff.*;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.utils.FileSystemWorker;
import ru.vachok.ostpst.utils.TForms;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;


/**
 @since 14.05.2019 (14:16) */
class ParserPSTMessages extends ParserFoldersWithAttachments {
    
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    private PSTFolder pstFolder;
    
    private long folderID;
    
    private ParserPSTMessages(PSTFile pstFile) {
        super(pstFile);
    }
    
    private ParserPSTMessages(String fileName) {
        super(fileName);
    }
    
    ParserPSTMessages(String fileName, long folderID) {
        super(fileName);
        try {
            this.pstFolder = (PSTFolder) PSTObject.detectAndLoadPSTObject(new PSTFile(fileName), folderID);
        }
        catch (IOException | PSTException e) {
            e.printStackTrace();
        }
    }
    
    void saveMessageToDisk(Vector<PSTObject> pstObjs, String name, Path fldPath) {
        
        for (PSTObject object : pstObjs) {
            PSTMessage message = (PSTMessage) object;
            String fileOutPath = fldPath.toAbsolutePath() + FileSystemWorker.SYSTEM_DELIMITER + message.getDescriptorNodeId() + ".msg";
            
            try (OutputStream outputStream = new FileOutputStream(fileOutPath)) {
                if (object.getMessageClass().toLowerCase().contains("note")) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(message.getItemsString());
                    if (message.hasAttachments()) {
                        saveAttachment(fileOutPath, message, stringBuilder);
                    }
                    outputStream.write(stringBuilder.toString().getBytes());
                }
            }
            catch (IOException | PSTException e) {
                messageToUser.error(e.getMessage());
            }
        }
    }
    
    private void saveAttachment(String path, PSTMessage message, StringBuilder stringBuilder) throws PSTException, IOException {
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
                messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".saveMessageToDisk", e));
            }
        }
    }
    
    List<String> getMessagesSubject() throws PSTException, IOException {
        List<String> stringList = new ArrayList<>();
        for (PSTObject folderChild : pstFolder.getChildren(pstFolder.getContentCount())) {
            PSTMessage pstMessage = (PSTMessage) folderChild;
            stringList.add(pstMessage.getSubject() + " id: " + pstMessage.getDescriptorNodeId() + " (from: " + pstMessage.getSenderName() + ")");
        }
        ;
        return stringList;
    }
    
    String searchBySubj(String searchKey) throws PSTException, IOException {
        StringBuilder stringBuilder = new StringBuilder();
        int indexSrch = 0;
        try {
            List<String> subjectsList = getMessagesSubject();
            Collections.sort(subjectsList);
            
            indexSrch = Collections.binarySearch(subjectsList, searchKey);
            stringBuilder.append(subjectsList.get(indexSrch));
        }
        catch (NullPointerException | IndexOutOfBoundsException e) {
            stringBuilder.append("Folder ID is not set! Key: ").append(searchKey).append(" not found... Index =").append(indexSrch).append("\n").append(new TForms().fromArray(e));
        }
        return stringBuilder.toString();
    }
    
}
