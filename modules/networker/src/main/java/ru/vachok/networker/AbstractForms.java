package ru.vachok.networker;


import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.prefs.Preferences;


public abstract class AbstractForms {


    private static final TForms T_FORMS = new TForms();

    @Contract(pure = true)
    public static TForms getI() {
        return T_FORMS;
    }

    @NotNull
    public static String networkerTrace(@NotNull Exception e) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage()).append("\n");
        stringBuilder.append(T_FORMS.networkerTrace(e.getStackTrace())).append("\n");
        try {
            stringBuilder.append(checkSu(e));
        }
        catch (RuntimeException su) {
            stringBuilder.append("NO Suppressed".toUpperCase());
            LoggerFactory.getLogger(AbstractForms.class.getSimpleName()).warn(AbstractForms.class.getSimpleName(), e.getMessage(), " see line: 29 ***");
        }
        return stringBuilder.toString();
    }

    @NotNull
    public static String fromArrayJson(@NotNull Map<Thread, StackTraceElement[]> threadStackMap) {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<Thread, StackTraceElement[]> threadEntry : threadStackMap.entrySet()) {
            jsonObject.add(threadEntry.getKey().toString(), fromArray(threadEntry.getValue()));
        }
        return jsonObject.toString();
    }

    public static String fromArray(StackTraceElement[] trace) {
        return T_FORMS.fromArray(trace);
    }

    public static String fromArray(Properties props) {
        return T_FORMS.fromArray(props);
    }

    public static String fromArray(Throwable e) {
        return T_FORMS.fromArray(e);
    }

    public static String fromArray(Map<?, ?> fromMap) {
        return T_FORMS.fromArray(fromMap);
    }

    public static JsonObject fromArray(ResultSetMetaData resultSetMetaData) throws SQLException {
        JsonObject jsonObject = new JsonObject();
        int colCount = resultSetMetaData.getColumnCount();
        jsonObject.add(" Columns", colCount);
        for (int i = 1; i < colCount; i++) {
            jsonObject.add("ColumnName", resultSetMetaData.getColumnName(i));
            jsonObject.add("ColumnLabel", resultSetMetaData.getColumnLabel(i));
            jsonObject.add("ColumnTypeName", resultSetMetaData.getColumnTypeName(i));
            jsonObject.add("ColumnType", resultSetMetaData.getColumnType(i));
            jsonObject.add("CatalogName", resultSetMetaData.getCatalogName(i));
            jsonObject.add("ColumnClassName", resultSetMetaData.getColumnClassName(i));
            jsonObject.add("ColumnDisplaySize", resultSetMetaData.getColumnDisplaySize(i));
            jsonObject.add("Precision", resultSetMetaData.getPrecision(i));
            jsonObject.add("Scale", resultSetMetaData.getScale(i));
            jsonObject.add("SchemaName", resultSetMetaData.getSchemaName(i));
            jsonObject.add("TableName", resultSetMetaData.getTableName(i));
            jsonObject.add("Signed", resultSetMetaData.isSigned(i));
        }
        return jsonObject;
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

    public static String fromArray(Preferences pref) {
        return T_FORMS.fromArray(pref);
    }

    public static String fromEnum(Enumeration<?> enumeration) {
        return T_FORMS.fromEnum(enumeration, true);
    }

    public static String sshCheckerMapWithDates(Map<String, Long> map, boolean b) {
        return T_FORMS.sshCheckerMapWithDates(map, b);
    }

    public static String networkerTrace(Throwable e) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage()).append("\n");
        stringBuilder.append(networkerTrace(e.getStackTrace()));
        return stringBuilder.toString();
    }

    @NotNull
    public static String networkerTrace(StackTraceElement[] trace) {
        return T_FORMS.networkerTrace(trace);
    }

    public static String fromArray(JsonObject jsonObject) {
        StringBuilder stringBuilder = new StringBuilder();
        if (jsonObject == null || jsonObject.isEmpty()) {
            stringBuilder.append("jsonObject is null of empty!");
        }
        else {
            stringBuilder.append("jsonObject:").append("\n");
            for (String name : jsonObject.names()) {
                stringBuilder.append(name).append(":").append(jsonObject.get(name)).append("\n");
            }
        }
        return stringBuilder.toString();
    }

    @NotNull
    private static String checkSu(@NotNull Throwable e) {
        StringBuilder stringBuilder = new StringBuilder();
        Throwable[] suppressedIfExists = e.getSuppressed();
        if (suppressedIfExists.length > 0) {
            for (Throwable throwable : suppressedIfExists) {
                stringBuilder.append(throwable.getClass().getSimpleName()).append(": ").append(throwable.getMessage()).append("\n");
                stringBuilder.append(T_FORMS.networkerTrace(throwable.getStackTrace())).append("\n");
                if (throwable.getSuppressed() != null) {
                    checkSu(throwable);
                }
                else {
                    stringBuilder.append("End Suppressed").append("\n");
                }
            }

        }
        return stringBuilder.toString();
    }
}
