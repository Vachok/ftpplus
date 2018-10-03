package ru.vachok.networker.componentsrepo;


/**
 @since 03.10.2018 (9:24) */
public class ThrowMeMaybe {

    public static Throwable badTryException() {
        Throwable throwable = new UnsupportedOperationException("Nice try, but NO!");
        throwable.setStackTrace(Thread.currentThread().getStackTrace());
        return throwable;
    }
}
