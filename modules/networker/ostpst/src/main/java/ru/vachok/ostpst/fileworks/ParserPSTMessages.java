// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import com.pff.*;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.utils.FileSystemWorker;
import ru.vachok.ostpst.utils.TForms;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 @since 14.05.2019 (14:16) */
class ParserPSTMessages extends ParserFoldersWithAttachments {
    
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    private PSTFolder pstFolder;
    
    private long folderID;
    
    private String fileName;
    
    private ParserPSTMessages(PSTFile pstFile) {
        super(pstFile);
    }
    
    private ParserPSTMessages(String fileName) {
        super(fileName);
    }
    
    ParserPSTMessages(String fileName, long folderID) {
        super(fileName);
        this.fileName = fileName;
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
    
    Map<Long, String> getMessagesSubject() throws PSTException, IOException {
        Map<Long, String> retMap = new ConcurrentHashMap<>();
        for (PSTObject folderChild : pstFolder.getChildren(pstFolder.getContentCount())) {
            PSTMessage pstMessage = (PSTMessage) folderChild;
            retMap.put(pstMessage.getDescriptorNodeId(), pstMessage.getSubject() + " id: " + " (from: " + pstMessage.getSenderName() + ")");
        }
        ;
        FileSystemWorker.writeMapToFile(pstFolder.getDisplayName() + ".txt", retMap);
        return retMap;
    }
    
    String searchBySubj(String searchKey) throws PSTException, IOException {
        StringBuilder stringBuilder = new StringBuilder();
        int indexSrch = 0;
        try {
            Map<Long, String> subjectsMap = getMessagesSubject();
            List<String> subjectsList = new ArrayList<>(subjectsMap.values());
            Collections.sort(subjectsList);
            indexSrch = Collections.binarySearch(subjectsList, searchKey);
            String foundStr = subjectsList.get(indexSrch);
            stringBuilder.append(foundStr);
            long msgID = 0;
            for (Map.Entry<Long, String> entry : subjectsMap.entrySet()) {
                if (entry.getValue().equals(foundStr)) {
                    msgID = entry.getKey();
                }
            }
            showMessage(msgID);
        }
        catch (NullPointerException | IndexOutOfBoundsException e) {
            stringBuilder.append("Key: ").append(searchKey).append(" not found... Index =").append(indexSrch).append("\n").append(new TForms().fromArray(e));
        }
        return stringBuilder.toString();
    }
    
    private void showMessage(long msgID) throws PSTException, IOException {
        PSTMessage pstMessage = (PSTMessage) PSTObject.detectAndLoadPSTObject(new PSTFile(fileName), msgID);
        System.out.println(pstMessage.getBodyPrefix());
        System.out.println(pstMessage.getTransportMessageHeaders());
        if (pstMessage.hasAttachments()) {
            List<PSTAttachment> attachmentList = new ArrayList<>();
            int attachmentsNum = pstMessage.getNumberOfAttachments();
            for (int i = 0; i < attachmentsNum; i++) {
                PSTAttachment attachment = pstMessage.getAttachment(i);
                attachmentList.add(attachment);
            }
            for (PSTAttachment x : attachmentList) {
                System.out.println(x.getSize() + " " + x.getFilename());
            }
        }
    }
    
}
