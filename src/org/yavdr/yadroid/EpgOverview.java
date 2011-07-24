package org.yavdr.yadroid;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.yavdr.yadroid.adapter.Channel;
import org.yavdr.yadroid.adapter.ChannelAdapter;
import org.yavdr.yadroid.adapter.EndlessAdapter;
import org.yavdr.yadroid.adapter.EpgElement;
import org.yavdr.yadroid.adapter.EpgTeaserAdapter;
import org.yavdr.yadroid.core.YaVDRApplication;
import org.yavdr.yadroid.core.json.JSONClient;
import org.yavdr.yadroid.services.VdrService;
import org.yavdr.yadroid.services.VdrService.VdrBinder;
import org.yavdr.yadroid.ui.coverflow.CoverFlow;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class EpgOverview extends YaVDRListActivity implements OnClickListener,
		OnTouchListener, OnKeyListener /*
						 * , OnScrollListener
						 */{
	public static final String TAG = EpgOverview.class.toString();
	public static final String STARTACTIVITY = "START";

	private ArrayList<EpgElement> items;
	private ArrayList<Channel> channelList;

	private int displayHeight;
	private int displayWidth;
	private boolean mStartActivity = false;
	private EpgListAdapter epgListAdapter;
	private String urlPrefix;
	private Channel currentChannel;
	private int currentChannelPos;

	private android.os.Handler handler = new android.os.Handler();
	private TextView tv;

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.main);

		final Bundle extras = getIntent().getExtras();

		if (extras != null) {
			Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay();
			displayHeight = display.getHeight();
			displayWidth = display.getWidth();

			mStartActivity = extras.getBoolean(STARTACTIVITY);

			/*
			 * Context context = getApplicationContext(); int duration =
			 * Toast.LENGTH_SHORT;
			 * 
			 * Toast toast = Toast.makeText(context, "start", duration);
			 * toast.show();
			 */

			urlPrefix = ((YaVDRApplication)getApplication()).getRestfulPrefix();
			// currentChannel = extras.getString("channel");

			setListAdapter(epgListAdapter = new EpgListAdapter(
					items = new ArrayList<EpgElement>(), urlPrefix,
					extras.getString("channel")));

			View mCoverFlow = ((View) getWindow().findViewById(R.id.coverflow));

			tv = (TextView) mCoverFlow.findViewById(R.id.text);

			CoverFlow cFlow = (CoverFlow) mCoverFlow.findViewById(R.id.gallery);

			cFlow.setAdapter(new ChannelListAdapter(
					channelList = new ArrayList<Channel>(), extras.getString("channel")));

			cFlow.setMaxZoom(-300);
			cFlow.setSpacing(0);
			tv.setText("loading ...");
			cFlow.setAnimationDuration(800);
			cFlow.setOnItemSelectedListener(new OnItemSelectedListener() {

				public void onItemSelected(
						@SuppressWarnings("rawtypes") AdapterView parent,
						View view, final int position, long id) {
					currentChannelPos = position;

					if (position < channelList.size()) {
						tv.setText(Integer.toString(channelList.get(position)
								.getNumber())
								+ " - "
								+ channelList.get(position).getName());
					} else {
						tv.setText("loading ...");
					}
					/*
					 * handler.postDelayed(new Runnable() {
					 * 
					 * @Override public void run() { switchChannel(position); }
					 * }, 5000);
					 */
				}

				public void onNothingSelected(AdapterView<?> view) {

				}
			});
			cFlow.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {

					switchChannel(position);

				}
			});
			cFlow.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> parent,
						View view, int position, long id) {

					Channel c = channelList.get(position);
					currentChannel = c;
					// tv.setText("loading ...");
					epgListAdapter.setChannelId(currentChannel.getChannelId());

					try {
						(new DefaultHttpClient()).execute(new HttpPost(new URI(
								urlPrefix + "/remote/switch/"
										+ c.getChannelId())));
					} catch (Exception e) {
						Log.e(TAG, e.getMessage());
					}
					return true;
				}
			});
			
		} else {
			Toast toast = new Toast(getApplicationContext());
			toast.setText("Fehler");
			toast.show();
			finish();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// The activity has become visible (it is now "resumed").
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Another activity is taking focus (this activity is about to be
		// "paused").
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// The activity is about to be destroyed.
	}

	@Override
	public boolean onTouch(View view, MotionEvent ev) {
		return false;
	}
    
	
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
	    if (event.getKeyCode() == KeyEvent.KEYCODE_POWER) {

	        return true;
	    }

	    return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (mStartActivity) {
				// bulid exit Dialog!
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Möchten Sie die App wirklich beenden?")
						.setCancelable(false)
						.setPositiveButton("Ja",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										EpgOverview.this.finish();
										return;
									}
								})
						.setNegativeButton("Nein",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										return;
									}
								});
				AlertDialog alert = builder.create();
				alert.show();
				return false;
			}
			break;
		case KeyEvent.KEYCODE_VOLUME_UP:
			if (vdrBound)
				vdrService.keyVolUp();
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if (vdrBound)
				vdrService.keyVolDown();
			return true;
		case KeyEvent.KEYCODE_MENU:
			if (vdrBound) {
				vdrService.keyMenu();
				// Intent intent = new
				// Intent("org.yavdr.yadroid.intent.action.MENU");
				// intent.putExtras(getIntent().getExtras());

				// startActivity(intent);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);

	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyLongPress(keyCode, event);
	}
	
	@Override
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyMultiple(keyCode, repeatCount, event);
	}
	
	@Override
	public void onClick(View view) {
		Log.d(TAG, "clikc");
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent("org.yavdr.yadroid.intent.action.EPGDETAIL");
		intent.putExtra("EPGDETAIL", (Serializable)items.get(position));
		startActivity(intent);
	}

	class EpgListAdapter extends EndlessAdapter {
		private RotateAnimation rotate;
		private JSONObject data;
		private String urlPrefix;
		private String channel;

		EpgListAdapter(ArrayList<EpgElement> list, String urlPrefix,
				String channel) {
			super(new EpgTeaserAdapter(getApplicationContext(),
					R.layout.epgitem, list, EpgOverview.this));

			rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,
					0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			rotate.setDuration(600);
			rotate.setRepeatMode(Animation.RESTART);
			rotate.setRepeatCount(Animation.INFINITE);

			this.urlPrefix = urlPrefix;
			this.channel = channel;
		}

		public void setChannelId(String channelId) {
			if (channelId != null && !channelId.equals(channel)) {
				this.channel = channelId;
				((EpgTeaserAdapter) getWrappedAdapter()).clear();
				data = null;
				notifyDataSetInvalidated();
			}
		}

		@Override
		protected View getPendingView(ViewGroup parent) {
			View row = getLayoutInflater().inflate(R.layout.pending_epg, null);

			row.setVisibility(View.VISIBLE);
			row.startAnimation(rotate);

			return (row);
		}

		@Override
		protected boolean cacheInBackground() {
			try {
				EpgTeaserAdapter a = (EpgTeaserAdapter) getWrappedAdapter();

				long startTime = 0;
				JSONClient client;
				if (a.getCount() > 0) {
					EpgElement lastItem = items.get(a.getCount() - 1);
					startTime = (lastItem.getStartTime() + lastItem
							.getDuration());
					client = JSONClient.create(urlPrefix + "/events/" + channel
							+ "/0/" + startTime + ".json?start=0&limit=15");
				} else
					client = JSONClient.create(urlPrefix + "/events/" + channel
							+ "/0.json?start=0&limit=15");
				client.setConnectionTimeout(3000);
				client.setSoTimeout(30000);
				data = client.callSimple();
				if (data != null) {
					return data.getInt("count") > 0
							&& data.getInt("total") >= items.size();
				}
			} catch (Exception e) {
				Log.e(EpgListAdapter.class.toString(), "catch Exception", e);
			}

			// no more data
			return false;
		}

		@Override
		protected void appendCachedData() {
			if (data != null) {
				EpgTeaserAdapter a = (EpgTeaserAdapter) getWrappedAdapter();

				JSONArray ja = data.optJSONArray("events");
				if (ja != null)
					for (int i = 0; i < ja.length(); ++i) {
						try {
							JSONObject elem = ja.optJSONObject(i);

							EpgElement epg = new EpgElement(elem.getInt("id"));

							epg.setTitle(elem.getString("title"));
							epg.setShortText(elem.getString("short_text"));
							epg.setDescription(elem.getString("description"));
							epg.setStartTime(elem.getLong("start_time"));
							epg.setDuration(elem.getInt("duration"));

							if (elem.has("images")) {
								int images = elem.getInt("images");
								epg.setImageCount(images);
								if (images > 0) {
									epg.setImageUrl(urlPrefix + "/events/image/"
											+ epg.getId() + "/");
								}
							}
							a.add(epg);
						} catch (Exception e) {
							Log.e(EpgOverview.class.toString(), "handle", e);
						}
						// Silent ignore!
					}

			}
		}
	}

	class ChannelListAdapter extends EndlessAdapter {
		private JSONObject data;
		private RotateAnimation rotate;

		public ChannelListAdapter(ArrayList<Channel> list, String channel) {
			super(new ChannelAdapter(getApplicationContext(),
					R.layout.cover_flow_view, list, EpgOverview.this));

			rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,
					0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			rotate.setDuration(600);
			rotate.setRepeatMode(Animation.RESTART);
			rotate.setRepeatCount(Animation.INFINITE);
		}

		@Override
		protected boolean cacheInBackground() {
			try {
				String urlPrefix = ((YaVDRApplication)getApplication()).getRestfulPrefix();

				ChannelAdapter a = (ChannelAdapter) getWrappedAdapter();

				JSONClient client;
				if (a.getCount() > 0)
					client = JSONClient.create(urlPrefix
							+ "/channels.json?start=" + a.getCount()
							+ "&limit=15");
				else
					client = JSONClient.create(urlPrefix
							+ "/channels.json?start=0&limit=5");

				client.setConnectionTimeout(3000);
				client.setSoTimeout(30000);
				data = client.callSimple();
				if (data != null) {
					return data.getInt("count") > 0
							&& data.getInt("total") >= channelList.size();
				}
			} catch (Exception e) {
				Log.e(TAG, "handle", e);
			}
			return false;
		}

		@Override
		protected void appendCachedData() {
			if (data != null) {
				ChannelAdapter a = (ChannelAdapter) getWrappedAdapter();

				JSONArray ja = data.optJSONArray("channels");
				if (ja != null)
					for (int i = 0; i < ja.length(); ++i) {
						try {
							JSONObject elem = ja.optJSONObject(i);

							Channel c = new Channel(
									elem.getString("channel_id"));
							c.setName(elem.getString("name"));
							c.setNumber(elem.getInt("number"));
							c.setGroup(elem.getString("group"));
							c.setTransponter(elem.getInt("transponder"));
							c.setStream(elem.getString("stream"));
							c.setAtsc(elem.getBoolean("is_atsc"));
							c.setCable(elem.getBoolean("is_cable"));
							c.setTerr(elem.getBoolean("is_terr"));
							c.setSat(elem.getBoolean("is_sat"));
							c.setRadio(elem.getBoolean("is_radio"));

							if (elem.getBoolean("image")) {
								c.setImageUrl(urlPrefix + "/channels/image/"
										+ c.getChannelId());
							}
							if (a.size() == currentChannelPos) {
								tv.setText(Integer.toString(c.getNumber())
										+ " - " + c.getName());
							}
							a.add(c);
						} catch (Exception e) {
							Log.e(EpgOverview.class.toString(), "handle", e);
						}
					}
			}
		}

		@Override
		protected View getPendingView(ViewGroup parent) {
			View row = getLayoutInflater().inflate(R.layout.pending_channel,
					null);

			// row.setVisibility(View.VISIBLE);
			// row.startAnimation(rotate);

			return (row);
		}

		/**
		 * Returns the size (0.0f to 1.0f) of the views depending on the
		 * 'offset' to the center.
		 */
		public float getScale(boolean focused, int offset) {
			/* Formula: 1 / (2 ^ offset) */
			return Math.max(0, 1.0f / (float) Math.pow(2, Math.abs(offset)));
		}
	}

	private void switchChannel(int position) {

		if (position < channelList.size()) {
			currentChannel = channelList.get(position);
			epgListAdapter.setChannelId(currentChannel.getChannelId());
		}
	}

	@Override
	public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
		// TODO Auto-generated method stub
		return false; //super.onKey(arg0, arg1, arg2);
	}

}