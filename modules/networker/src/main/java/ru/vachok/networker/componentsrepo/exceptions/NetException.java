package ru.vachok.networker.componentsrepo.exceptions;


import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.IOException;
import java.net.InetAddress;
import java.text.MessageFormat;


/**
 Class ru.vachok.networker.componentsrepo.exceptions.NetException
 <p>

 @since 23.04.2020 (17:10) */
public class NetException extends Throwable {


    private final InetAddress ip;

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, NetException.class.getSimpleName());

    private String message;

    public NetException(String message, InetAddress ip) {
        super(message);
        this.message = message;
        this.ip = ip;
    }

    @Override
    public String getMessage() {
        try {
            message = MessageFormat.format("{0} {1}: {2} with timeout 500 ms", message, ip.toString(), ip.isReachable(500));
        }
        catch (IOException e) {
            messageToUser.warn(NetException.class.getSimpleName(), e.getMessage(), " see line: 33 ***");
        }
        return MessageFormat.format("{0}\n{1}", super.getMessage(), message);
    }
}