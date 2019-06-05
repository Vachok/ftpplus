// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.ftp;


import java.net.ConnectException;
import java.nio.file.AccessDeniedException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;


/**
 Interface ru.vachok.networker.net.ftp.FTPHelper
 <p>
 
 @since 01.06.2019 (4:18) */
public interface LibsHelp {
    
    
    String uploadLibs() throws AccessDeniedException, ConnectException;
    
    Queue<String> getContentsQueue();
    
    default String getVersion(String name) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("w");
        return name.split("-")[0] + "-8.0.19" + simpleDateFormat.format(new Date());
    }
    
}