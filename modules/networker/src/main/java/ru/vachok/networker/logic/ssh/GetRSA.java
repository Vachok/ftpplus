package ru.vachok.networker.logic.ssh;


import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.config.AppComponents;

import java.io.InputStream;
import java.sql.*;


/**
 @since 22.08.2018 (12:33) */
public class GetRSA implements Runnable {

    /*Methods*/
    @Override
    public void run() {
        DataConnectTo dataConnectTo = new RegRuMysql();
        String sql = "select * from sshid where pc is like 'no00%'";
        try(Connection c = dataConnectTo.getDefaultConnection("u0466446_liferpg");
            PreparedStatement p = c.prepareStatement(sql);
            ResultSet r = p.executeQuery()){
            while(r.next()){
                InputStream pem = r.getBinaryStream("pem");
            }
        } catch(SQLException e){
            AppComponents.logger().error(e.getMessage(), e);
        }
    }
}
