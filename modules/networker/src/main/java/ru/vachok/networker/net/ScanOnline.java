package ru.vachok.networker.net;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.systray.MessageToTray;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


/**
 Сканирование только тех, что он-лайн
 <p>

 @see DiapazonedScan
 @since 26.01.2019 (11:18) */
public class ScanOnline implements Runnable {

    private MessageToUser messageToUser = new MessageToTray((ActionEvent e) -> {
        try{
            Desktop.getDesktop().browse(URI.create("http://localhost:8880/showalldev?needsopen"));
        }
        catch(IOException ignore){
            //
        }
    });


    @Override
    public void run() {
        messageToUser.infoNoTitles("ScanOnline.run");
        try{
            List<InetAddress> onList = onlinesAddressesList();
            runPing(onList);
        }
        catch(IOException e){
            messageToUser = new MessageCons();
            messageToUser.errorAlert(getClass().getSimpleName(), e.getMessage(), new TForms().fromArray(e, false));
        }
    }

    private List<InetAddress> onlinesAddressesList() throws IOException {
        messageToUser = new MessageCons();
        List<InetAddress> onlineAddresses = new ArrayList<>();
        List<String> fileAsList = NetScanFileWorker.getI().getListOfOnlineDev();
        fileAsList.forEach(x -> {
            try{
                String[] sS = x.split(" ");
                byte[] inetBytesAddr = InetAddress.getByName(sS[1]).getAddress();
                onlineAddresses.add(InetAddress.getByAddress(inetBytesAddr));
            }
            catch(ArrayIndexOutOfBoundsException | UnknownHostException ignore){
                //
            }
        });
        return onlineAddresses;
    }

    private void runPing(List<InetAddress> onList) {
        onList.forEach(x -> {
            try{
                messageToUser = new MessageCons();
                messageToUser.info(
                    getClass().getSimpleName(),
                    x.toString(),
                    "is online: " + x.isReachable(250));
            }
            catch(IOException e){
                messageToUser = new MessageToTray();
                FileSystemWorker.recFile(
                    getClass().getSimpleName() + ConstantsFor.LOG,
                    e.getMessage() + "\n" + new TForms().fromArray(e, false));
                messageToUser.errorAlert(getClass().getSimpleName(), "runPing", e.getMessage());
            }
        });
        messageToUser.info(
            getClass().getSimpleName(),
            ConstantsFor.getUpTime(),
            onList.size() + " online. Scanned: " + ConstantsFor.ALL_DEVICES.size() + "/" + ConstantsFor.IPS_IN_VELKOM_VLAN);
    }
}