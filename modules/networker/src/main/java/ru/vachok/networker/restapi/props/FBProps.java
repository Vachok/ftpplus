package ru.vachok.networker.restapi.props;


import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.firebase.cloud.FirestoreClient;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.PropertiesNames;

import java.io.File;
import java.util.Map;
import java.util.Properties;


/**
 Class ru.vachok.networker.restapi.props.FBProps
 <p>

 @since 06.05.2020 (2:52) */
class FBProps implements InitProperties {


    private final Properties properties = new Properties();

    private CollectionReference collection;

    @Override
    public Properties getProps() {
        this.collection = FirestoreClient.getFirestore().collection("props");
        collection.document("ver").addSnapshotListener((value, error)->{
            if (value != null) {
                Map<String, Object> data = value.getData();
                if (data != null) {
                    for (String s : data.keySet()) {
                        FBProps.this.properties.put(s, data.get(s));
                    }
                }
                FileSystemWorker.writeFile("app.ver", properties.getProperty(PropertiesNames.APPVERSION));
            }
        });
        if (this.properties.size() <= 0) {
            properties.setProperty("file", new File("app.ver").getAbsolutePath());
        }
        return properties;
    }

    @Override
    public boolean setProps(Properties properties) {
        this.collection = FirestoreClient.getFirestore().collection("props");
        DocumentReference document = collection.document("ver");
        document.set(properties);
        return true;
    }

    @Override
    public boolean delProps() {
        throw new UnsupportedOperationException("Only by hands!; 06.05.2020 (2:53)");
    }
}