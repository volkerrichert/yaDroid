package org.yavdr.yadroid;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.yavdr.yadroid.adapter.Channel;
import org.yavdr.yadroid.adapter.ChannelAdapter;
import org.yavdr.yadroid.adapter.EndlessAdapter;
import org.yavdr.yadroid.adapter.EpgElement;
import org.yavdr.yadroid.adapter.EpgTeaserAdapter;
import org.yavdr.yadroid.core.YaVDRApplication;
import org.yavdr.yadroid.dao.pojo.Vdr;
import org.yavdr.yadroid.services.PushService;
import org.yavdr.yadroid.ui.coverflow.CoverFlow;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
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

public class EpgOverview extends YaVDRListActivity implements OnClickListener, OnKeyListener /*
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
	private Channel currentChannel;
	private int currentChannelPos;
	private YaVDRApplication app;
	private TextView tv;

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		app = (YaVDRApplication) getApplication();
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.main);

		final Bundle extras = getIntent().getExtras();

		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay();
		displayHeight = display.getHeight();
		displayWidth = display.getWidth();

		mStartActivity = extras.getBoolean(STARTACTIVITY);
		if (app.getCurrentVdr() != null) {
			JSONObject data = app.getCurrentVdr().queryInfo("/info.json");
			String channel = null;
			if (data != null && data.has("channel")) {
				try {
					channel = data.getString("channel");
				} catch (JSONException e) {
				}
			}
			data = app.getCurrentVdr().queryInfo(
					"/channels/" + channel + ".json");
			int channelNr = 0;
			if (data != null && data.has("channels")) {
				try {
					channelNr = data.getJSONArray("channels").getJSONObject(0)
							.getInt("number") - 1;
				} catch (JSONException e) {
				}
			}
			setListAdapter(epgListAdapter = new EpgListAdapter(
					items = new ArrayList<EpgElement>(), app.getCurrentVdr(),
					channel));

			View mCoverFlow = ((View) getWindow().findViewById(R.id.coverflow));
			tv = (TextView) mCoverFlow.findViewById(R.id.text);
			CoverFlow cFlow = (CoverFlow) mCoverFlow.findViewById(R.id.gallery);
			cFlow.setAdapter(new ChannelListAdapter(
					channelList = new ArrayList<Channel>(), channelNr));
			cFlow.setSelection(channelNr);
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
						app.getCurrentVdr().sendKeystroke(
								"switch/" + c.getChannelId());
					} catch (Exception e) {
						Log.e(TAG, e.getMessage());
					}
					return true;
				}
			});
		} else {
			Toast.makeText(getApplicationContext(), "kein vdr????", Toast.LENGTH_LONG);
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.epgoverview, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.manage_vdr:
			Intent intent = new Intent(
					"org.yavdr.yadroid.intent.action.MANAGEVDR");
			startActivity(intent);
			return true;
		case R.id.help:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
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
										PushService
												.actionStop(getApplicationContext());
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
			/*
			 * case KeyEvent.KEYCODE_MENU: if (vdrBound) { vdrService.keyMenu();
			 * // Intent intent = new //
			 * Intent("org.yavdr.yadroid.intent.action.MENU"); //
			 * intent.putExtras(getIntent().getExtras());
			 * 
			 * // startActivity(intent); } return true;
			 */
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
		Log.d(TAG, "click");
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent("org.yavdr.yadroid.intent.action.EPGDETAIL");
		intent.putExtra("EPGDETAIL", (Serializable) items.get(position));
		startActivity(intent);
	}

	class EpgListAdapter extends EndlessAdapter {
		private RotateAnimation rotate;
		private JSONObject data;
		private String channel;
		private Vdr vdr;

		EpgListAdapter(ArrayList<EpgElement> list, Vdr vdr, String channel) {
			super(new EpgTeaserAdapter(getApplicationContext(),
					R.layout.epgitem, list, EpgOverview.this));

			rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,
					0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			rotate.setDuration(600);
			rotate.setRepeatMode(Animation.RESTART);
			rotate.setRepeatCount(Animation.INFINITE);

			this.vdr = vdr;
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

			View pendingView = getLayoutInflater().inflate(R.layout.pending_epg, null);

			pendingView.setVisibility(View.VISIBLE);
			pendingView.startAnimation(rotate);

			return pendingView;
		}

		@Override
		protected boolean cacheInBackground() {
			try {
				EpgTeaserAdapter a = (EpgTeaserAdapter) getWrappedAdapter();

				long startTime = 0;
				if (a.getCount() > 0) {
					EpgElement lastItem = items.get(a.getCount() - 1);
					startTime = (lastItem.getStartTime() + lastItem
							.getDuration());
					data = vdr.queryInfo("/events/" + channel + "/0/"
							+ startTime + ".json?start=0&limit=15");
				} else
					data = vdr.queryInfo("/events/" + channel
							+ "/0.json?start=0&limit=15");

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
									epg.setImageUrl(vdr.getUrlPrefix()
											+ "/events/image/" + epg.getId()
											+ "/");
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
		private int startChannelNr;

		public ChannelListAdapter(ArrayList<Channel> list, int channelNr) {
			super(new ChannelAdapter(getApplicationContext(),
					R.layout.cover_flow_view, list, EpgOverview.this));

			rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,
					0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			rotate.setDuration(600);
			rotate.setRepeatMode(Animation.RESTART);
			rotate.setRepeatCount(Animation.INFINITE);

			startChannelNr = channelNr;
		}

		@Override
		protected boolean cacheInBackground() {
			try {

				ChannelAdapter a = (ChannelAdapter) getWrappedAdapter();
				if (a.getCount() > 0)
					data = app.getCurrentVdr().queryInfo(
							"/channels.json?start=" + a.getCount()
									+ "&limit=15");
				else
					data = app.getCurrentVdr().queryInfo(
							"/channels.json?start=0&limit="
									+ Math.max(5, startChannelNr));

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
								c.setImageUrl(app.getCurrentVdr()
										.getUrlPrefix()
										+ "/channels/image/"
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
		return false; // super.onKey(arg0, arg1, arg2);
	}

}