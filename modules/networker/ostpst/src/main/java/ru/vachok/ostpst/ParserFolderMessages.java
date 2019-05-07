package ru.vachok.ostpst;


import com.pff.PSTException;
import com.pff.PSTFolder;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 @since 07.05.2019 (14:54) */
public class ParserFolderMessages {
    
    
    private final MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    private PSTFolder folderForParse;
    
    private PrintStream printStream;
    
    private String writtenFileName;
    
    public ParserFolderMessages(PSTFolder folderForParse) {
        this.folderForParse = folderForParse;
        Path rootPath = Paths.get("");
        this.writtenFileName = rootPath.toAbsolutePath() + "\\obj\\" + folderForParse + ".pstfld";
        try (OutputStream outputStream = new FileOutputStream(writtenFileName)) {
            this.printStream = new PrintStream(outputStream);
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
    }
    
    String showFolderContents() {
        try {
            if (folderForParse.getEmailCount() > 0) {
                printStream.println(folderForParse.getItemsString());
                boolean checkError = printStream.checkError();
                this.writtenFileName = writtenFileName + " " + checkError;
            }
        }
        catch (IOException | PSTException e) {
            this.writtenFileName = e.getMessage();
        }
        return writtenFileName;
    }
}
