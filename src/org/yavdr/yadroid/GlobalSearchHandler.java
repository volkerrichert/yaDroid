package org.yavdr.yadroid;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class GlobalSearchHandler extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		String intentAction = intent.getAction();
		Uri intentData = intent.getData();
		Long id = null;
		try {
			id = intentData.getLastPathSegment() != null ? Long
					.valueOf(intentData.getLastPathSegment()) : null;
		} catch (NumberFormatException e) {
			// null id may be just fine or it may be not
		}
		if (id != null) {
			Intent targetIntent = new Intent(intentAction);
			// configure the intent based on the retrieved id
			startActivity(targetIntent);
		}
		finish();
	}

}