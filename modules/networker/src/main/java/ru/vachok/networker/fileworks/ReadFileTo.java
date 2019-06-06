// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.fileworks;



import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 Запись в файл

 @since 27.12.2018 (13:27) */
@Deprecated
public class ReadFileTo implements ProgrammFilesReader {

    private File fileToReadNameStr = null;

    private BufferedReader bufferedReader = null;

    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());


    @Override public Object call() throws Exception {
        return readFile(fileToReadNameStr);
    }


    @Override public String readFile(File fileToReadNameStr) {
        this.fileToReadNameStr = fileToReadNameStr;
        StringBuilder stringBuilder = new StringBuilder();
        try{
            if(bufReaderInitiator()) {
                bufferedReader.lines().forEach(stringBuilder::append);
                bufferedReader.close();
            }
            else { stringBuilder.append("READER IS NULL! ").append(getClass().getSimpleName()); }
        }catch(IOException e){
            messageToUser.error(e.getMessage());
        }
        return stringBuilder.toString();
    }


    @Override public List<String> readFileAsList(File fileToRead) {
        List<String> fileAsList = new ArrayList<>();
        bufferedReader.lines().forEach(fileAsList::add);
        return fileAsList;
    }


    private boolean bufReaderInitiator() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(fileToReadNameStr);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        this.bufferedReader = new BufferedReader(inputStreamReader);
        return true;
    }
}
