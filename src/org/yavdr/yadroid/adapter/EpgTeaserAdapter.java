package org.yavdr.yadroid.adapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.yavdr.yadroid.R;
import org.yavdr.yadroid.core.ImageLoader;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class EpgTeaserAdapter extends BaseAdapter {

	public static final int EPG = 0;
	
	private List<EpgElement> teaser = null;
	private Activity activity;
	private static LayoutInflater inflater = null;
	
	private ImageLoader imageLoader;
	
	final Calendar cal = Calendar.getInstance();
	final SimpleDateFormat sdf = new SimpleDateFormat("E, d.M. HH:mm");
	final SimpleDateFormat day = new SimpleDateFormat("HH:mm");

	private int itemResId;

	private static final int TYPE_MAX_COUNT = 6;

	public EpgTeaserAdapter(final Context context, final int itemResId,
			final List<EpgElement> teaser, Activity activity) {
		super();
		
		this.teaser = teaser;
		this.activity = activity;
		this.itemResId = itemResId;

		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		imageLoader = new ImageLoader(activity.getApplicationContext());
	}

	public int getCount() {
		return teaser.size();
	}

	public Object getItem(int position) {
		return teaser.get(position);
	}

	public long getItemId(int position) {
		return position;
		//return teaser.get(position).getId();
	}
	
	public boolean add(EpgElement element) {
		return this.teaser.add(element);
	}
	
	public void clear() {
		this.teaser.clear();
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

		final EpgElement item = this.teaser.get(position);
		try {
			ViewHolder holder;
			int type = getItemViewType(position);
			if (convertView == null) {
				switch (type) {
				case EPG:
					convertView = inflater.inflate(itemResId, null);
					//convertView.setOnTouchListener((OnTouchListener) activity);
					//convertView.setOnClickListener((OnClickListener) activity);
					holder = new ViewHolder();
					holder.titleView = (TextView) convertView.findViewById(R.id.title);
					holder.secondlineView = (TextView) convertView.findViewById(R.id.secondLine);
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
			case EPG:
				EpgElement epg = (EpgElement) item;
				if (epg.getTitle().trim().length() > 0) {
					holder.titleView.setText(epg.getTitle());
					cal.setTimeInMillis(epg.getStartTime()*1000);
					holder.secondlineView.setText(sdf.format(cal.getTime()));
					
					holder.imageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.icon));
					if (epg.getImageCount() >= 1) {
						holder.imageView.setTag(epg.getImageUrl() + "0");
						//convertView.findViewById(R.id.image).setVisibility(View.VISIBLE);
					
						imageLoader.DisplayImage(epg.getImageUrl() + "0", activity, holder.imageView);
					} else {
						//holder.imageView.setTag(null);
						//imageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.icon));

						//imageView.setImage(R.drawable.icon);
						//convertView.findViewById(R.id.image).setVisibility(View.INVISIBLE);
					}
				} else {
					//convertView.setVisibility(View.GONE);
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
		public TextView titleView;
		public TextView secondlineView;
	}

}
