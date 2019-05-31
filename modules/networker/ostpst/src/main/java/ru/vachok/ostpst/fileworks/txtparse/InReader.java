package ru.vachok.ostpst.fileworks.txtparse;


import ru.vachok.ostpst.utils.TFormsOST;

import java.io.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class InReader {
    
    
    private BlockingQueue<String> stringsQueue = new ArrayBlockingQueue<>(100);
    
    private long lastPosRead;
    
    public InReader(long lastPosRead) {
        this.lastPosRead = lastPosRead;
    }
    
    public void dozenReadFile() {
        try (InputStream inputStream = new FileInputStream("C:\\Users\\ikudryashov\\IdeaProjects\\ftpplus\\modules\\networker\\192.168.13.220_43786.csv");
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        ) {
            while (stringsQueue.remainingCapacity() > 0) {
                bufferedReader.lines().distinct().forEach(x->{
                    try {
                        stringsQueue.put(x);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(new TFormsOST().fromArray(stringsQueue.stream()));
    }
    
    public static void main(String[] args) {
        new InReader(0).dozenReadFile();
    }
}
