package ru.vachok.networker.services;



/**
 @since 09.04.2019 (0:54) */
public interface SSHService {

    String execCommand(String connectToSrv , String commandToExec);

    void checkPem();
}
