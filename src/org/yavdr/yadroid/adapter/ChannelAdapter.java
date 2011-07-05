package org.yavdr.yadroid.adapter;

import java.util.List;

import org.yavdr.yadroid.R;
import org.yavdr.yadroid.core.ImageLoader;
import org.yavdr.yadroid.ui.BitmapText;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;


public class ChannelAdapter extends BaseAdapter {

	public static final int CHANNEL = 0;
	
	private List<Channel> channels = null;
	private Activity activity;
	private static LayoutInflater inflater = null;
	
	private ImageLoader imageLoader;
	
	private int itemResId;

	private Context context;

	public ChannelAdapter(final Context context, final int itemResId,
			final List<Channel> channels, Activity activity) {
		super();
		
		this.context = context;
		this.channels = channels;
		this.activity = activity;
		this.itemResId = itemResId;

		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		imageLoader = new ImageLoader(activity.getApplicationContext());
	}

	public int getCount() {
		return channels.size();
	}

	public Object getItem(int position) {
		return channels.get(position);
	}

	public long getItemId(int position) {
		//return teaser.get(position).getId();
		return position;
	}
	
	public void add(Channel element) {
		this.channels.add(element);
	}
	
	public int size() {
		return this.channels.size();
	}
	/*
	@Override
	public int getItemViewType(int position) {
		return EpgTeaserAdapter.EPG;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}
*/
	public View getView(int position, View convertView, ViewGroup parent) {

		final Channel item = this.channels.get(position);
		try {
			ViewHolder holder;
			int type = getItemViewType(position);
			if (convertView == null) {
				switch (type) {
				case CHANNEL:
					convertView = inflater.inflate(itemResId, null);
					//convertView.setOnTouchListener((OnTouchListener) activity);
					holder = new ViewHolder();
					holder.imageView = (ImageView) convertView.findViewById(R.id.image);
					convertView.setTag(holder);
					break;
				default:
					throw new Exception("Unknown Teaser Element");

				}
			 } else {
				 holder = (ViewHolder) convertView.getTag();
			 }
			
			switch (type) {
			case CHANNEL:
				Channel epg = (Channel) item;
				if (epg.getName().length() > 0) {
					convertView.setVisibility(View.VISIBLE);
					
					if (epg.getImageUrl() != null) {
						BitmapText bm = new BitmapText(context, epg.getName(), R.drawable.channelbackgound);
						// Find the correct scale value. It should be the power of 2.
						final int REQUIRED_SIZE = 70;
						int width_tmp = bm.getBounds().width(), height_tmp = bm.getBounds().height();
						int scale = 1;
						while (true) {
							if (width_tmp / 2 < REQUIRED_SIZE
									|| height_tmp / 2 < REQUIRED_SIZE)
								break;
							width_tmp /= 2;
							height_tmp /= 2;
							scale *= 2;
						}
						
						holder.imageView.setMinimumWidth(70);
						holder.imageView.setMaxWidth(70);
						holder.imageView.setImageDrawable(bm);
						holder.imageView.setScaleType(ScaleType.FIT_CENTER);
						
						holder.imageView.setTag( epg.getImageUrl());
						//convertView.findViewById(R.id.image).setVisibility(View.VISIBLE);
					
						imageLoader.DisplayImage(epg.getImageUrl(), activity, holder.imageView);
					} else {
						holder.imageView.setImageDrawable(new BitmapText(context, epg.getName(), R.drawable.channelbackgound));

						holder.imageView.setMinimumWidth(70);
						holder.imageView.setMaxWidth(70);
						holder.imageView.setScaleType(ScaleType.FIT_CENTER);
					}
				} else {
					convertView.setVisibility(View.GONE);
				}
				break;
				
			default:
				throw new Exception("Unknown Teaser Element");

			}

		} catch (Exception e) {

			Toast.makeText(activity.getApplicationContext(), e.toString(),
					Toast.LENGTH_LONG).show();
			Log.e("IMAGETEASERADAPTER", "Uncaught exception", e);
		}
		return convertView;
	}
	

	private static class ViewHolder {
		public ImageView imageView;
	}

}
