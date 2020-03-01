package ru.vachok.networker.net.scanner;


import ru.vachok.networker.restapi.message.MessageToUser;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


/**
 Class ru.vachok.networker.net.scanner.MACResolver
 <p>

 @since 23.02.2020 (22:17) */
public class MACResolver {


    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, MACResolver.class.getSimpleName());

    public void getMac() {
        try {
            Enumeration<NetworkInterface> netFace = NetworkInterface.getNetworkInterfaces();
            while (netFace.hasMoreElements()) {
                NetworkInterface anInterface = netFace.nextElement();
                byte[] address = anInterface.getHardwareAddress();
                if (address != null) {
                    System.out.println("netFace.nextElement().getInetAddresses() = " + anInterface.getName());
                }
            }
        }
        catch (SocketException e) {
            messageToUser.warn(MACResolver.class.getSimpleName(), e.getMessage(), " see line: 29 ***");
        }
    }
}