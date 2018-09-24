package ru.vachok.networker.logic;


import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.EMailAndDB.SpeedRunActualize;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.sql.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 @since 22.08.2018 (9:36) */
public class SpeedChecker implements Runnable {

    private static final DataConnectTo DATA_CONNECT_TO = new RegRuMysql();

    /*Methods*/
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
    public void run() {
        chkForLast();
    }

    private static void speedRun() {
        Runnable speedRunActualize = new SpeedRunActualize();
        ScheduledExecutorService executorService =
            Executors.unconfigurableScheduledExecutorService(Executors.newSingleThreadScheduledExecutor());
        executorService.scheduleWithFixedDelay(speedRunActualize, ConstantsFor.INIT_DELAY, ConstantsFor.DELAY, TimeUnit.SECONDS);
    }

    private static void chkForLast() {
        String sql = "select * from speed";
        try(Connection c = DATA_CONNECT_TO.getDefaultConnection(ConstantsFor.DB_PREFIX + "liferpg");
            PreparedStatement p = c.prepareStatement(sql);
            ResultSet r = p.executeQuery()){
            while (r.last()) {
                double timeSpend = r.getDouble("TimeSpend");
                AppComponents.getLogger().warn(timeSpend + " time spend");
            }
        } catch (SQLException e) {
            AppComponents.getLogger().error(e.getMessage(), e);
        }
    }
}
