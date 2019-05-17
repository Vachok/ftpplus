package ru.vachok.ostpst.fileworks;


import com.pff.PSTException;
import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTObject;
import ru.vachok.ostpst.utils.TForms;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Vector;


class ParserObjects {
    
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
    
    void getObjects() {
        int nodeType = object.getNodeType();
        if (nodeType == 2) {
            getFolders();
        }
        else {
            throw new IllegalComponentStateException("16.05.2019 (15:59)");
        }
    }
    
    String getObjectItemsString() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            PSTObject loadedObject = PSTObject.detectAndLoadPSTObject(pstFile, objID);
            this.object = loadedObject;
            stringBuilder.append(loadedObject.getDisplayName()).append("\n");
            stringBuilder.append(loadedObject.getItemsString()).append("\n\n");
        }
        catch (IOException | PSTException e) {
            stringBuilder.append(e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        return stringBuilder.toString();
    }
    
    private void printObjectDescriptorsToFile(Vector<PSTObject> objectVector, String objName) {
        System.out.println(objName + " size = " + objectVector.size());
        for (PSTObject pstObject : objectVector) {
            try (OutputStream outputStream = new FileOutputStream(objName)) {
                String objectItemsString = String.valueOf(pstObject.getDescriptorNode());
                outputStream.write(objectItemsString.getBytes());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void getFolders() {
        PSTFolder pstFolder = (PSTFolder) object;
        ParserFoldersWithAttachments parserFoldersWithAttachments = new ParserFoldersWithAttachments(pstFolder);
        try {
            parserFoldersWithAttachments.showFoldersIerarchy();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
