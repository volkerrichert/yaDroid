package org.yavdr.yadroid.dao;

import java.sql.SQLException;

import org.yavdr.yadroid.dao.pojo.Vdr;

import android.R.string;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * Database helper which creates and upgrades the database and provides the DAOs for the app.
 * 
 * @author kevingalligan
 */
public class YaDroidDBHelper extends OrmLiteSqliteOpenHelper {

	private static final String TAG = YaDroidDBHelper.class.toString();
	private static final String DATABASE_NAME = "yaDroid.db";
	private static final int DATABASE_VERSION = 3;

	private Dao<Vdr, String> vdrDao;

	public YaDroidDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, Vdr.class);
		} catch (SQLException e) {
			Log.e(TAG, "Unable to create datbases", e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVer, int newVer) {
		try {
			TableUtils.dropTable(connectionSource, Vdr.class, true);
			onCreate(sqliteDatabase, connectionSource);
		} catch (SQLException e) {
			Log.e(TAG, "Unable to upgrade database from version " + oldVer + " to new "
					+ newVer, e);
		}
	}

	public Dao<Vdr, String> getVdrDao() throws SQLException {
		if (vdrDao == null) {
			vdrDao = getDao(Vdr.class);
		}
		return vdrDao;
	}
	
	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		vdrDao = null;
	}
}