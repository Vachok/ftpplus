// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.common;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 @see OldBigFilesInfoCollector
 @since 17.06.2019 (14:41) */
@Ignore
public class OldBigFilesInfoCollectorTest {


    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());

    private final OldBigFilesInfoCollector infoCollector = new OldBigFilesInfoCollector();

    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        testConfigureThreadsLogMaker.before();
    }

    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }

    @Test
    public void testCall() {
        String startPath = infoCollector.getStartPath();
        Assert.assertEquals(startPath, "\\\\srv-fs.eatmeat.ru\\common_new");
        Future<String> submit = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(infoCollector);
        try {
            submit.get();
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testTestToString() {
        Assert.assertTrue(infoCollector.toString().contains("OldBigFilesInfoCollector{"), infoCollector.toString());
    }

    @Test
    public void realCall() {
        OldBigFilesInfoCollector oldBigFilesInfoCollector = (OldBigFilesInfoCollector) IntoApplication.getContext()
            .getBean(OldBigFilesInfoCollector.class.getSimpleName());
        oldBigFilesInfoCollector.call();
    }

    @Test
    public void testInDB() {
        MysqlDataSource dataSource = DataConnectTo.getDefaultI().getDataSource();
        dataSource.setDatabaseName(ModelAttributeNames.COMMON);
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("select * from oldfiles")) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        System.out.println(MessageFormat.format("{0} is {1} {2} megabytes", resultSet.getInt(1), resultSet.getString(2), resultSet.getFloat(3)));
                        System.out.println(resultSet.getString(3));
                    }
                }
            }
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }

    @Test
    public void testConfirm() {
        try {
            String confirm = MessageToUser.getInstance(MessageToUser.SWING, this.getClass().getSimpleName())
                    .confirm(this.getClass().getSimpleName(), "Do you want to clean?", "msg");
            System.out.println("confirm = " + confirm);
        }
        catch (UnsupportedOperationException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }

    }
}