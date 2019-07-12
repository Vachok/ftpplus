package ru.vachok.networker.net.scanner;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.services.MessageLocal;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;


/**
 @see ru.vachok.networker.net.scanner.CheckerIpTest
 @since 12.07.2019 (14:36) */
class CheckerIp {
    
    
    private final NetListKeeper netListKeeper = AppComponents.netKeeper();
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private PrintStream printStream;
    
    private Map<String, String> netListKeeperOffLines = netListKeeper.editOffLines();
    
    private ConcurrentMap<String, String> onLinesResolve = netListKeeper.getOnLinesResolve();
    
    private String inetAddrStr;
    
    CheckerIp(String inetAddrStr, PrintStream printStream) {
        this.printStream = printStream;
        this.inetAddrStr = inetAddrStr;
    }
    
    public boolean checkIP() {
        
        boolean xReachable = false;
        byte[] addressBytes = new byte[0];
        
        try {
            addressBytes = InetAddress.getByName(inetAddrStr.split(" ")[0]).getAddress();
        }
        catch (UnknownHostException | NullPointerException e) {
            addressBytes = InetAddress.getLoopbackAddress().getAddress();
        }
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByAddress(addressBytes);
        }
        catch (UnknownHostException e) {
            messageToUser.error(MessageFormat.format("InetAddress {1} says: {0}", e.getMessage(), inetAddress));
        }
        try {
            xReachable = inetAddress.isReachable(ConstantsFor.TIMEOUT_650 / 2);
        }
        catch (IOException | NullPointerException e) {
            messageToUser.error(MessageFormat.format("{1} is {0} ({2})", e.getMessage(), inetAddress, xReachable));
        }
        
        if (inetAddress.equals(InetAddress.getLoopbackAddress())) {
            xReachable = false;
        }
        if (!xReachable) {
            xNotReachable();
        }
        else {
            xIsReachable();
        }
        netListKeeper.setOffLines(netListKeeperOffLines);
        return xReachable;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CheckerIp{");
        sb.append("Offline pc is <font color=\"red\"><b>").append(netListKeeper.editOffLines().size()).append(":</b></font><br>");
        sb.append("Online  pc is<font color=\"#00ff69\"> <b>").append(onLinesResolve.size()).append(":</b><br>");
        sb.append(new TForms().fromArray(onLinesResolve, true)).append("</font><br>");
        sb.append('}');
        return sb.toString();
    }
    
    private void xIsReachable() {
        printStream.println(inetAddrStr + " <font color=\"green\">online</font>.");
        
        String ifAbsent = onLinesResolve.putIfAbsent(inetAddrStr, LocalTime.now().toString());
        String removeOffline = netListKeeperOffLines.remove(inetAddrStr);
        if (!(removeOffline == null)) {
            messageToUser.info(inetAddrStr, "online", MessageFormat.format("{0} gets online!", removeOffline));
        }
    }
    
    private void xNotReachable() {
        printStream.println(new StringBuilder().append(inetAddrStr).append(" <font color=\"red\">offline</font>."));
        String removeOnline = onLinesResolve.remove(inetAddrStr);
        
        if (!(removeOnline == null)) {
            netListKeeperOffLines.putIfAbsent(inetAddrStr, new Date().toString());
            messageToUser.info(MessageFormat.format("Map<String, String> offLines size = {0} items", netListKeeperOffLines.size()));
        }
        else {
            messageToUser.info(MessageFormat.format("String removeOnline is NULL! Size onLinesResolve Map is {0}. Tried del: {1}", onLinesResolve.size(), inetAddrStr));
        }
    }
}
