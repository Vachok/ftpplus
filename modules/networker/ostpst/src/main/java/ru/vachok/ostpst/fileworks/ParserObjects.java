package ru.vachok.ostpst.fileworks;


import com.pff.PSTFolder;
import com.pff.PSTObject;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Vector;


class ParserObjects {
    
    
    private PSTFolder pstFolder;
    
    private Iterable<PSTFolder> pstFolderVector;
    
    private Path recordPath;
    
    private PSTObject object;
    
    public ParserObjects(PSTObject object) {
        this.object = object;
        this.pstFolderVector = new Vector<>();
    }
    
    ParserObjects(Iterable<PSTFolder> pstFolderVector) {
        this.pstFolderVector = pstFolderVector;
    }
    
    void getObjects() {
        int nodeType = object.getNodeType();
        if (nodeType == 2) {
            getFolders(object);
        }
        else {
            throw new IllegalComponentStateException("16.05.2019 (15:59)");
        }
    }
    
    private void getFolders(PSTObject object) {
        this.pstFolder = (PSTFolder) object;
        ParserFoldersWithAttachments parserFoldersWithAttachments = new ParserFoldersWithAttachments(pstFolder);
        parserFoldersWithAttachments.showFoldersIerarchy();
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
}
