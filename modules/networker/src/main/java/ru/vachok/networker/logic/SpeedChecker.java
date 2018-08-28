package ru.vachok.networker.logic;



import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.EMailAndDB.SpeedRunActualize;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ApplicationConfiguration;
import ru.vachok.networker.web.ConstantsFor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 @since 22.08.2018 (9:36) */
public class SpeedChecker implements Runnable{

    private static final DataConnectTo DATA_CONNECT_TO = new RegRuMysql();

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        SpeedRunActualize speedRunActualize = new SpeedRunActualize();
        speedRunActualize.run();
        speedRunActualize.avgInfo(0);
        speedRunActualize.avgInfo(1);
        chkForLast();
    }


    private void chkForLast() {
        String sql = "select * from speed";
        try (Connection c = DATA_CONNECT_TO.getDefaultConnection(ConstantsFor.DB_PREFIX + "liferpg"); PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {
            while (r.last()) {
                double timeSpend = r.getDouble("TimeSpend");
                ApplicationConfiguration.logger().warn(timeSpend + " time spend");
            }
        } catch (SQLException e) {
            ApplicationConfiguration.logger().error(e.getMessage() , e);
        }
    }
}
