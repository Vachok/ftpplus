package ru.vachok.ostpst.fileworks;


import com.pff.PSTException;
import com.pff.PSTFile;
import com.pff.PSTObject;
import ru.vachok.ostpst.api.MessageToUser;
import ru.vachok.ostpst.api.MessengerOST;
import ru.vachok.ostpst.utils.FileSystemWorkerOST;
import ru.vachok.ostpst.utils.TFormsOST;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;


class ParserObjects {
    
    
    private String fileName;
    
    private MessageToUser messageToUser = new MessengerOST(getClass().getSimpleName());
    
    public MessageToUser getMessageToUser() {
        return messageToUser;
    }
    
    public void setMessageToUser(MessageToUser messageToUser) {
        this.messageToUser = messageToUser;
    }
    
    public ParserObjects(String fileName, long objID) {
        this.fileName = fileName;
        this.objID = objID;
        this.pstFile = new PSTFileNameConverter().getPSTFile(fileName);
    }
    
    private Path recordPath;
    
    private PSTObject object;
    
    private PSTFile pstFile;
    
    private long objID;
    
    ParserObjects(PSTFile pstFile, long objID) {
        this.pstFile = pstFile;
        this.objID = objID;
    }
    
    ParserObjects(PSTObject object) {
        this.object = object;
    }
    
    public Optional<PSTObject> getObject() {
        Optional<PSTObject> pstObject = null;
        try {
            pstObject = Optional.ofNullable(PSTObject.detectAndLoadPSTObject(pstFile, objID));
        }
        catch (IOException | PSTException e) {
            messageToUser.error(FileSystemWorkerOST.error(getClass().getSimpleName() + ".getObject", e));
        }
        return pstObject;
    }
    
    
    String getObjectDescriptorID() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            PSTObject loadedObject = PSTObject.detectAndLoadPSTObject(pstFile, objID);
            this.object = loadedObject;
        }
        catch (IOException | PSTException e) {
            stringBuilder.append(e.getMessage() + "\n" + new TFormsOST().fromArray(e));
        }
        stringBuilder.append(objID).append(" id parsed.").append("\n");
    
        stringBuilder.append(object.getDisplayName()).append("\n");
        stringBuilder.append(object.getDescriptorNode()).append(" descriptor node");
    
        return stringBuilder.toString();
    }
    
    private void printObjectDescriptorsToFile() {
        throw new IllegalComponentStateException("17.05.2019 (16:20)");
    }
}
