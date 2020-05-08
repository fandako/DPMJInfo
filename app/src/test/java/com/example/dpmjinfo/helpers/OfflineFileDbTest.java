package com.example.dpmjinfo.helpers;

import android.content.Context;
import android.util.Pair;

import androidx.test.core.app.ApplicationProvider;

import com.example.dpmjinfo.BusStop;
import com.example.dpmjinfo.queries.ActualDepartureQuery;
import com.example.dpmjinfo.queries.ScheduleQuery;
import com.example.dpmjinfo.queryModels.ActualDepartureQueryModel;
import com.example.dpmjinfo.queryModels.ScheduleQueryModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class OfflineFileDbTest {
    private OfflineFileDb db;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();

        db = new OfflineFileDb(context);
        db.clearDatabase();
    }

    @Test
    public void insertFileTest() {
        db.clearDatabase();

        String path = "/mock/path";
        db.insertFile(OfflineFilesManager.SCHEDULE, path);

        assertEquals(path, db.getFilePath(OfflineFilesManager.SCHEDULE));

        path += "/new";
        db.insertFile(OfflineFilesManager.SCHEDULE, path);

        assertEquals(path, db.getFilePath(OfflineFilesManager.SCHEDULE));
    }

    @Test
    public void deleteFileTest() {
        db.clearDatabase();

        String path = "/mock/path";
        db.insertFile(OfflineFilesManager.SCHEDULE, path);

        assertEquals(path, db.getFilePath(OfflineFilesManager.SCHEDULE));


        assertEquals(1, db.deleteFile(OfflineFilesManager.SCHEDULE));
        assertEquals("", db.getFilePath(OfflineFilesManager.SCHEDULE));
    }

    @Test
    public void insertFavouriteTest() {
        db.clearDatabase();

        ActualDepartureQueryModel model = new ActualDepartureQueryModel();
        model.setBusStop(new BusStop(1, "Chlumova"));

        ScheduleQuery q = ScheduleQuery.getQueryFromSerializedModel(context, ActualDepartureQuery.class.getSimpleName(), model);

        assertTrue(db.saveFavourite(q) > 0);

        //already saved favourite
        assertEquals(-2, db.saveFavourite(q));
    }

    @Test
    public void deleteFavouriteTest() {
        db.clearDatabase();

        ActualDepartureQueryModel model = new ActualDepartureQueryModel();
        model.setBusStop(new BusStop(1, "Chlumova"));

        ScheduleQuery q = ScheduleQuery.getQueryFromSerializedModel(context, ActualDepartureQuery.class.getSimpleName(), model);

        assertTrue(db.saveFavourite(q) > 0);

        assertTrue(db.deleteFavourite(q));

        //if successfully deleted (above), query can be inserted again
        assertTrue(db.saveFavourite(q) > 0);
    }

    @Test
    public void getFavouritesTest() {
        db.clearDatabase();

        ActualDepartureQueryModel model = new ActualDepartureQueryModel();
        model.setBusStop(new BusStop(1, "Chlumova"));

        ScheduleQuery q = ScheduleQuery.getQueryFromSerializedModel(context, ActualDepartureQuery.class.getSimpleName(), model);

        assertTrue(db.saveFavourite(q) > 0);

        List<Pair<String, ScheduleQueryModel>> favourites = db.getFavourites();

        assertEquals(1, favourites.size());

        ActualDepartureQueryModel modelFromDb = (ActualDepartureQueryModel) favourites.get(0).second;

        assertEquals(model.getBusStop().getCISId(), modelFromDb.getBusStop().getCISId());
        assertEquals(model.getBusStop().getName(), modelFromDb.getBusStop().getName());
    }


    @After
    public void finish() {
        db.close();
    }
}