package ru.vachok.money.components;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.vachok.money.ConstantsFor;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.StringJoiner;


/**
 @since 27.09.2018 (1:02) */
@Component ("appversion")
public class AppVersion {

    public static final int GENERIC_ID = new SecureRandom().nextInt(1984);

    private static final Logger LOGGER = LoggerFactory.getLogger(AppVersion.class.getSimpleName());

    private static final String VERSION_STR = "version = '0.";

    private static final String COMPATIBILITY_STR = "sourceCompatibility";

    private static InitProperties initProperties = new DBRegProperties(ConstantsFor.APP_NAME + AppVersion.class.getSimpleName());

    private String appSrcVersion;

    private String appDisplayVersion;

    private String buildTime;

    private String appVBuild;

    /*Instances*/
    public AppVersion() {
        File no0027 = new File("c:\\Users\\ikudryashov\\IdeaProjects\\spring\\modules\\money\\build.gradle");
        File home = new File("g:\\My_Proj\\FtpClientPlus\\modules\\money\\build.gradle");
        if(no0027.exists()){
            infoSet(no0027);
        }
        if(home.exists()){
            infoSet(home);
        }
        else
            infoGet();
    }

    private void infoSet(File file) {
        Properties properties = initProperties.getProps();
        this.appVBuild = GENERIC_ID + "." + ConstantsFor.localPc();
        try (FileReader fileReader = new FileReader(file)) {
            BufferedReader reader = new BufferedReader(fileReader);
            setBuildTime(LocalDateTime.now().toString());
            reader.lines().forEach(x -> {
                if (x.contains(VERSION_STR)) {
                    setAppDisplayVersion(x);
                    properties.setProperty(VERSION_STR, x.replace(" = '0.", ""));
                }
                if (x.contains(COMPATIBILITY_STR)) {
                    setAppSrcVersion(x);
                    properties.setProperty(COMPATIBILITY_STR, x.replace("sourceCompatibility = ", ""));
                }
            });
            properties.setProperty("appbuild", appVBuild);
            properties.setProperty("buildtime", System.currentTimeMillis() + "");

            initProperties.delProps();
            initProperties.setProps(properties);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void infoGet() {
        Properties properties = initProperties.getProps();
        setAppDisplayVersion(properties.getProperty(VERSION_STR, "unknown"));
        setAppSrcVersion(properties.getProperty(COMPATIBILITY_STR, LocalDateTime.now().toString()));
        setAppVBuild(properties.getProperty("appbuild" ));
        setBuildTime(properties.getProperty("buildtime"));
    }

    public String getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(final String buildTime) {
        this.buildTime = buildTime;
    }

    public String getAppSrcVersion() {
        return appSrcVersion;
    }

    public void setAppSrcVersion(String appSrcVersion) {
        this.appSrcVersion = appSrcVersion;
    }

    public String getAppDisplayVersion() {
        return appDisplayVersion;
    }

    public void setAppDisplayVersion(String appDisplayVersion) {
        this.appDisplayVersion = appDisplayVersion;
    }

    public String getAppVBuild() {
        return appVBuild;
    }

    public void setAppVBuild(String appVBuild) {
        this.appVBuild = appVBuild;
    }

    @Override
    public String toString() {
        return new StringJoiner("\n", AppVersion.class.getSimpleName() + "\n", "\n")
            .add(appDisplayVersion + "'\n")
            .add(appSrcVersion + "'\n")
            .add("appVBuild='" + appVBuild + "'\n")
            .add("buildTime='" + buildTime + "'\n")
            .toString();
    }
}