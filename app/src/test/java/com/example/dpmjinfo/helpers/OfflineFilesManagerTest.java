package com.example.dpmjinfo.helpers;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.example.dpmjinfo.FakeRequestQueue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class OfflineFilesManagerTest {
    private Context context;
    private OfflineFilesManager ofm;
    private OfflineFileDb db;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();

        ofm = new OfflineFilesManager(context);
        db = new OfflineFileDb(context);
    }

    @Test
    public void getFilenameFromUrl() {
        String url = "http://localhost//dpmjinfoserver/CISdb/127.db";

        String name = ofm.getFilenameFromUrl(url);

        assertEquals("127.db", name);
    }

    @Test
    public void getFilePathFromUrl() {
        String url = "http://localhost//dpmjinfoserver/CISdb/127.db";

        String path = ofm.getFilePathFromUrl(url);
        String dir = ofm.getDownloadDir();

        assertEquals(dir + "/127.db", path);
    }

    @Test
    public void getFilePath() {
        db.clearDatabase();

        String path = "/mock/path/to/file";

        File testFile = context.getDir("mockDir", Context.MODE_PRIVATE);

        db.insertFile(OfflineFilesManager.SCHEDULE, testFile.getPath());

        String filePath = ofm.getFilePath(OfflineFilesManager.SCHEDULE);

        assertEquals(testFile.getPath(), filePath);

        assertTrue(testFile.delete());

        filePath = ofm.getFilePath(OfflineFilesManager.SCHEDULE);

        //file does not exists -> returns empty string
        assertEquals("", filePath);
    }

    @Test
    public void fileDownloaded() throws IOException {
        db.clearDatabase();

        String url = "http://localhost//dpmjinfoserver/CISdb/127.db";
        File testFile = context.getDir("127.db", Context.MODE_PRIVATE);
        File targetFile = new File(ofm.getDownloadDir() + "/127.db");
        Files.move(testFile.toPath(), targetFile.toPath());

        assertTrue(ofm.fileDownloaded(OfflineFilesManager.SCHEDULE, url));

        String path = ofm.getFilePath(OfflineFilesManager.SCHEDULE);

        assertEquals(ofm.getDownloadDir() + "/127.db", path);

        url = "http://localhost//dpmjinfoserver/CISdb/128.db";

        assertTrue(ofm.fileDownloaded(OfflineFilesManager.SCHEDULE, url));

        assertFalse(targetFile.exists());
    }

    @Test
    public void deleteFile() throws IOException {
        db.clearDatabase();

        assertFalse(ofm.deleteFile(OfflineFilesManager.SCHEDULE));

        String url = "http://localhost//dpmjinfoserver/CISdb/127.db";
        File testFile = context.getDir("127.db", Context.MODE_PRIVATE);
        File targetFile = new File(ofm.getDownloadDir() + "/127.db");
        Files.move(testFile.toPath(), targetFile.toPath());

        assertTrue(ofm.fileDownloaded(OfflineFilesManager.SCHEDULE, url));

        assertTrue(ofm.deleteFile(OfflineFilesManager.SCHEDULE));

        assertFalse(targetFile.exists());
    }

    /*@Test
    public void checkForUpdateTest(){
        db.clearDatabase();

        ofm.setQueue(new FakeRequestQueue("{\"isError\":false,\"errorMessage\":\"\",\"content\":[{\"id\":8,\"fileName\":\"CIS.zip\",\"uploadDate\":\"2020-04-01\",\"effectDate\":\"2020-04-27\",\"mobileFile\":\"http:\\/\\/testalbum.8u.cz\\/dpmjinfoserver\\/CISdb\\/8.db\",\"fileType\":\"schedule\"}]}"));

        List<String> filesToCheck = new ArrayList<>();
        filesToCheck.add(OfflineFilesManager.SCHEDULE);

        ofm.getFilesToDownload(new OfflineFileManagerRequestsDoneListener() {
            @Override
            public void onOfflineFileManagerRequestsDone(Hashtable<String, String> results) {
                assertTrue(results.contains(OfflineFilesManager.SCHEDULE));
            }
        }, filesToCheck);
    }*/
}