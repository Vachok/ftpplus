package ru.vachok.money.mycar;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.config.ThrAsyncConfigurator;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


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

    private static MyOpel myOpel = new MyOpel();

    private String lastTimeA107;

    private String carName;

    private int countA107;

    private int countRiga;

    private int carMiletage;

    private double avgSpeedA107;

    private double avgSpeedRiga;

    private double avgTime;

    private String gosNum;

    private String lastTimeNRiga;

    /*Get&Set*/
    public void setAvgSpeedA107(double avgSpeedA107) {
        this.avgSpeedA107 = avgSpeedA107;
    }

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

    private void setAvgSpeedRiga(double avgSpeedRiga) {
        this.avgSpeedRiga = avgSpeedRiga;
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

    public static MyOpel getI() {
        return myOpel;
    }

    /*Instances*/
    private MyOpel() {
        ThreadPoolTaskExecutor defaultExecutor = new ThrAsyncConfigurator().getDefaultExecutor();
        defaultExecutor.initialize();
        Runnable runnable = () -> {
            getAvgSpeedA107();
            getAvgSpeedRiga();
        };
        defaultExecutor.execute(runnable);
    }

    /**
     @param rowsCount лимит строк
     @return средние показания MAF-датчика из 2х таблиц.
     */
    String getMAFAverages(int rowsCount) {
        String sql = new StringBuilder()
            .append("SELECT * FROM obdrawdata ORDER BY  'Mass Air Flow Rate(g/s)' DESC LIMIT 0 , ")
            .append(rowsCount).toString();
        ConcurrentMap<String, Double> mafSensorData = new ConcurrentHashMap<>();
        ConcurrentMap<String, Double> mafSensorData1 = new ConcurrentHashMap<>();
        DataConnectTo dataConnectTo = new RegRuMysql();
        Connection defaultConnection = dataConnectTo.getDefaultConnection(ConstantsFor.DB_PREFIX + "car");
        try(PreparedStatement preparedStatement = defaultConnection.prepareStatement(sql)){
            try(PreparedStatement preparedStatement1 = defaultConnection.prepareStatement(sql.replace("obdrawdata", "milcelon"))){
                try(ResultSet resultSet = preparedStatement.executeQuery()){
                    while(resultSet.next()){
                        String gpsTime = resultSet.getString("GPS Time");
                        double resultSetDouble = resultSet.getDouble("Mass Air Flow Rate(g/s)");
                        mafSensorData.put(gpsTime, resultSetDouble);
                    }
                    try(ResultSet resultSet1 = preparedStatement1.executeQuery()){
                        while(resultSet1.next()){
                            String gpsTime = resultSet1.getString("GPS Time");
                            double resultSetDouble = resultSet1.getDouble("Mass Air Flow Rate(g/s)");
                            mafSensorData1.put(gpsTime, resultSetDouble);
                        }
                    }
                }
            }
        }
        catch(SQLException e){
            String msg = "tryed " + rowsCount + " rows".toUpperCase() + "\n" + e.getMessage();
            LOGGER.warn(msg);
            if(rowsCount > 4998){
                rowsCount = rowsCount - 4999;
                String msg1 = rowsCount + " rows count now".toUpperCase();
                LOGGER.info(msg1);
                closeIt(defaultConnection);
                return getMAFAverages(rowsCount);
            }
            else{
                return e.getMessage();
            }
        }
        Collection<Double> values = mafSensorData.values();
        Collection<Double> values1 = mafSensorData1.values();
        List<Double> vList = new ArrayList<>();
        List<Double> vList1 = new ArrayList<>();
        vList.addAll(values);
        vList1.addAll(values1);
        double value = 0.0;
        double value1 = 0.0;
        for(int i = 0; i < vList.size(); i++){
            value = value + vList.get(i);
        }
        value = value / vList.size();
        for(int i = 0; i < vList1.size(); i++){
            value1 = value1 + vList.get(i);
        }
        value1 = value1 / vList1.size();
        String s = "<h2>Расход воздуха: " + value + " Normal Mass Air Flow rate g/s (v) || <font color=\"yellow\">" + value1 + " NOW</font> (v1) || diff (v-v1) is <i>" + (value - value1) + "</i" +
            "></h2>";
        try{
            File file = new File("maf.txt");
            if(!file.exists()){
                String newFile = "File is new = " + file.createNewFile();
                LOGGER.info(newFile);
            }
            writeToFile(s, file);
        }
        catch(IOException ignore){
            //
        }
        return s;
    }

    private void closeIt(Connection defaultConnection) {
        try{
            defaultConnection.close();
        }
        catch(SQLException e){
            LOGGER.error(e.getMessage(), e);
        }
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
            String msg = e.getSQLState() + " SQL state " + e.getErrorCode() + " err code " + e.getNextException() + " next e.";
            LOGGER.warn(msg);
            LOGGER.error(e.getMessage(), e);

        }
        if(road==0){
            setCountA107(speedsDoubles.size());
            return getRet(speedsDoubles, timeDoubles);
        }
        if(road==1){
            setCountRiga(speedsDoubles.size());
            return getRet(speedsDoubles, timeDoubles);
        }
        throw new UnsupportedOperationException("Не могу вычислить...");
    }

    private Map<String, Double> getRet(List<Double> speedsDoubles, List<Double> timeDoubles) throws NoSuchElementException {
        Map<String, Double> retMap = new HashMap<>();
        (( ArrayList<Double> ) speedsDoubles).trimToSize();
        OptionalDouble avgSpeed = speedsDoubles.stream().mapToDouble(x -> x).average();
        OptionalDouble averTime = timeDoubles.stream().mapToDouble(x -> x).average();
        retMap.put("speed", avgSpeed.getAsDouble());
        retMap.put("time", averTime.getAsDouble());
        return retMap;
    }

    private void writeToFile(String s, File file) {
        try(InputStream inputStream = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(reader)){
            StringBuilder sBuilder = new StringBuilder(s);
            while(reader.ready()){
                sBuilder
                    .append("\n")
                    .append(br.readLine())
                    .append("\n");
            }
            s = sBuilder.toString();
            try(OutputStream outputStream = new FileOutputStream(file);
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                BufferedWriter bw = new BufferedWriter(writer)){
                bw.write(s);
            }
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
    }

}