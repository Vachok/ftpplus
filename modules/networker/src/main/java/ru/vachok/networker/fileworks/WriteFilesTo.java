// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.fileworks;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.*;
import java.util.List;
import java.util.stream.Stream;


/**
 Class ru.vachok.networker.fileworks.WriteFilesTo
 <p>
 @deprecated 17.07.2019 (0:36)
 @since 06.04.2019 (17:48) */
@Deprecated
public class WriteFilesTo {

    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
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
        try(OutputStream outputStream = new FileOutputStream(file);
            PrintStream printStream = new PrintStream(outputStream , true)
        )
        {
            toWriteStream.forEach(printStream::println);
        }catch(IOException e){
            messageToUser.error(e.getMessage());
        }
        return file.exists();
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}