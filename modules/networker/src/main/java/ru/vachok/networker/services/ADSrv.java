package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Service;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.ADComputer;
import ru.vachok.networker.componentsrepo.ADUser;
import ru.vachok.networker.config.AppCtx;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 <h1></h1>

 @since 25.09.2018 (15:10) */
@Service("adsrv")
public class ADSrv implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ADSrv.class.getName());

    private ADUser adUser;

    public ADUser getAdUser() {
        return adUser;
    }

    public ADComputer getAdComputer() {
        return adComputer;
    }

    private ADComputer adComputer;

    private String[] userS;

    private String[] compS;

    private AutowireCapableBeanFactory autowireCapableBeanFactory = new AppCtx().getAutowireCapableBeanFactory();

    public Map<ADComputer, ADUser> getAdComputerADUserMap() {
        return adComputerADUserMap;
    }

    private Map<ADComputer, ADUser> adComputerADUserMap = new ConcurrentHashMap<>();

    @Autowired
    public ADSrv(ADUser adUser, ADComputer adComputer) {
        this.adUser = adUser;
        this.adComputer = adComputer;
    }

    @Override
    public void run() {
        fileRead();

    }

    private void fileRead() {

        try(FileInputStream resourceAsStream = ( FileInputStream ) getClass().getResourceAsStream("/static/texts/computers.txt");
            BufferedInputStream bufferedInputStream = new BufferedInputStream(resourceAsStream)){
            byte[] bytes = new byte[ConstantsFor.KBYTE * 500];
            while(bufferedInputStream.available() > 0){
                int read = bufferedInputStream.read(bytes, 0, ConstantsFor.KBYTE * 500);
            }
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void streamRead() {
        StringBuilder userBuilder = new StringBuilder();
        StringBuilder pcBuilder = new StringBuilder();
        try (
            InputStream compInputStream = getClass().getResourceAsStream("/static/texts/computers.txt");
            InputStream usrInputStream = getClass().getResourceAsStream("/static/texts/users.txt")) {
            int i = ConstantsFor.KBYTE;
            byte[] compBytes = new byte[i];
            while (compInputStream.available() > 0) {
                i = compInputStream.read(compBytes, 0, i);
                pcBuilder.append(new String(compBytes, StandardCharsets.UTF_8));
            }
            String msg = "Computers read " + i + " bytes";
            LOGGER.info(msg);

            int j = ConstantsFor.KBYTE;
            byte[] userBytes = new byte[j];
            while (usrInputStream.available() > 0) {
                j = usrInputStream.read(userBytes, 0, j);
                userBuilder.append(new String(compBytes, StandardCharsets.UTF_8));
            }
            String msg1 = "Users read " + j + " bytes";
            LOGGER.info(msg1);

            userS = userBuilder.toString().split("\n");
            compS = pcBuilder.toString().split("\n");
            userSetter();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void userSetter() {
        Map<Integer, ADUser> adUsers = adUser.getAdUsers();
        int indexUser = 0;
        for (String s : userS) {
            indexUser++;
            try {
                if (s.contains("DistinguishedName")) adUser.setDistinguishedName(s.split(": ")[1]);
                if (s.contains("Enabled")) adUser.setEnabled(s.split(": ")[1]);
                if (s.contains("GivenName")) adUser.setGivenName(s.split(": ")[1]);
                if (s.contains("Name")) adUser.setName(s.split(": ")[1]);
                if (s.contains("ObjectClass")) adUser.setObjectClass(s.split(": ")[1]);
                if (s.contains("ObjectGUID")) adUser.setObjectGUID(s.split(": ")[1]);
                if (s.contains("SamAccountName")) adUser.setSamAccountName(s.split(": ")[1]);
                if (s.contains("SID")) adUser.setSID(s.split(": ")[1]);
                if (s.contains("Surname")) adUser.setSurname(s.split(": ")[1]);
                if (s.contains("UserPrincipalName")) adUser.setUserPrincipalName(s.split(": ")[1]);
            }
            catch(ArrayIndexOutOfBoundsException ignore){
                //
            }
            adUsers.put(indexUser, adUser);
        }
        String msg = adUsers.size() + " adUsers MAP";
        LOGGER.info(msg);
        adUser.setAdUsers(adUsers);
        Map<Integer, ADComputer> adComputers = adComputer.getAdComputers();
        int index = 0;
        for (String s : compS) {
            try{
                index++;
                if (s.contains("DistinguishedName")) adComputer.setDistinguishedName(s.split(": ")[1]);
                if (s.contains("DNSHostName")) adComputer.setDnsHostName(s.split(": ")[1]);
                if (s.contains("Enabled")) adComputer.setEnabled(s.split(": ")[1]);
                if (s.contains("Name")) adComputer.setName(s.split(": ")[1]);
                if (s.contains("ObjectClass")) adComputer.setObjectClass(s.split(": ")[1]);
                if (s.contains("ObjectGUID")) adComputer.setObjectGUID(s.split(": ")[1]);
                if (s.contains("SamAccountName")) adComputer.setSamAccountName(s.split(": ")[1]);
                if (s.contains("SID")) adComputer.setSID(s.split(": ")[1]);
                if (s.contains("UserPrincipalName")) adComputer.setUserPrincipalName(s.split(": ")[1]);
                adComputers.put(index, adComputer);
                LOGGER.info(adComputer.toString());
            }
            catch(ArrayIndexOutOfBoundsException ignore){
                //
            }
        }
        String msg2 = adComputers.size() + " adcomps size MAP";
        LOGGER.info(msg2);
        adComputer.setAdComputers(adComputers);
        adComputerADUserMap.put(adComputer, adUser);
    }
}
