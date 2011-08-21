package org.yavdr.yadroid.adapter;

import java.util.List;

import org.yavdr.yadroid.R;
import org.yavdr.yadroid.core.ImageLoader;
import org.yavdr.yadroid.dao.pojo.Vdr;
import org.yavdr.yadroid.services.VdrService;
import org.yavdr.yadroid.ui.BitmapText;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

public class VdrAdapter extends BaseAdapter {

	public static final int VDR = 0;
	private static final String TAG = Vdr.class.toString();

	private List<Vdr> vdrs = null;
	private Activity activity;
	private static LayoutInflater inflater = null;
	private int itemResId;
	private Context context;

	public VdrAdapter(final Context context, final int itemResId,
			final List<Vdr> vdrs, Activity activity) {
		super();

		this.context = context;
		this.vdrs = vdrs;
		this.activity = activity;
		this.itemResId = itemResId;

		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return vdrs.size();
	}

	public Object getItem(int position) {
		return vdrs.get(position);
	}

	public long getItemId(int position) {
		// return teaser.get(position).getId();
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		final Vdr vdr = this.vdrs.get(position);
		try {
			ViewHolder holder;
			int type = getItemViewType(position);
			if (convertView == null) {
				switch (type) {
				case VDR:
					convertView = inflater.inflate(itemResId, null);
					holder = new ViewHolder();
					holder.titleView = (TextView) convertView
							.findViewById(R.id.title);
					holder.secondlineView = (TextView) convertView
							.findViewById(R.id.secondLine);
					holder.imageView = (ImageView) convertView
							.findViewById(R.id.image);
					holder.vdr = vdr;
					convertView.setTag(holder);
					break;
				default:
					throw new Exception("Unknown Element");

				}
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			switch (type) {
			case VDR:

				convertView.setVisibility(View.VISIBLE);
				holder.titleView.setText(vdr.getName());
				holder.secondlineView.setText("http://" + vdr.getAddress()
						+ ":" + vdr.getRestfulPort());

				if (holder.vdr.isOnline()) {
					holder.imageView.setImageResource(R.drawable.online);
				} else {
					holder.imageView.setImageResource(R.drawable.offline);
				}
				break;

			default:
				throw new Exception("Unknown Teaser Element");
			}

		} catch (Exception e) {

			Toast.makeText(activity.getApplicationContext(), e.toString(),
					Toast.LENGTH_LONG).show();
			Log.e(TAG, "Uncaught exception", e);
		}
		return convertView;
	}

	private static class ViewHolder {
		public ImageView imageView;
		public TextView titleView;
		public TextView secondlineView;
		public Vdr vdr;
	}

}
