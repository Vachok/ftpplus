package ru.vachok.networker.logic.ssh;


import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.config.AppComponents;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


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
                byte[] pemBytes;
                while(pem.available() > 0){
                    pemBytes = new byte[pem.read()];
                    int read = pem.read(pemBytes);
                    String msg = read + " bytes were read";
                    AppComponents.getLogger().info(msg);
                }
            }
        }
        catch(SQLException | IOException e){
            AppComponents.getLogger().error(e.getMessage(), e);
        }
    }
}
