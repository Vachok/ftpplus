// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.fileworks;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;

import java.io.*;
import java.util.List;
import java.util.stream.Stream;


/**
 Class ru.vachok.networker.fileworks.WriteFilesTo
 <p>
 
 @since 06.04.2019 (17:48)
 @deprecated 17.07.2019 (0:36) */
@Deprecated
public class WriteFilesTo {
    
    
    private MessageToUser messageToUser = ru.vachok.networker.restapi.MessageToUser
        .getInstance(ru.vachok.networker.restapi.MessageToUser.LOCAL_CONSOLE, getClass().getSimpleName());
    
    private String fileName;
    
    public WriteFilesTo(String fileName) {
        this.fileName = fileName;
    }
    
    public boolean writeFile(@NotNull List<?> toWriteList) {
        File file = new File(fileName);
        writeFile(toWriteList.stream());
        return file.exists();
    }
    
    public boolean writeFile(@NotNull Stream<?> toWriteStream) {
        File file = new File(fileName);
        try (OutputStream outputStream = new FileOutputStream(file);
             PrintStream printStream = new PrintStream(outputStream, true)
        ) {
            toWriteStream.forEach(printStream::println);
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        return file.exists();
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}