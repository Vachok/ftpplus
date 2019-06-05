// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.ftp;


import java.net.ConnectException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.Scanner;


/**
 Interface ru.vachok.networker.net.ftp.FTPHelper
 <p>
 
 @since 01.06.2019 (4:18) */
public interface LibsHelp {
    
    
    String uploadLibs() throws AccessDeniedException, ConnectException;
    
    Queue<String> getContentsQueue();
    
    default String getVersion() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyw");
        return "8.0." + simpleDateFormat.format(new Date());
    }
    
    default String getHashString(String cipherName) {
        String retStr = "Please, enter a pass, and I'll gives you a hash:\n";
        try (Scanner scanner = new Scanner("initpass")) {
            byte[] enterPassAsBytes = scanner.nextLine().getBytes();
            MessageDigest cipDigest = MessageDigest.getInstance(cipherName);
            byte[] digestMD5 = cipDigest.digest(enterPassAsBytes);
            retStr = new String(digestMD5);
        }
        catch (NoSuchAlgorithmException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".getHashString");
            System.out.println("You need to put file, with name \"pass\", to program main directory: " + Paths.get(".").toAbsolutePath().normalize() + " , for init you new pass");
        }
        return retStr;
    }
    
}