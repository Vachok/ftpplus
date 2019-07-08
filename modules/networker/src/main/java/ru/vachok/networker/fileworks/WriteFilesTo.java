// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.fileworks;



import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.componentsrepo.DeprecatedException;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


/**
 Class ru.vachok.networker.fileworks.WriteFilesTo
 <p>

 @since 06.04.2019 (17:48) */
public class WriteFilesTo implements ProgrammFilesWriter {

    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    private String fileName;


    public WriteFilesTo(String fileName) {
        this.fileName = fileName;
    }


    @Override public boolean writeFile(List<?> toWriteList) {
        File file = new File(fileName);
        writeFile(toWriteList.stream());
        return file.exists();
    }


    @Override public boolean writeFile(Stream<?> toWriteStream) {
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


    @Override public String error(String fileName , Exception e) {
        throw new DeprecatedException("Since 09.07.2019 (2:38)");
    }


    @Override public boolean writeFile(Map<?, ?> toWriteMap) {
        return false;
    }


    @Override public boolean writeFile(File toWriteFile) {
        return false;
    }


    @Override public boolean writeFile(Exception e) {
        return false;
    }


    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     Computes a result, or throws an exception if unable to do so.

     @return computed result

     @throws Exception if unable to compute a result
     */
    @Override public String call() throws Exception {
        return null;
    }
}