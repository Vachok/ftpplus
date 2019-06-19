// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import com.pff.*;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.api.MessageToUser;
import ru.vachok.ostpst.api.MessengerOST;
import ru.vachok.ostpst.usermenu.MenuConsoleLocal;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;


/**
 @since 14.05.2019 (14:16) */
class ParserPSTMessages extends ParserFolders {
    
    
    private MessageToUser messageToUser = new MessengerOST(getClass().getSimpleName());
    
    public MessageToUser getMessageToUser() {
        return messageToUser;
    }
    
    public void setMessageToUser(MessageToUser messageToUser) {
        this.messageToUser = messageToUser;
    }
    
    private MemoryMXBean mxBean = ManagementFactory.getMemoryMXBean();
    
    private PSTFolder pstFolder;
    
    private String fileName;
    
    public ParserPSTMessages(PSTFolder pstObject) {
        super(pstObject);
        this.pstFolder = pstObject;
    }
    
    ParserPSTMessages(String fileName, long folderID) {
        super(fileName);
        this.fileName = fileName;
        try {
            this.pstFolder = (PSTFolder) PSTObject.detectAndLoadPSTObject(new PSTFile(this.fileName), folderID);
        }
        catch (IOException | PSTException e) {
            e.printStackTrace();
            new MenuConsoleLocal(fileName).showMenu();
        }
    }
    
    protected ParserPSTMessages(String fileName, String thing) throws PSTException, IOException {
        super(fileName, thing);
        String thing1 = thing.toLowerCase();
        this.fileName = fileName;
    }
    
    private ParserPSTMessages(PSTFile pstFile) {
        super(pstFile);
    }
    
    private ParserPSTMessages(String fileName) {
        super(fileName);
    }
    
    void saveMessageToDisk(Vector<PSTObject> pstObjs, String name, Path fldPath) {
        
        for (PSTObject object : pstObjs) {
            PSTMessage message = (PSTMessage) object;
            String fileOutPath = fldPath.toAbsolutePath() + ConstantsOst.SYSTEM_SEPARATOR + message.getDescriptorNodeId() + ".msg";
            
            try (OutputStream outputStream = new FileOutputStream(fileOutPath)) {
                if (object.getMessageClass().toLowerCase().contains("note")) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(message.getItemsString());
                    if (message.hasAttachments()) {
                        new WriterMessageAndAttachments().saveAttachment(fileOutPath, message, stringBuilder);
                    }
                    outputStream.write(stringBuilder.toString().getBytes());
                }
            }
            catch (IOException e) {
                messageToUser.error(e.getMessage());
            }
        }
    }
    
    Map<Long, String> getMessagesSubject() throws PSTException, IOException {
        Map<Long, String> retMap = new ConcurrentHashMap<>();
        for (PSTObject folderChild : pstFolder.getChildren(pstFolder.getContentCount())) {
            if (folderChild instanceof PSTMessage) {
                PSTMessage pstMessage = (PSTMessage) folderChild;
                retMap.put(pstMessage.getDescriptorNodeId(), pstMessage.getSubject() + " (from: " + pstMessage.getSenderName() + " sent: " + pstMessage.getMessageDeliveryTime() + ")");
                Thread.currentThread().setName(String.valueOf(mxBean.getHeapMemoryUsage()));
            }
        }
        ;
        return retMap;
    }
    
    String showMessage(long msgID) throws PSTException, IOException {
        PSTObject pstObject = PSTObject.detectAndLoadPSTObject(new PSTFileNameConverter().getPSTFile(fileName), msgID);
        StringBuilder stringBuilder = new StringBuilder();
        if (pstObject instanceof PSTMessage) {
            PSTMessage pstMessage = (PSTMessage) pstObject;
            new WriterMessageAndAttachments()
                .saveAttachment(Paths.get(".").toAbsolutePath().normalize() + ConstantsOst.SYSTEM_SEPARATOR + ConstantsOst.STR_ATTACHMENTS, pstMessage, stringBuilder);
            stringBuilder.append("\n").append(pstMessage.getSubject()).append(", from:").append(pstMessage.getSenderName())
                .append(", sent:").append(pstMessage.getMessageDeliveryTime()).append(", id = ").append(pstMessage.getDescriptorNodeId()).append("\n");
        }
        return stringBuilder.toString();
    }
    
    class DeleterAttachments extends SimpleFileVisitor<Path> {
        
        
        @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            file.toFile().delete();
            return FileVisitResult.CONTINUE;
        }
        
        @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            dir.toFile().delete();
            return FileVisitResult.CONTINUE;
        }
    }
}
