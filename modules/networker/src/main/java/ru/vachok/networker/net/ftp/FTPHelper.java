// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.ftp;


import java.nio.file.AccessDeniedException;
import java.util.Queue;


/**
 Interface ru.vachok.networker.net.ftp.FTPHelper
 <p>
 
 @since 01.06.2019 (4:18) */
public interface FTPHelper {
    
    
    void connectTo() throws AccessDeniedException;
    
    Queue<String> getContentsQueue();
}