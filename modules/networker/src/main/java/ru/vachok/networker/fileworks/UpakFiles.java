// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.fileworks;


import ru.vachok.networker.componentsrepo.InvokeEmptyMethodException;

import java.io.File;


/**
 Class ru.vachok.networker.fileworks.UpakFiles
 <p>
 
 @see ru.vachok.networker.fileworks.UpakFilesTest
 @since 06.07.2019 (7:32) */
public class UpakFiles extends FileSystemWorker {
    
    
    @Override public String packFile(File forUpakFile) {
        throw new InvokeEmptyMethodException(getClass().getTypeName());
    }
}