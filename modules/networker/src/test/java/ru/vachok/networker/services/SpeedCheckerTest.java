// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;

import java.util.concurrent.*;


public class SpeedCheckerTest {
    
    
    private static final MessageToUser LOGGER = new MessageCons();
    
    public void rutClass() throws ExecutionException, InterruptedException {
        Callable<Long> speedChecker = new SpeedChecker();
        ExecutorService executorService = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor());
        Future<Long> submit = executorService.submit(speedChecker);
        LOGGER.infoNoTitles(speedChecker.toString());
        LOGGER.infoNoTitles(executorService.toString() + submit.get());
    }
    
}