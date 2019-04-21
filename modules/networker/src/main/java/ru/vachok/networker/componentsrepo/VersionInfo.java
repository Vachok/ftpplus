// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo;



import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.util.Objects;
import java.util.Properties;


/**

 @since 24.09.2018 (9:44) */
@Component(ConstantsFor.STR_VERSIONINFO)
@Scope(ConstantsFor.SINGLETON)
public class VersionInfo {

    /**
     Ссылка на /doc/index.html
     */
    private static final String DOC_URL = "<a href=\"/doc/index.html\">DOC</a>";

    /**
     {@link AppComponents#getProps()}
     */
    private static final Properties PROPERTIES = AppComponents.getProps();

    private static final String PR_APP_BUILD = "appBuild";

    /**
     {@link ConstantsFor#thisPC()}
     */
    private final String thisPCNameStr = ConstantsFor.thisPC();

    private static final MessageToUser messageToUser = new MessageLocal(VersionInfo.class.getSimpleName());
    /**
     Версия
     */
    private String appVersion = "No version";
    /**
     Билд
     */
    private String appBuild = String.valueOf(this.hashCode());
    /**
     Время сборки
     */
    private String buildTime = "1";


    @Override public int hashCode() {
        return Objects.hash(thisPCNameStr , appVersion , appBuild , buildTime);
    }


    @Override public boolean equals( Object o ) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersionInfo that = (VersionInfo) o;
        return Objects.equals(thisPCNameStr , that.thisPCNameStr) &&
            appVersion.equals(that.appVersion) &&
            appBuild.equals(that.appBuild) &&
            Objects.equals(buildTime , that.buildTime);
    }

    /**
     Конструктор по-умолчанию.
     <p>
     Если имя ПК содержит "home" или "no0" {@link #setParams()}
     */
    public VersionInfo() {
    }

    /**
     @return {@link #appBuild}
     */
    public String getAppBuild() {
        return appBuild;
    }

    /**
     @return {@link #appVersion}
     */
    public String getAppVersion() {
        return appVersion;
    }

    /**
     @return {@link #buildTime}
     */
    public String getBuildTime() {
        return buildTime;
    }



    /**
     *
     */
    public void setParams() {
        File file = new File("G:\\My_Proj\\FtpClientPlus\\modules\\networker\\build.gradle");

        if (file.exists()) {
            setterVersionFromFiles(file);
        } else {
            file = new File("C:\\Users\\ikudryashov\\IdeaProjects\\spring\\modules\\networker\\build.gradle");
            if (file.exists()) {
                setterVersionFromFiles(file);
            } else {
                try {
                    getParams();
                } catch (Exception e) {
                    messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".setParams" , e));
                }
            }
        }
    }


    /**
     Usages: {@link #setParams()} <br> Uses: - <br>

     @param file gradle.build
     */
    private void setterVersionFromFiles( File file ) {
        try (InputStream inputStream = new FileInputStream(file);
             InputStreamReader reader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(reader)
        )
        {
            bufferedReader.lines().forEach(x -> {
                if (x.contains("version = '8.")) {
                    PROPERTIES.setProperty(ConstantsFor.PR_APP_VERSION , x.split("'")[1]);
                }
            });
        } catch (IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".setterVersionFromFiles" , e));
        }
    }


    private void getParams() throws Exception {
        Properties properties = AppComponents.getProps();
        this.appBuild = properties.getProperty(ConstantsFor.PR_APP_BUILD , "Property does not exists");
        this.appVersion = properties.getProperty(ConstantsFor.PR_APP_VERSION , "Property does not exists");
        this.buildTime = properties.getProperty(ConstantsFor.PR_APP_BUILDTIME , "Property does not exists");
    }


    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("VersionInfo{");
        sb.append("appBuild='").append(appBuild).append('\'');
        sb.append(", appVersion='").append(appVersion).append('\'');
        sb.append(", buildTime='").append(buildTime).append('\'');
        sb.append(", thisPCNameStr='").append(thisPCNameStr).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
