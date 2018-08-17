package ru.vachok.networker.pasclass;



import org.springframework.stereotype.Repository;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;


/**
 * <h1>Репозиторий</h1>
 *
 * @since 10.08.2018 (22:09)
 */
@Repository
public class Repo implements Serializable {

    private static final long serialVersionUID = 4L;
    private static Repo iAm = new Repo();
    private static long tm;
    private static transient File oSer = new File(serialVersionUID + ".tmp");
    private final Map<Long, String> visMap = new ConcurrentHashMap<>();


    /**
     * Gets i.
     *
     * @param timeSt
     * @param remAddr
     * @return the i
     */
    public static Repo getI( long timeSt , String remAddr ) throws IOException {
        boolean newFile = oSer.createNewFile();
        if (newFile) {
            try (FileInputStream fileInputStream = new FileInputStream(oSer);
                 ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
                iAm = (Repo) objectInputStream.readObject();
                oSer.deleteOnExit();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return iAm;
    }


    private Map<Long, String> visitsMap( long tm , String newVisitor ) {
        visMap.put(tm , newVisitor);
        if (visMap.size() > 10) {
            toDB(visMap);
        } else visMap.forEach(( x , y ) -> {
            System.out.println(visMap.size() + " size");
            System.out.println("x = " + new Date(x));
            System.out.println("y = " + y);
        });
        writeO();
        visMap.forEach(( x , y ) -> {
            System.out.println("x = " + x);
            System.out.println("y = " + y);
        });
        writeO();
        return visMap;
    }


    private static void writeO() {
        try (FileOutputStream fileOutputStream = new FileOutputStream(oSer); ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
            objectOutputStream.writeObject(iAm);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void toDB( Map<Long, String> v ) {

        DataConnectTo dataConnectTo = new RegRuMysql();
        Connection c = dataConnectTo.getDefaultConnection("u0466446_liferpg");
        try (PreparedStatement preparedStatement = c.prepareStatement("insert into visitors (timemillis, ipaddr) values (?,?)");) {
            BiConsumer<Long, String> biConsumer = ( x , y ) -> {
                try {
                    preparedStatement.setTimestamp(1 , new Timestamp(x));
                    preparedStatement.setString(2 , y);
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            };
            v.forEach(biConsumer);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Gets repo.
     *
     * @return the repo
     */
    public String getRepo() {
        return this.toString();
    }
}