package ru.vachok.networker;


import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public abstract class AbstractForms {
    
    
    private static final TForms T_FORMS = new TForms();
    
    public static @NotNull String exceptionNetworker(StackTraceElement[] trace) {
        return T_FORMS.exceptionNetworker(trace);
    }
    
    public static String fromArray(Properties props) {
        return T_FORMS.fromArray(props);
    }
    
    public static String fromArray(Exception e) {
        return T_FORMS.fromArray(e);
    }
    
    public static String fromArray(Map<?, ?> fromMap) {
        return T_FORMS.fromArray(fromMap);
    }
    
    public static @NotNull String fromArrayJson(@NotNull Map<Thread, StackTraceElement[]> threadStackMap) {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<Thread, StackTraceElement[]> threadEntry : threadStackMap.entrySet()) {
            jsonObject.add(threadEntry.getKey().toString(), fromArray(threadEntry.getValue()));
        }
        return jsonObject.toString();
    }
    
    public static String fromArray(StackTraceElement[] trace) {
        return T_FORMS.fromArray(trace);
    }
    
    public static String fromArray(Deque<?> objDequeue) {
        return T_FORMS.fromArray(objDequeue);
    }
    
    public static String fromArray(List<?> fromList) {
        return T_FORMS.fromArray(fromList);
    }
    
    public static String fromArray(Set<?> set) {
        return T_FORMS.fromArray(set);
    }
    
    public static String fromArray(Queue<?> queue) {
        return T_FORMS.fromArray(queue);
    }
    
    public static String fromArray(Object[] objects) {
        return T_FORMS.fromArray(objects);
    }
    
    public static String fromArray(Collection<?> collection) {
        return T_FORMS.fromArray(collection);
    }
}
