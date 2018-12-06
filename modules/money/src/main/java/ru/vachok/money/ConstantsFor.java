package ru.vachok.money;


import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.money.config.AppComponents;
import ru.vachok.money.services.TimeChecker;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Function;


/**
 @since 20.08.2018 (11:31) */
@SuppressWarnings ("NonFinalFieldInEnum")
public enum ConstantsFor {
    ;


    public static final int ONE_YEAR = 365;

    public static final String URL_AS_STRING = "https://cbr.ru/currency_base/daily/";

    /**
     Кол-во байт в мегабайте
     */
    public static final int MEGABYTE = 1048576;

    /**
     Название приложения, для поиска properties
     */
    public static final String APP_NAME = ConstantsFor.class.getPackage().getName().replaceAll("\\Q.\\E", "_") + "-";

    public static final long START_STAMP = System.currentTimeMillis();

    public static final int YEAR_BIRTH = 1984;

    public static final float FILES_TO_ENC_BLOCK = 111.0f;

    public static final String DB_PREFIX = "u0466446_";


    /*Fields*/
    private static final Logger LOGGER = AppComponents.getLogger();

    public static final String AT_NAME_TITLE = "title";

    public static final int MONTH_BIRTH = 1;

    public static final int DAY_OF_B_MONTH = 7;

    public static final double NRIGA = 32.2;

    public static final double A107 = 21.6;

    public static final int INITIAL_DELAY = 30;

    public static final int DELAY = 300;

    public static final float ONE_HOUR = 60f;

    public static final float ONE_DAY = 24f;

    public static final int LISTEN_PORT = 9991;

    public static final int USER_EXIT = 222;

    public static final String AT_NAME_FOOTER = "footer";

    public static final String AT_NAME_RESULT = "result";

    public static String getAtomTime() {
        TimeChecker timeChecker = new TimeChecker();
        TimeInfo call = null;
        try{
            call = timeChecker.call();
            call.computeDetails();
            return new java.util.Date(call.getReturnTime()).toString();
        }
        catch(Exception e){
            LOGGER.error(e.getMessage(), e);
        }
        throw new NoSuchElementException();
    }

    public static BiConsumer<String, String> ok = (className, msg) -> new Thread(() -> {
        MessageToUser emailMe = new ESender("143500@gmail.com");
        emailMe.info(ConstantsFor.APP_NAME, className + " ok", msg);
    }).start();

    public static final InitProperties DB_REG = new DBRegProperties(APP_NAME + ConstantsFor.class.getSimpleName());

    public static final int ROWS_COUNT = Integer.parseInt(DB_REG.getProps()
        .getOrDefault("rows", "300000").toString());
    /**
     Кол-во байт в килобайте
     */
    public static final int KILOBYTE = 1024;

    private static boolean myPC;

    public static Function<DataConnectTo, String> dbSpeedCheck = (x) -> {
        List<Integer> integersA107 = new ArrayList<>();
        List<Integer> integersNriga = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        Connection c = x.getDefaultConnection(ConstantsFor.DB_PREFIX + "liferpg");
        try(PreparedStatement p = c.prepareStatement("select * from speed where Road = 0");
            PreparedStatement p1 = c.prepareStatement("select * from speed where Road = 1");
            ResultSet r = p.executeQuery();
            ResultSet r1 = p1.executeQuery()) {
            while(r.next()){
                integersA107.add(r.getInt("Speed"));
            }
            while(r1.next()){
                integersNriga.add(r1.getInt("Speed"));
            }
        }
        catch(SQLException ignore){
            //
        }
        stringBuilder.append(integersA107.size()).append(" по Бетонке").append("\n");
        stringBuilder.append(integersNriga.size()).append(" по Риге").append("\n");
        return stringBuilder.toString();
    };

    public static void setMyPC(boolean myPC) {
        ConstantsFor.myPC = myPC;
    }

    public static boolean isMyPC() {
        try{
            myPC = InetAddress.getLocalHost().getHostName().equalsIgnoreCase("home");
        }
        catch(UnknownHostException e){
            getLogger().error(ConstantsFor.class.getSimpleName());
        }
        return myPC;
    }

    public static Logger getLogger() {
        return LoggerFactory.getLogger(DB_PREFIX + APP_NAME);
    }

    public static String localPc() {
        try{
            return InetAddress.getLocalHost().getHostName();
        }catch (UnknownHostException e){
            return e.getMessage();
        }
    }
}
