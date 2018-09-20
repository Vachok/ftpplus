package ru.vachok.money;


import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.money.config.AppComponents;
import ru.vachok.money.config.AppCtx;
import ru.vachok.money.services.DBMessage;
import ru.vachok.mysqlandprops.DataConnectTo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;


/**
 @since 20.08.2018 (11:31) */
@SuppressWarnings ("NonFinalFieldInEnum")
public enum ConstantsFor {
    ;

    /*Fields*/

    /**
     Кол-во байт в мегабайте
     */
    public static final int MEGABYTE = 1024 * 1024;

    /**
     Название приложения, для поиска properties
     */
    public static final String APP_NAME = ConstantsFor.class.getPackage().getName().replaceAll("\\Q.\\E", "_") + "-";

    public static final long START_STAMP = System.currentTimeMillis();

    public static BiConsumer<String, String> toDBAndMail = (x, y) -> {
        MessageToUser[] messagesToUser = {new ESender("143500@gmail.com"), new DBMessage()};
        for(MessageToUser messageToUser : messagesToUser){
            messageToUser.info(new Date() + " " + ConstantsFor.APP_NAME + " | ", x, y);
        }
    };

    public static final int YEAR_BIRTH = 1984;

    public static final float FILES_TO_ENC_BLOCK = 111.0f;

    public static final String DB_PREFIX = "u0466446_";

    public static final int MONTH_BIRTH = 1;

    public static final int DAY_OF_B_MONTH = 7;

    public static final double NRIGA = 32.2;

    public static final double A107 = 21.6;

    public static final int INITIAL_DELAY = 30;

    public static final int DELAY = 300;

    public static String scheduleSpeedAct;

    public static BiConsumer<String, String> ok = (className, msg) -> new Thread(() -> {
        MessageToUser emailMe = new ESender("143500@gmail.com");
        emailMe.info(ConstantsFor.APP_NAME, className + " ok", msg);
    }).start();

    /**
     Кол-во байт в килобайте
     */
    public static final int KILOBYTE = 1024;

    public static final AnnotationConfigApplicationContext CONTEXT = AppCtx.getCtx();

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
            AppComponents.getLogger().error(ConstantsFor.class.getSimpleName());
        }
        return myPC;
    }

    public static String localPc() {
        try{
            return InetAddress.getLocalHost().getHostName();
        }catch (UnknownHostException e){
            return e.getMessage();
        }
    }
}
