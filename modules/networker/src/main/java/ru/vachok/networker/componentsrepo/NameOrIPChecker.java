// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.UnknownFormatConversionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 @see ru.vachok.networker.componentsrepo.NameOrIPCheckerTest
 @since 03.12.2018 (16:42) */
public class NameOrIPChecker {
    
    
    /**
     {@link Pattern} локального имени в домене {@link ConstantsFor#DOMAIN_EATMEATRU}
     */
    private static final Pattern PATTERN_NAME = Pattern.compile("^(([apAdDTtNn])(([0-3])|([oOTtPp])))((\\d{2})|(\\d{4}))");
    
    /**
     {@link Pattern} IP-адреса
     */
    private static final Pattern PATTERN_IP = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    
    /**
     {@link MessageLocal}
     */
    private final MessageToUser messageToUser = new MessageLocal(NameOrIPChecker.class.getSimpleName());
    
    /**
     Ввод от юзера
     */
    private String userIn;
    
    /**
     Default Constructor
     <p>
     
     @param userIn ввод из <a href="http://rups00.eatmeat.ru:8880/sshacts">SSHACTS</a>.
     */
    public NameOrIPChecker(@NotNull String userIn) {
        if (userIn.contains(ConstantsFor.STR_EATMEAT)) {
            this.userIn = userIn.split(ConstantsFor.STR_EATMEAT)[0];
        }
        else {
            this.userIn = userIn;
        }
    }
    
    public boolean isLocalAddress() {
        try {
            InetAddress inetAddress = resolveInetAddress();
            return true;
        }
        catch (UnknownFormatConversionException e) {
            messageToUser.error(MessageFormat.format("NameOrIPChecker.isLocalAddress: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            return false;
        }
    }
    
    /**
     Преобразование пользовательского ввода в {@link InetAddress}
     <p>
     Проверяет по: {@link #PATTERN_IP} и {@link #PATTERN_NAME}, что именно прилетело из броузера. <br>
     В зависимости от р-та проверки, резолвит и отдаёт {@link InetAddress}.
     
     @return {@link InetAddress}
     
     @throws UnknownFormatConversionException если не удалось опознать строку-ввод.
     */
    public InetAddress resolveInetAddress() throws UnknownFormatConversionException {
        InetAddress inetAddress = InetAddress.getLoopbackAddress();
        Matcher mName = PATTERN_NAME.matcher(userIn);
        Matcher mIP = PATTERN_IP.matcher(userIn);
        if (mName.matches()) {
            inetAddress = nameMach();
        }
        if (mIP.matches()) {
            inetAddress = ipMach();
        }
        return inetAddress;
    }
    
    private InetAddress nameMach() {
        try {
            return InetAddress.getByName(userIn + ConstantsFor.DOMAIN_EATMEATRU);
        }
        catch (UnknownHostException e) {
            throw new UnknownFormatConversionException("Name not mach: " + userIn);
        }
    }
    
    private @NotNull InetAddress ipMach() {
        byte[] addressBytes;
        try {
            addressBytes = InetAddress.getByName(userIn).getAddress();
            return InetAddress.getByAddress(addressBytes);
        }
        catch (UnknownHostException e) {
            throw new UnknownFormatConversionException("Ip is no local ip. " + userIn);
        }
        
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NameOrIPChecker{");
        sb.append(", userIn='").append(userIn).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    private String resolveName() throws UnknownHostException {
        byte[] addressBytes = InetAddress.getByName(userIn).getAddress();
        InetAddress inetAddress = InetAddress.getByAddress(addressBytes);
        return inetAddress.getHostName();
    }
    
    /**
     Резолвинг имени компа, или допись {@link ConstantsFor#DOMAIN_EATMEATRU}
     <p>
     {@link #resolveName()}
     
     @return имя ПК
     */
    private @NotNull String checkPat() {
        StringBuilder stringBuilder = new StringBuilder();
        Matcher mName = PATTERN_NAME.matcher(userIn);
        Matcher mIP = PATTERN_IP.matcher(userIn);
        if (mName.matches()) {
            stringBuilder.append(userIn).append(ConstantsFor.DOMAIN_EATMEATRU);
        }
        else {
            if (mIP.matches()) {
                try {
                    stringBuilder.append(resolveName());
                }
                catch (UnknownHostException e) {
                    stringBuilder.append(e.getMessage());
                }
            }
        }
        return stringBuilder.toString();
    }
}
