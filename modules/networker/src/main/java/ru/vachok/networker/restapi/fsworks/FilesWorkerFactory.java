// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.fsworks;


import ru.vachok.networker.abstr.Keeper;
import ru.vachok.networker.accesscontrol.common.usermanagement.UserACLManagerImpl;


/**
 Class ru.vachok.networker.restapi.fsworks.FilesWorkerFactory
 <p>
 
 @since 19.07.2019 (22:48) */
public interface FilesWorkerFactory extends Keeper {
    
    
    UserACLManagerImpl getFileServerACLManager();
}