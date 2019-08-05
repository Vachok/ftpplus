// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.fsworks;


import ru.vachok.networker.AbstractNetworkerFactory;
import ru.vachok.networker.abstr.Keeper;


/**
 Class ru.vachok.networker.restapi.fsworks.FilesWorkerFactory
 <p>
 
 @since 19.07.2019 (22:48) */
public abstract class FilesWorkerFactory1 extends AbstractNetworkerFactory implements Keeper {
    
    
    public static UpakFiles getInstance() {
        return new UpakFiles(9);
    }
    
    public abstract UpakFiles getUpakFiles(int compLevel0to9);
}