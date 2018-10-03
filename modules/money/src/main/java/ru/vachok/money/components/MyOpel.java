package ru.vachok.money.components;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.config.ThrAsyncConfigurator;
import ru.vachok.mysqlandprops.RegRuMysql;

import java.sql.*;
import java.util.*;


/**
 @since 01.09.2018 (20:26) */
@Component
public class MyOpel {

    /*Fields*/
    private static final Connection DEF_CON = new RegRuMysql()
        .getDefaultConnection(ConstantsFor.DB_PREFIX + "liferpg");

    private static final String SOURCE_CLASS = MyOpel.class.getSimpleName();

    private static final Logger LOGGER = LoggerFactory.getLogger(SOURCE_CLASS);

    private static final String SPEED = "Speed";

    private String lastTimeA107;

    private String carName;

    private int countA107;
    private int countRiga;

    public int getCountA107() {
        return countA107;
    }

    public void setCountA107(int countA107) {
        this.countA107 = countA107;
    }

    public int getCountRiga() {
        return countRiga;
    }

    public void setCountRiga(int countRiga) {
        this.countRiga = countRiga;
    }

    private int carMiletage;

    private double avgSpeedA107;

    private double avgSpeedRiga;

    private double avgTime;

    private String gosNum;

    private String lastTimeNRiga;

    public String getLastTimeA107() {
        return lastTimeA107;
    }

    public void setLastTimeA107(String lastTimeA107) {
        this.lastTimeA107 = lastTimeA107;
    }

    public String getLastTimeNRiga() {
        return lastTimeNRiga;
    }

    public void setLastTimeNRiga(String lastTimeNRiga) {
        this.lastTimeNRiga = lastTimeNRiga;
    }

    public double getAvgSpeedA107() {
        Map<String, Double> stringDoubleMap = avgInfo(0);
        Double speed = stringDoubleMap.get(SPEED.toLowerCase());
        setAvgSpeedA107(speed);
        return avgSpeedA107;
    }

    public String getGosNum() {
        return gosNum;
    }

    public void setGosNum(String gosNum) {
        this.gosNum = gosNum;
    }

    public int getCarMiletage() {
        return carMiletage;
    }

    public void setCarMiletage(int carMiletage) {
        this.carMiletage = carMiletage;
    }

    public double getAvgTime() {
        return avgTime;
    }

    public void setAvgTime(double avgTime) {
        this.avgTime = avgTime;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public double getAvgSpeedRiga() {
        Map<String, Double> stringDoubleMap = avgInfo(1);
        Double speed = stringDoubleMap.get(SPEED.toLowerCase());
        setAvgSpeedRiga(speed);
        return avgSpeedRiga;
    }

    public void setAvgSpeedA107(double avgSpeedA107) {
        this.avgSpeedA107 = avgSpeedA107;
    }

    /*Instances*/
    public MyOpel() {
        ThreadPoolTaskExecutor defaultExecutor = new ThrAsyncConfigurator().getDefaultExecutor();
        defaultExecutor.initialize();
        Runnable runnable = () -> {
            getAvgSpeedA107();
            getAvgSpeedRiga();
        };
        defaultExecutor.execute(runnable);
    }

    /**
     <b>Среднее по Бетонке</b>
     */
    private Map<String, Double> avgInfo(int road) {
        List<Double> speedsDoubles = new ArrayList<>();
        List<Double> timeDoubles = new ArrayList<>();
        try(PreparedStatement ps = DEF_CON.prepareStatement("select * from speed where Road = ?")){
            ps.setInt(1, road);
            try(ResultSet r = ps.executeQuery()){
                while(r.next()){
                    speedsDoubles.add(r.getDouble(SPEED));
                    timeDoubles.add(r.getDouble("TimeSpend"));
                }
                if(r.last() && road==0){
                    setLastTimeA107(r.getString("TimeStamp"));
                }
                if(r.last() && road==1){
                    setLastTimeNRiga(r.getString("TimeStamp"));
                }
            }
        }
        catch(SQLException e){
            LOGGER.error(e.getMessage(), e);
        }
        if(road==0) setCountA107(speedsDoubles.size());
        if(road==1) setCountRiga(speedsDoubles.size());
        Map<String, Double> retMap = new HashMap<>();
        (( ArrayList<Double> ) speedsDoubles).trimToSize();
        OptionalDouble avgSpeed = speedsDoubles.stream().mapToDouble(x -> x).average();
        OptionalDouble averTime = timeDoubles.stream().mapToDouble(x -> x).average();
        retMap.put("speed", avgSpeed.getAsDouble());
        retMap.put("time", averTime.getAsDouble());
        return retMap;
    }

    public void setAvgSpeedRiga(double avgSpeedRiga) {
        this.avgSpeedRiga = avgSpeedRiga;
    }

}