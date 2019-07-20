// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.fsworks;


import ru.vachok.networker.AbstractNetworkerFactory;

import java.text.MessageFormat;


/**
 Class ru.vachok.networker.restapi.fsworks.FilesWorkerFactory
 <p>
 
 @since 19.07.2019 (22:48) */
public class FilesWorkerFactory extends AbstractNetworkerFactory implements FilesHelper {
    
    
    @Override
    public String getStatistics() {
        return MessageFormat.format("{0} cpu\n{1} memory\n{2} runtime", getCPU(), getMemory(), getRuntime());
    }
}