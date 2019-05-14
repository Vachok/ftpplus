package ru.vachok.ostpst.fileworks;


import com.pff.PSTException;
import com.pff.PSTFolder;
import com.pff.PSTObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Vector;


class ParserObjects {
    
    
    private PSTFolder pstFolder;
    
    private Vector<PSTFolder> pstFolderVector;
    
    private Path recordPath;
    
    public ParserObjects(Vector<PSTFolder> pstFolderVector) {
        this.pstFolderVector = pstFolderVector;
    }
    
    void getObjects(String objectType) {
        for (PSTFolder f : pstFolderVector) {
            if (f.hasSubfolders()) {
                System.out.println("f = " + f.getDisplayName());
                try {
                    for (PSTFolder folder : f.getSubFolders()) {
                        printObjectDescriptorsToFile(folder.getChildren(folder.getContentCount()), folder.getDisplayName());
                    }
                }
                catch (PSTException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
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
