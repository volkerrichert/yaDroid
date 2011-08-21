package org.yavdr.yadroid;

import java.sql.SQLException;
import java.util.List;

import org.yavdr.yadroid.adapter.VdrAdapter;
import org.yavdr.yadroid.core.YaVDRApplication;
import org.yavdr.yadroid.dao.YaDroidDBHelper;
import org.yavdr.yadroid.dao.pojo.Vdr;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;

public class ManageVdr extends YaVDRListActivity {
	private static final int CONTEXTMENU_DELETEITEM = 0;
	private static final int CONTEXTMENU_EDITITEM = 1;

	private Dao<Vdr, String> dao;
	private YaDroidDBHelper dbHelper;
	private ListView lv;
	private List<Vdr> knownVdr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		dbHelper = new YaDroidDBHelper(getApplicationContext());

		lv = getListView();
		lv.setTextFilterEnabled(true);

		try {
			dao = dbHelper.getVdrDao();
		} catch (SQLException e) {
			dao = null;
		}

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		initListView();

		final Bundle extras = getIntent().getExtras();

		if (extras != null && extras.getBoolean("offline", false)) {
			Toast.makeText(getApplicationContext(), "keine VDR online",
					Toast.LENGTH_LONG).show();
		}
		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (position < knownVdr.size()) {
			((YaVDRApplication)getApplication()).setCurrentVdr(knownVdr.get(position));
		}
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.managevdr, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.add_vdr:
			Intent intent = new Intent("org.yavdr.yadroid.intent.action.ADDVDR");
			//startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void refreshListItems() {
		try {
			knownVdr = dao.queryForAll();

			setListAdapter(new VdrAdapter(getApplicationContext(),
					R.layout.managevdr_item, knownVdr, this));
		} catch (SQLException e) {
		}

	}

	private void initListView() {
		/* Loads the items to the ListView. */
		refreshListItems();

		/* Add Context-Menu listener to the ListView. */
		lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenu.ContextMenuInfo menuInfo) {
				menu.setHeaderTitle(((TextView) ((AdapterView.AdapterContextMenuInfo) menuInfo).targetView)
						.getText());
				menu.add(0, CONTEXTMENU_EDITITEM, 0, "Edit this VDR!");
				menu.add(0, CONTEXTMENU_DELETEITEM, 0, "Delete this VDR!");
				/* Add as many context-menu-options as you want to. */

			}
		});
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ContextMenuInfo menuInfo = (ContextMenuInfo) item.getMenuInfo();

		/* Switch on the ID of the item, to get what the user selected. */
		switch (item.getItemId()) {
		case CONTEXTMENU_DELETEITEM:
			return true; /* true means: "we handled the event". */
		case CONTEXTMENU_EDITITEM:
			return true; /* true means: "we handled the event". */
		}
		return super.onContextItemSelected(item);
	}

}
