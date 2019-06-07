// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol;



import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UnknownFormatConversionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 Проверяет пользовательский ввод на соотв. паттерну IP или имя ПК

 @since 03.12.2018 (16:42) */
public class NameOrIPChecker {


    /**
     {@link Pattern} локального имени в домене {@link ConstantsFor#DOMAIN_EATMEATRU}
     */
    private static final Pattern PATTERN_NAME = Pattern.compile("^(([aAdDTtNn])(([0-3])|([oOTtPp])))((\\d{2})|(\\d{4}))");

    /**
     {@link Pattern} IP-адреса
     */
    private static final Pattern PATTERN_IP = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    /**
     Ввод от юзера
     */
    private String userIn;

    /**
     * {@link MessageLocal}
     */
    private final MessageToUser messageToUser = new MessageLocal(NameOrIPChecker.class.getSimpleName());

    /**
     Default Constructor
     <p>

     @param userIn ввод из <a href="http://rups00.eatmeat.ru:8880/sshacts">SSHACTS</a>.
     */
    public NameOrIPChecker(String userIn) {
        this.userIn = userIn;
    }

    /**
     Резолвинг имени компа, или допись {@link ConstantsFor#DOMAIN_EATMEATRU}
     <p>
     {@link #resolveName(String)}
 
     @return имя ПК
     @param userIn пользовательский ввод
     */
    public String checkPat(String userIn) {
        StringBuilder stringBuilder = new StringBuilder();
        this.userIn = userIn;
        Matcher mName = PATTERN_NAME.matcher(userIn);
        Matcher mIP = PATTERN_IP.matcher(userIn);
        if(mName.matches()){
            stringBuilder.append(userIn).append(ConstantsFor.DOMAIN_EATMEATRU);
        }
        else{
            if(mIP.matches()){
                try{
                    stringBuilder.append(resolveName(userIn));
                }
                catch(UnknownHostException e){
                    stringBuilder.append(e.getMessage());
                }
            }
        }
        return stringBuilder.toString();
    }

    /**
     Преобразование пользовательского ввода в {@link InetAddress}

     @param userIn позьзовательский ввод
     @return {@link InetAddress#getHostName()}
     @throws UnknownHostException если недоступен ПК
     */
    private String resolveName(String userIn) throws UnknownHostException {
        byte[] addressBytes = InetAddress.getByName(userIn).getAddress();
        InetAddress inetAddress = InetAddress.getByAddress(addressBytes);
        return inetAddress.getHostName();
    }

    /**
     Преобразование пользовательского ввода в {@link InetAddress}
     <p>
     Проверяет по: {@link #PATTERN_IP} и {@link #PATTERN_NAME}, что именно прилетело из броузера. <br>
     В зависимости от р-та проверки, резолвит и отдаёт {@link InetAddress}.

     @return {@link InetAddress}
     @throws UnknownFormatConversionException если не удалось опознать строку-ввод.
     */
    public InetAddress resolveIP() throws UnknownFormatConversionException {
        InetAddress inetAddress;
        Matcher mName = PATTERN_NAME.matcher(userIn);
        Matcher mIP = PATTERN_IP.matcher(userIn);
        try{
            if(mIP.matches()){
                byte[] addressBytes = InetAddress.getByName(userIn).getAddress();
                inetAddress = InetAddress.getByAddress(addressBytes);
            }
            else{
                if(mName.matches()){
                    userIn += ConstantsFor.DOMAIN_EATMEATRU;
                    inetAddress = InetAddress.getByName(userIn);
                }
                else{
                    //noinspection UnusedAssignment
                    inetAddress = InetAddress.getLoopbackAddress();
                    throw new UnknownFormatConversionException("Can't convert user input to Inet Address :(");
                }
            }
        }
        catch(UnknownHostException e){
            messageToUser.errorAlert("NameOrIPChecker", "resolveIP", e.getMessage());
            FileSystemWorker.error("NameOrIPChecker.resolveIP", e);
            throw new IllegalArgumentException(userIn + " if shit!");
        }
        return inetAddress;
    }
}
