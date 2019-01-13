package ru.vachok.networker.services;


import org.slf4j.Logger;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.sql.*;
import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 @since 22.08.2018 (9:36) */
public class SpeedChecker implements Callable<Long> {

    /**
     {@link RegRuMysql}
     */
    private static final DataConnectTo DATA_CONNECT_TO = new RegRuMysql();

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, MatrixCtr the thread causes the object's
     * <code>dnldRSA</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>dnldRSA</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public Long call() {
        Long chkForLastLong = chkForLast();
        String msg = new java.util.Date(chkForLastLong) + " from " + SpeedChecker.class.getSimpleName();
        LOGGER.info(msg);
        return chkForLastLong;
    }

    private static Long chkForLast() {
        String sql = "select * from speed";
        Long rtLong = Calendar.getInstance().getTimeInMillis() - ConstantsFor.getAtomicTime();
        try(Connection c = DATA_CONNECT_TO.getDefaultConnection(ConstantsFor.DB_PREFIX + "liferpg");
            PreparedStatement p = c.prepareStatement(sql);
            ResultSet r = p.executeQuery()){
            while (r.last()) {
                double timeSpend = r.getDouble("TimeSpend");
                long timeStamp = r.getTimestamp("TimeStamp").getTime();
                String msg = timeSpend + " time spend;\n" + timeStamp;
                rtLong = timeStamp + TimeUnit.MINUTES.toMillis(3);
                LOGGER.info(msg);
                return rtLong;
            }
        } catch (SQLException e) {
            FileSystemWorker.recFile(SpeedChecker.class.getSimpleName(), Collections.singletonList(new TForms().fromArray(e, false)));
        }
    return rtLong;
    }
}
