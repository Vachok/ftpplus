// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.fsworks;


import ru.vachok.networker.AbstractNetworkerFactory;


/**
 Class ru.vachok.networker.restapi.fsworks.FilesWorkerFactory
 <p>
 
 @since 19.07.2019 (22:48) */
public abstract class FilesWorkerFactory extends AbstractNetworkerFactory {
    
    
    public static UpakFiles getInstance() {
        return new UpakFiles(9);
    }
    
    public abstract UpakFiles getUpakFiles(int compLevel0to9);
}