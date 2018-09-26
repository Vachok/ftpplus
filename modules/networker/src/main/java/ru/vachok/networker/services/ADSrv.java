package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Service;
import ru.vachok.networker.componentsrepo.ADComputer;
import ru.vachok.networker.componentsrepo.ADUser;
import ru.vachok.networker.config.AppCtx;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 <h1></h1>

 @since 25.09.2018 (15:10) */
@Service ("adsrv")
public class ADSrv implements Runnable {

    /*Fields*/
    private static final Logger LOGGER = LoggerFactory.getLogger(ADSrv.class.getName());

    private ADUser adUser;

    private ADComputer adComputer;

    private String[] userS;

    private String[] compS;

    private AutowireCapableBeanFactory autowireCapableBeanFactory = new AppCtx().getAutowireCapableBeanFactory();

    private Map<ADComputer, ADUser> adComputerADUserMap = new ConcurrentHashMap<>();

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

    private void streamRead() {
        String msg;
        try(
            InputStream compInputStream = getClass().getResourceAsStream("/static/texts/computers.txt");
            InputStream usrInputStream = getClass().getResourceAsStream("/static/texts/users.txt")
        ){
            int i = compInputStream.available();
            msg = "Computers to read " + i + " bytes";
            byte[] compBytes = new byte[i];
            while(compInputStream.available() > 0){
                i = compInputStream.read(compBytes, 0, i);
            }
            LOGGER.info(msg);

            i = usrInputStream.available();
            msg = "Bytes to read " + i;
            byte[] userBytes = new byte[i];
            while(usrInputStream.available() > 0){
                i = usrInputStream.read(userBytes, 0, i);
            }
            LOGGER.info(msg);

            userS = new String(userBytes, StandardCharsets.UTF_8).split("\n\r");
            compS = new String(compBytes, StandardCharsets.UTF_8).split("\n\r");

            List<ADUser> adUserList = userSetter();

            adUser.setAdUsers(adUserList);
            List<ADComputer> adComputers = adComputerSetter();
            final ADUser adUser = new ADUser();
            final ADComputer adComputer = new ADComputer();
            this.adComputer = adComputer;
            this.adUser = adUser;
            adComputer.setAdComputers(adComputers);
            adUser.setAdUsers(adUserList);
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
        msg = adComputerADUserMap.size() + " adComputerADUserMap size";
        LOGGER.info(msg);
    }

    private List<ADUser> userSetter() {
        List<ADUser> adUserList = new ArrayList<>();
        int indexUser = 0;
        for(String s : userS){
            this.adUser = new ADUser();
            indexUser++;
            String[] sS = s.split("\r\n");
            try{
                for(String ssStr : sS){
                    if(ssStr.contains("DistinguishedName")){
                        this.adUser.setDistinguishedName(ssStr.split(": ")[1]);
                    }
                    if(ssStr.contains("Enabled")){
                        this.adUser.setEnabled(ssStr.split(": ")[1]);
                    }
                    if(ssStr.contains("GivenName")){
                        this.adUser.setGivenName(ssStr.split(": ")[1]);
                    }
                    if(ssStr.contains("Name")){
                        this.adUser.setName(ssStr.split(": ")[1]);
                    }
                    if(ssStr.contains("ObjectClass")){
                        this.adUser.setObjectClass(ssStr.split(": ")[1]);
                    }
                    if(ssStr.contains("ObjectGUID")){
                        this.adUser.setObjectGUID(ssStr.split(": ")[1]);
                    }
                    if(ssStr.contains("SamAccountName")){
                        this.adUser.setSamAccountName(ssStr.split(": ")[1]);
                    }
                    if(ssStr.contains("SID")){
                        this.adUser.setSID(ssStr.split(": ")[1]);
                    }
                    if(ssStr.contains("Surname")){
                        this.adUser.setSurname(ssStr.split(": ")[1]);
                    }
                    if(ssStr.contains("UserPrincipalName")){
                        this.adUser.setUserPrincipalName(ssStr.split(": ")[1]);
                    }
                }
            }
            catch(ArrayIndexOutOfBoundsException e){
                LOGGER.error(e.getMessage(), e);
            }
            adUserList.add(this.adUser);
            LOGGER.info("Adding user: " + indexUser + ") " + adUser.getName());
        }
        return adUserList;
    }

    private List<ADComputer> adComputerSetter() {
        List<ADComputer> adComputers = new ArrayList<>();
        int index = 0;
        for(String s : compS){
            index++;
            this.adComputer = new ADComputer();
            String[] sS = s.split("\r\n");
            try{
                for(String ssStr : sS){
                    if(s.contains("DistinguishedName")){
                        this.adComputer.setDistinguishedName(ssStr.split(": ")[1]);
                    }
                    if(s.contains("DNSHostName")){
                        this.adComputer.setDnsHostName(ssStr.split(": ")[1]);
                    }
                    if(s.contains("Enabled")){
                        this.adComputer.setEnabled(ssStr.split(": ")[1]);
                    }
                    if(s.contains("Name")){
                        this.adComputer.setName(ssStr.split(": ")[1]);
                    }
                    if(s.contains("ObjectClass")){
                        this.adComputer.setObjectClass(ssStr.split(": ")[1]);
                    }
                    if(s.contains("ObjectGUID")){
                        this.adComputer.setObjectGUID(ssStr.split(": ")[1]);
                    }
                    if(s.contains("SamAccountName")){
                        this.adComputer.setSamAccountName(ssStr.split(": ")[1]);
                    }
                    if(s.contains("SID")){
                        this.adComputer.setSID(ssStr.split(": ")[1]);
                    }
                    if(s.contains("UserPrincipalName")){
                        this.adComputer.setUserPrincipalName(ssStr.split(": ")[1]);
                    }
                }
            }
            catch(ArrayIndexOutOfBoundsException e){
                LOGGER.error(e.getMessage(), e);
            }
            adComputers.add(this.adComputer);
            LOGGER.info("Adding pc: " + index + ") " + adComputer.getName());
        }
        return adComputers;
    }
}
