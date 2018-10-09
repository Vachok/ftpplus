package ru.vachok.networker.ad;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
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

    private String userInputRaw;

    public String getUserInputRaw() {
        return userInputRaw;
    }

    public void setUserInputRaw(String userInputRaw) {
        this.userInputRaw = userInputRaw;
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
        Boolean call = new DataBaseADUsers().call();
        LOGGER.warn(call + " database ADUsers");
        streamRead();
    }

    String getDetails(String queryString) throws IOException {
        if (InetAddress.getByName(queryString + ".eatmeat.ru").isReachable(500)) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<p>   Более подробно про ПК:<br>");
            File[] files = new File("\\\\" + queryString + ".eatmeat.ru\\c$\\Users\\").listFiles();
            for (File file : files) {
                stringBuilder
                    .append(file.getName())
                    .append(" ")
                    .append(new Date(file.lastModified()).toString())
                    .append("<br>");
            }
            return stringBuilder.toString();
        } else {
            return "PC Offline";
        }
    }

    private List<String> adFileReader() {
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

    /**
     <b>Сетает пользователей и ПК из файлов.</b><br>
     Текстовые файлы - результаты выполенения get-aduser , get-adcomputer. Сервер srv-ad. PowerShell.<br>
     <b>Требуется перевод в UTF-8</b>
     */
    private void streamRead() {
        String msg;
        try (
            InputStream compInputStream = getClass().getResourceAsStream("/static/texts/computers.txt")) {
            int i = compInputStream.available();
            msg = "Computers to read " + i + " bytes";
            byte[] compBytes = new byte[i];
            while (compInputStream.available() > 0) {
                i = compInputStream.read(compBytes, 0, i);
            }
            LOGGER.info(msg);
            msg = "Bytes to read " + i;
            LOGGER.info(msg);
            this.compS = new String(compBytes, StandardCharsets.UTF_8).split("\n\r");
            msg = userS.length + " users and " + compS.length + " pc read.";
            LOGGER.warn(msg);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        try {
            adComputerSetter();
            userSetter();
        } catch (ArrayIndexOutOfBoundsException e) {
            LOGGER.error(e.getMessage(), e);
        }

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
                    if (ssStr.contains("DistinguishedName")) {
                        adC.setDistinguishedName(ssStr.split(": ")[1]);
                    }
                    if (ssStr.contains("DNSHostName")) {
                        adC.setDnsHostName(ssStr.split(": ")[1]);
                    }
                    if (ssStr.contains("Enabled")) {
                        adC.setEnabled(ssStr.split(": ")[1]);
                    }
                    if (ssStr.contains("^Name")) {
                        adC.setName(ssStr.split(": ")[1]);
                    }
                    if (ssStr.contains("ObjectClass")) {
                        adC.setObjectClass(ssStr.split(": ")[1]);
                    }
                    if (ssStr.contains("ObjectGUID")) {
                        adC.setObjectGUID(ssStr.split(": ")[1]);
                    }
                    if (ssStr.contains("SamAccountName")) {
                        adC.setSamAccountName(ssStr.split(": ")[1]);
                    }
                    if (ssStr.contains("SID")) {
                        adC.setSID(ssStr.split(": ")[1]);
                    }
                    if (ssStr.contains("UserPrincipalName")) {
                        adC.setUserPrincipalName(ssStr.split(": ")[1]);
                    }
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
        adComputer.setAdComputers(adComputers);
        return adComputers;
    }

    List<ADUser> userSetter() {
        try (InputStream usrInputStream = getClass().getResourceAsStream("/static/texts/users.txt")) {
            int i = usrInputStream.available();
            byte[] userBytes = new byte[i];
            while (usrInputStream.available() > 0) {
                i = usrInputStream.read(userBytes, 0, i);
            }
            this.userS = new String(userBytes, StandardCharsets.UTF_8).split("\n\r");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        List<ADUser> adUserList = new ArrayList<>();
        int indexUser = 0;
        for (String s : this.userS) {
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

                }
            } catch (ArrayIndexOutOfBoundsException ignore) {
                //
            }
            adUserList.add(adU);
        }
        try {
            String msg = indexUser + " index " + getClass().getMethod("userSetter", String[].class);
            LOGGER.info(msg);
        } catch (NoSuchMethodException ignore) {
            //
        }
        adUser.setAdUsers(adUserList);
        psComm(adUser.getAdUsers());
        return adUserList;
    }

    private void psComm(List<ADUser> adUsers) {
        PhotoConverterSRV photoConverterSRV = new PhotoConverterSRV();
        photoConverterSRV.psCommands();
    }

}
