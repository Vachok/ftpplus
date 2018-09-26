package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.networker.componentsrepo.ADComputer;
import ru.vachok.networker.componentsrepo.ADUser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 <h1></h1>

 @since 25.09.2018 (15:10) */
@Service("adsrv")
public class ADSrv implements Runnable {

    /*Fields*/
    private static final Logger LOGGER = LoggerFactory.getLogger(ADSrv.class.getName());

    private ADUser adUser;

    private ADComputer adComputer;

    private Map<ADComputer, ADUser> adComputerADUserMap = new ConcurrentHashMap<>();

    private String[] userS;

    private String[] compS;

    public ADUser getAdUser() {
        return adUser;
    }

    public ADComputer getAdComputer() {
        return adComputer;
    }

    public Map<ADComputer, ADUser> getAdComputerADUserMap() {
        return adComputerADUserMap;
    }

    /*Instances*/
    @Autowired
    public ADSrv(ADUser adUser, ADComputer adComputer) {
        this.adUser = adUser;
        this.adComputer = adComputer;
    }

    @Override
    public void run() {
        streamRead();
    }

    public List<String> adFileReader() {
        List<String> strings = new ArrayList<>();
        File adUsers = new File("allmailbox.txt");
        BufferedReader bufferedReader;
        try (FileReader fileReader = new FileReader(adUsers)) {
            bufferedReader = new BufferedReader(fileReader);
            while (bufferedReader.ready()) {
                strings.add(bufferedReader.readLine());
            }
        } catch (IOException | InputMismatchException e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.info(adUser.toString());
        return strings;
    }

    private void streamRead() {
        String msg;
        try (
            InputStream compInputStream = getClass().getResourceAsStream("/static/texts/computers.txt");
            InputStream usrInputStream = getClass().getResourceAsStream("/static/texts/users.txt")
        ) {
            int i = compInputStream.available();
            msg = "Computers to read " + i + " bytes";
            byte[] compBytes = new byte[i];
            while (compInputStream.available() > 0) {
                i = compInputStream.read(compBytes, 0, i);
            }
            LOGGER.info(msg);
            i = usrInputStream.available();
            msg = "Bytes to read " + i;
            byte[] userBytes = new byte[i];
            while (usrInputStream.available() > 0) {
                i = usrInputStream.read(userBytes, 0, i);
            }
            userS = new String(userBytes, StandardCharsets.UTF_8).split("\n\r");

            compS = new String(compBytes, StandardCharsets.UTF_8).split("\n\r");

        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        adComputerSetter();
        userSetter();
    }

    private List<ADComputer> adComputerSetter() {
        List<ADComputer> adComputers = new ArrayList<>();
        int index = 0;
        for (String s : compS) {
            index++;
            ADComputer adC = new ADComputer();
            String[] sS = s.split("\r\n");
            try {
                for (String ssStr : sS) {
                    if (s.contains("DistinguishedName")) {
                        adC.setDistinguishedName(ssStr.split(": ")[1]);
                    }
                    if (s.contains("DNSHostName")) {
                        adC.setDnsHostName(ssStr.split(": ")[1]);
                    }
                    if (s.contains("Enabled")) {
                        adC.setEnabled(ssStr.split(": ")[1]);
                    }
                    if (s.contains("Name")) {
                        adC.setName(ssStr.split(": ")[1]);
                    }
                    if (s.contains("ObjectClass")) {
                        adC.setObjectClass(ssStr.split(": ")[1]);
                    }
                    if (s.contains("ObjectGUID")) {
                        adC.setObjectGUID(ssStr.split(": ")[1]);
                    }
                    if (s.contains("SamAccountName")) {
                        adC.setSamAccountName(ssStr.split(": ")[1]);
                    }
                    if (s.contains("SID")) {
                        adC.setSID(ssStr.split(": ")[1]);
                    }
                    if (s.contains("UserPrincipalName")) {
                        adC.setUserPrincipalName(ssStr.split(": ")[1]);
                    }
                    adComputers.add(adC);
                    adComputer.getAdComputers().add(adC);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                LOGGER.error(e.getMessage(), e);
            }
            adComputers.add(adC);
        }
        try {
            String msg = index + " index\n" + this.getClass().getMethod("adComputerSetter", String[].class);
            LOGGER.info(msg);
        } catch (NoSuchMethodException ignore) {
            //
        }
        return adComputers;
    }

    private List<ADUser> userSetter() {
        List<ADUser> adUserList = new ArrayList<>();
        int indexUser = 0;
        for (String s : userS) {
            ADUser adU = new ADUser();
            indexUser++;
            String[] sS = s.split("\r\n");
            try {
                for (String ssStr : sS) {
                    if (ssStr.contains("DistinguishedName")) {
                        adU.setDistinguishedName(ssStr.split(": ")[1]);
                    }
                    if (ssStr.contains("Enabled")) {
                        adU.setEnabled(ssStr.split(": ")[1]);
                    }
                    if (ssStr.contains("GivenName")) {
                        adU.setGivenName(ssStr.split(": ")[1]);
                    }
                    if (ssStr.contains("Name")) {
                        adU.setName(ssStr.split(": ")[1]);
                    }
                    if (ssStr.contains("ObjectClass")) {
                        adU.setObjectClass(ssStr.split(": ")[1]);
                    }
                    if (ssStr.contains("ObjectGUID")) {
                        adU.setObjectGUID(ssStr.split(": ")[1]);
                    }
                    if (ssStr.contains("SamAccountName")) {
                        adU.setSamAccountName(ssStr.split(": ")[1]);
                    }
                    if (ssStr.contains("SID")) {
                        adU.setSID(ssStr.split(": ")[1]);
                    }
                    if (ssStr.contains("Surname")) {
                        adU.setSurname(ssStr.split(": ")[1]);
                    }
                    if (ssStr.contains("UserPrincipalName")) {
                        adU.setUserPrincipalName(ssStr.split(": ")[1]);
                    }
                    adUserList.add(adU);
                    adUser.getAdUsers().add(adU);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                LOGGER.error(e.getMessage(), e);
            }
            adUserList.add(adU);
        }
        try {
            String msg = indexUser + " index " + getClass().getMethod("userSetter", String[].class);
            LOGGER.info(msg);
        } catch (NoSuchMethodException ignore) {
            //
        }
        return adUserList;
    }

}
