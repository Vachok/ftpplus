package ru.vachok.networker.events;


import org.springframework.context.ApplicationEvent;

import java.util.StringJoiner;


/**
 Class ru.vachok.networker.events.MyEvent
 <p>

 @since 06.05.2020 (12:32) */
public class MyEvent extends ApplicationEvent {


    private final Object source;

    public MyEvent(Object source) {
        super(source);
        this.source = source;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public String toString() {
        return new StringJoiner(",\n", MyEvent.class.getSimpleName() + "[\n", "\n]")
            .add("source = " + source)
            .toString();
    }
}