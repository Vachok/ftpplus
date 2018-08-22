package ru.vachok.networker.logic.ssh;


import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.web.ApplicationConfiguration;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 @since 22.08.2018 (12:33) */
public class GetRSA implements Runnable {

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
      }
      catch(SQLException e){
         ApplicationConfiguration.logger().error(e.getMessage(), e);
      }
   }
}
