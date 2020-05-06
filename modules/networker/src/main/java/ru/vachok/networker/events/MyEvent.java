package ru.vachok.networker.events;


import com.eclipsesource.json.JsonObject;
import org.springframework.context.ApplicationEvent;
import ru.vachok.networker.data.enums.PropertiesNames;


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
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(PropertiesNames.JSONNAME_CLASS, getClass().getSimpleName());
        jsonObject.add("source", source.toString());
        return jsonObject.toString();
    }
}