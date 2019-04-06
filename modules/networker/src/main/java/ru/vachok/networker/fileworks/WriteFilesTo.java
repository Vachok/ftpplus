package ru.vachok.networker.fileworks;



import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


/**
 Class ru.vachok.networker.fileworks.WriteFilesTo
 <p>

 @since 06.04.2019 (17:48) */
public class WriteFilesTo implements ProgrammFilesWriter {

    @Override public boolean writeFile(List<?> toWriteList) {
        return false;
    }


    @Override public boolean writeFile(Stream<?> toWriteStream) {
        return false;
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


    @Override public String error(String fileName , Exception e) {
        boolean isWritten = new CountSizeOfWorkDir(fileName).writeFile(e);
        return fileName + " is " + isWritten;
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