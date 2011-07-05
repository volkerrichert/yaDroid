package org.yavdr.yadroid.provider;

import java.util.ArrayList;
import java.util.List;

import org.yavdr.yadroid.adapter.EpgElement;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

public class SearchProvider extends ContentProvider {
	public static String AUTHORITY = "org.yavdr.yadroid.search";

	private static final String[] COLUMN_NAMES = new String[] { "_id",
			SearchManager.SUGGEST_COLUMN_TEXT_1,
			SearchManager.SUGGEST_COLUMN_TEXT_2,
			SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
			SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID };
	private static final int SEARCH_SUGGEST = 0;

	private static UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY,
				SEARCH_SUGGEST);
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		String searchString = uri.getLastPathSegment();
		MatrixCursor cursor = new MatrixCursor(COLUMN_NAMES);
		List<EpgElement> actions = new ArrayList<EpgElement>();
		EpgElement e = new EpgElement(1);
		e.setTitle("heute");
		e.setDescription("");

		actions.add(e);

		for (EpgElement action : actions) {
			Object[] rowObject = new Object[] { action.getId(),
					action.getTitle(), "heute 20:15",
					"org.yavdr.yadroid.intent.action.EPGDETAIL", action.getId() };
			cursor.addRow(rowObject);
		}
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case SEARCH_SUGGEST:
			return SearchManager.SUGGEST_MIME_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
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

}