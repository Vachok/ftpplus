package ru.vachok.networker.net.ssh;


import ru.vachok.networker.data.enums.ConstantsFor;

import java.io.IOException;
import java.net.InetAddress;
import java.util.StringJoiner;


/**
 @since 21.03.2020 (12:34) */
public class VpnHelper extends SshActs {


    private static final String GET_STATUS_COMMAND = "cat openvpn-status && exit";

    public String getStatus() {
        String result;
        try {
            InetAddress byName = InetAddress.getByName(ConstantsFor.SRV_VPN);
            if (byName.isReachable(200)) {
                result = execSSHCommand(byName.getHostAddress(), GET_STATUS_COMMAND);
            }
            else {
                result = byName + " is not Reachable".toUpperCase();
            }
        }
        catch (IOException e) {
            result = e.getMessage();
        }
        if (result.isEmpty() || !result.contains("OpenVPN CLIENT LIST")) {
            result = result + "\n" + whatSrvNeed() + " openvpn-status: \n" + execSSHCommand(GET_STATUS_COMMAND);
        }
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(",\n", VpnHelper.class.getSimpleName() + "[\n", "\n]")
            .toString();
    }
}