package ru.vachok.networker;


/**
 @since 03.10.2018 (16:28) */
public @interface Locked {
    Thread.State id();
}
