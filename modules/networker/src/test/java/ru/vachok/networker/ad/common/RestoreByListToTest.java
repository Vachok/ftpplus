package ru.vachok.networker.ad.common;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.*;


/**
 @see RestoreByListTo
 @since 12.09.2019 (13:30) */
public class RestoreByListToTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(RestoreByListTo.class.getSimpleName(), System.nanoTime());

    private Path folderToCopy = Paths.get(".");

    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }

    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }

    @Test
    public void testRestoreList() {
        this.folderToCopy = Paths.get(folderToCopy.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + "copiedtest");
        RestoreByListTo restoreByListTo = new RestoreByListTo(folderToCopy);
        Future<String> submit = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(restoreByListTo);
        try {
            String getFutureStr = submit.get(15, TimeUnit.SECONDS);
            System.out.println("getFutureStr = " + getFutureStr);
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        catch (TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }

    @Test
    @Ignore("For try only")
    public void logicCopy() {
        Queue<String> pathsQAsStrings = FileSystemWorker.readFileEncodedToQueue(Paths.get("search_37504.res"), "utf-8");
        Assert.assertTrue(pathsQAsStrings.size() > 1000, MessageFormat.format("{0} small size of Q", pathsQAsStrings.size()));
        Deque<Path> pathDeque = new ConcurrentLinkedDeque<>();
        while (!pathsQAsStrings.isEmpty()) {
            String toPath = pathsQAsStrings.poll();
            try {
                if (!toPath.isEmpty()) {
                    pathDeque.addFirst(Paths.get(toPath));
                }
            }
            catch (InvalidPathException ignore) {
                //12.09.2019 (13:36)
            }
        }
        Assert.assertTrue(pathDeque.size() > 1000, MessageFormat.format("{0} is small deq", pathDeque.size()));
        this.folderToCopy = Paths.get(folderToCopy.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + "copied");
        while (!pathDeque.isEmpty()) {
            Path fileToCopy = pathDeque.removeFirst();
            try {
                cpToDirs(fileToCopy);
            }
            catch (InvokeIllegalException e) {
                Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
            }
        }
    }

    private void cpToDirs(@NotNull Path fileForCopy) throws InvokeIllegalException {
        String parent = fileForCopy.getParent().getFileName().toString();
        parent = folderToCopy + ConstantsFor.FILESYSTEM_SEPARATOR + parent + ConstantsFor.FILESYSTEM_SEPARATOR + fileForCopy.getFileName().toString();
        FileSystemWorker.copyOrDelFile(fileForCopy.toFile(), Paths.get(parent), false);
    }
}