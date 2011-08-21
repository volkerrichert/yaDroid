package org.yavdr.yadroid.provider;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.yavdr.yadroid.adapter.EpgElement;
import org.yavdr.yadroid.core.YaVDRApplication;
import org.yavdr.yadroid.dao.YaDroidDBHelper;
import org.yavdr.yadroid.dao.pojo.Vdr;
import org.yavdr.yadroid.services.VdrService;

import com.j256.ormlite.dao.Dao;

import android.R.bool;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class SearchProvider extends ContentProvider {
	public static String AUTHORITY = "org.yavdr.yadroid.search";

	private static final String[] COLUMN_NAMES = new String[] {
			BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1,
			SearchManager.SUGGEST_COLUMN_TEXT_2,
			SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
			SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID };
	private static final int SEARCH_SUGGEST = 0;

	private final Calendar cal = Calendar.getInstance();
	private final SimpleDateFormat sdf = new SimpleDateFormat("E, d.M. HH:mm");
	private final SimpleDateFormat day = new SimpleDateFormat("HH:mm");

	private static UriMatcher uriMatcher;
	private Dao<Vdr, String> dao;

	private YaDroidDBHelper dbHelper;

	private Vdr vdr;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY,
				SEARCH_SUGGEST);
	}

	@Override
	public boolean onCreate() {

		dbHelper = new YaDroidDBHelper(getContext());

		hasRunningVDR();

		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		if (hasRunningVDR()) {
			String searchString = uri.getLastPathSegment();
			if (searchString.length() > 3) {
				List<EpgElement> shows = vdr.search(searchString, 0);
				MatrixCursor cursor = new MatrixCursor(COLUMN_NAMES);

				for (EpgElement show : shows) {
					cal.setTimeInMillis(show.getStartTime() * 1000);
					Object[] rowObject = new Object[] { show.getId(),
							show.getTitle(), sdf.format(cal.getTime()),
							"org.yavdr.yadroid.intent.action.EPGDETAIL",
							show.getId() };
					cursor.addRow(rowObject);
				}
				return cursor;
			}
			return null;
		} else
			return null;
	}

	@Override
	public String getType(Uri uri) {
		return SearchManager.SUGGEST_MIME_TYPE;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	private boolean hasRunningVDR() {
		if (vdr != null && vdr.isOnline()) {
			return true;
		} else {
			try {

				dao = dbHelper.getVdrDao();
				List<Vdr> knownVdr = dao.queryForAll();
				boolean found = false;

				for (Iterator<Vdr> iterator = knownVdr.iterator(); iterator
						.hasNext();) {
					vdr = (Vdr) iterator.next();

					if (vdr.isOnline()) {
						found = true;
						break;
					}
				}
				if (found)
					return true;
				vdr = null;
			} catch (SQLException e) {
			}
		}
		return false;
	}
}