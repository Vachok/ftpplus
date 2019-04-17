package ru.vachok.networker.accesscontrol;


import com.jcraft.jsch.*;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.stats.connector.SSHWorker;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 @since 17.04.2019 (11:30) */
public class AccessListsCheckUniq implements SSHWorker, Runnable {
    
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    @Override public void run() {
        messageToUser.info(getClass().getSimpleName() + ".run", "connectTo()", " = " + connectTo());
    }
    
    @Override public String connectTo() {
        JSch jSch = new JSch();
        Map<String, String> sshMap = new HashMap<>();
        try {
            jSch.addIdentity(getKey());
        }
        catch (JSchException e) {
            e.printStackTrace();
        }
        try {
            Session itDept = jSch.getSession("ITDept", "srv-nat.eatmeat.ru");
            itDept.setConfig(new DBRegProperties("general-jsch").getProps());
            itDept.setInputStream(System.in);
            itDept.connect();
            if (itDept.isConnected()) {
                sshMap = getSSHMap(itDept, "sudo ls /etc/pf/");
            }
        }
        catch (JSchException e) {
            e.printStackTrace();
        }
        return new TForms().fromArray(sshMap, true);
    }
    
    private Map<String, String> getSSHMap(Session dept, String commSSH) {
        Map<String, String> retMap = new HashMap<>();
        List<File> fromPFFiles = new ArrayList<>();
        try {
            Channel openChannel = dept.openChannel("exec");
            List<String> lsEtcPf = new ArrayList<>();
            File toFile = Files.createTempFile("list", ".ssh").toFile();
            if (makeSSHSessionFile(openChannel, commSSH, toFile)) {
                lsEtcPf = parseListFiles(toFile);
                lsEtcPf.stream().forEach(x->{
                    File fromPF = new File(x);
                    try {
                        if (makeSSHSessionFile(openChannel, commSSH, fromPF)) {
                            fromPFFiles.add(fromPF);
                        }
                    }
                    catch (IOException | JSchException e) {
                        messageToUser.error(e.getMessage());
                    }
                });
            }
            
        }
        catch (JSchException | IOException eJ) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".getSSHMap", eJ));
        }
        fromPFFiles.stream().forEach(x->retMap.putIfAbsent(x.getAbsolutePath(), FileSystemWorker.readFile(x.getAbsolutePath())));
        return retMap;
    }
    
    private boolean makeSSHSessionFile(Channel openChannel, String commSSH, File toFile) throws IOException, JSchException {
        
        ((ChannelExec) openChannel).setCommand(commSSH);
        openChannel.connect();
        InputStream channelInputStream = openChannel.getInputStream();
        OutputStream outputStream = new FileOutputStream(toFile);
        byte[] bBuf = new byte[ConstantsFor.KBYTE];
        while (true) {
            int read = channelInputStream.read(bBuf, 0, bBuf.length);
            if (read <= 0) {
                break;
            }
            outputStream.write(bBuf);
        }
        toFile.deleteOnExit();
        return toFile.exists() && toFile.canRead();
    }
    
    private List<String> parseListFiles(File file) {
        return FileSystemWorker.readFileToList(file.getAbsolutePath());
    }
}
