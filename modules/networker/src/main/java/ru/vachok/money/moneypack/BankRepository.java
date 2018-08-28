package ru.vachok.money.moneypack;



import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ApplicationConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * <b>Репозиторий банков</b>
 *
 * @since 20.08.2018 (11:33)
 */
public class BankRepository {

    private static BankRepository ourInstance = new BankRepository();

    private static DataConnectTo dataConnectTo = new RegRuMysql();

    private static Connection connection = dataConnectTo.getDefaultConnection("u0466446_liferpg");

    private final Map<Integer, Banking> bankingMapByID;


    private BankRepository() {
        super();
        this.bankingMapByID = new LinkedHashMap<>();
        String sql = "select * from Banking";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql); ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                bankingMapByID.put(resultSet.getInt("idvisit") , new Banking(resultSet.getNString("product") , resultSet.getInt("value")));
            }
        } catch (SQLException e) {
            ApplicationConfiguration.logger().error(e.getMessage() , e);
        }
    }


    public static BankRepository getInstance() {
        return ourInstance;
    }


    public List<Banking> findAll() {
        return new ArrayList<>(this.bankingMapByID.values());
    }


    public Banking findID( final Integer idBanking ) {
        return this.bankingMapByID.get(idBanking);
    }
}
