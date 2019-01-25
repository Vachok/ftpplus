package ru.vachok.networker.errorexceptions;


/**
 Заменяем {@link NullPointerException}

 @since 21.01.2019 (23:15) */
public class MyNull extends Throwable {

    /**
     @param msg сообщение пользователю
     @return new {@link Throwable} with {@link NullPointerException} cause
     */
    public Throwable throwMe(String msg) {
        Throwable t = new Throwable(msg);
        t.initCause(new NullPointerException());
        return t;
    }

}