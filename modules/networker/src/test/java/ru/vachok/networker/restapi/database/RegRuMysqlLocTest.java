// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.database;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.restapi.DataConnectTo;

import java.sql.Connection;
import java.sql.SQLException;


/**
 @see RegRuMysqlLoc
 @since 14.07.2019 (12:34) */
public class RegRuMysqlLocTest {
    
    private DataConnectTo dataConTo = new RegRuMysqlLoc();
    
    
    @Test
    public void testGetDefaultConnection() {
        try (Connection connection = dataConTo.getDefaultConnection(ConstantsFor.DBBASENAME_U0466446_TESTING)) {
            connection.isValid((int) ConstantsFor.DELAY);
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }catch (InvokeEmptyMethodException e){
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testToString1() {
        
        try{
            System.out.println(dataConTo.toString());
        }catch (ExceptionInInitializerError e){
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
}