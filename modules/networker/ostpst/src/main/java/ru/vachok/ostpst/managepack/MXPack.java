package ru.vachok.ostpst.managepack;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;

import javax.management.*;
import javax.management.remote.rmi.RMIConnection;
import javax.management.remote.rmi.RMIJRMPServerImpl;
import javax.management.remote.rmi.RMIServer;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;
import java.util.concurrent.ConcurrentHashMap;


/**
 @since 07.05.2019 (10:52) */
public class MXPack implements MXManager {
    
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    public void managerGet() {
    
        try (RMIConnection rmiConnection = getRmiServer().newClient("")) {
            MBeanInfo beanInfo = rmiConnection.getMBeanInfo(ObjectName.WILDCARD, null);
            String name = beanInfo.getClassName();
            messageToUser.info(getClass().getSimpleName() + ".managerGet", "name", " = " + name);
            rmiConnection.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (IntrospectionException e) {
            e.printStackTrace();
        }
        catch (InstanceNotFoundException e) {
            e.printStackTrace();
        }
        catch (ReflectionException e) {
            e.printStackTrace();
        }
    }
    
    @Override public MXPack getMXPack() {
        return new MXPack();
    }
    
    private RMIServer getRmiServer() throws IOException, IntrospectionException, InstanceNotFoundException, ReflectionException {
        RMIServer rmiServer = new RMIJRMPServerImpl(8811, null, null, new ConcurrentHashMap<>());
        try (ServerSocket serverSocket = RMISocketFactory.getDefaultSocketFactory().createServerSocket(8811);
             Socket accept = serverSocket.accept()
        ) {
            while (accept.isConnected()) {
                PrintStream printStream = new PrintStream(accept.getOutputStream());
                System.setOut(printStream);
            }
        }
        return rmiServer;
    }
}
