// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.sysinfo;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;


/**
 @since 15.06.2019 (14:00) */
@SuppressWarnings("ALL") public class VersionInfoTest {
    
    @Test
    public void testSetParams() {
        String setParamsString = AppComponents.versionInfo().toString();
        Assert.assertTrue(setParamsString.contains(ConstantsFor.thisPC()), setParamsString);
    }
    
    @Test
    public void getParamsTEST() {
        VersionInfo infoVers = AppComponents.versionInfo();
        String versString = infoVers.toString();
        Assert.assertFalse(versString.contains("rups00"), versString);
        Assert.assertEquals(AppComponents.getUserPref().get(ConstantsFor.PR_APP_VERSION, ""), infoVers.getAppVersion());
        Path parentPath = Paths.get(".").toAbsolutePath().normalize().getParent();
        String buildGradleString = "NO file Build Gradle";
        try {
            VersionInfoTest.BuildGradleSearch buildSearch = new VersionInfoTest.BuildGradleSearch();
            Files.walkFileTree(parentPath, buildSearch);
            Path pathBuildGradle = buildSearch.getBuildGradlePath();
            Assert.assertNotNull(pathBuildGradle, parentPath.toString());
            buildGradleString = FileSystemWorker.readFile(pathBuildGradle.toAbsolutePath().normalize().toString());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + " \n" + parentPath);
        }
        Assert.assertTrue(buildGradleString.contains(infoVers.getAppVersion()), infoVers.getAppVersion() + "\n" + buildGradleString);
    }
    
    private class BuildGradleSearch extends SimpleFileVisitor<Path> {
        
        
        private Path buildGradlePath;
        
        public Path getBuildGradlePath() {
            return buildGradlePath;
        }
        
        @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }
        
        @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (file.toFile().getName().toLowerCase().contains(ConstantsFor.FILENAME_BUILDGRADLE) & attrs.isRegularFile()) {
                this.buildGradlePath = file.toAbsolutePath().normalize();
            }
            return FileVisitResult.CONTINUE;
        }
        
        @Override public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
        
        @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }
}