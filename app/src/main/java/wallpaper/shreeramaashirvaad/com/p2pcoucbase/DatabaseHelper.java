package wallpaper.shreeramaashirvaad.com.p2pcoucbase;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;

public class DatabaseHelper {


    static Database getEmptyDatabase(String name, Manager manager) {

        Database database = null;
        try {
            database = manager.getExistingDatabase(name);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        if (database != null)
            try {
                database.delete();
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }

        try {
            database = manager.getDatabase(name);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        return database;
    };

}
