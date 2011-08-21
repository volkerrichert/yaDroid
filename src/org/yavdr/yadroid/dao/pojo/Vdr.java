package org.yavdr.yadroid.dao.pojo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.yavdr.yadroid.EpgOverview;
import org.yavdr.yadroid.adapter.EpgElement;
import org.yavdr.yadroid.core.json.JSONClient;
import org.yavdr.yadroid.core.json.JSONRPCException;

import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "vdr")
public class Vdr {
	@DatabaseField
	private String name;

	@DatabaseField
	private int restfulPort;

	@DatabaseField(id = true)
	private String address;

	public Vdr() {
	}

	public Vdr(String hostName, String hostAddress, int port) {
		name = hostName;
		address = hostAddress;
		restfulPort = port;

		urlPrefix = "http://" + address + ":" + port;
	}

	public String getAddress() {
		return address;
	}

	public String getName() {
		return name;
	}

	public int getRestfulPort() {
		return restfulPort;
	}

	private transient String urlPrefix = null;

	public String getUrlPrefix() {
		if (urlPrefix == null)
			urlPrefix = "http://" + address + ":" + restfulPort;
		return urlPrefix;
	}

	private static transient final String TAG = Vdr.class.toString();
	private static transient final DefaultHttpClient client = new DefaultHttpClient();

	public boolean isOnline() {
		try {

			HttpParams params = client.getParams();
			int connectionTimeout = HttpConnectionParams
					.getConnectionTimeout(params);
			int soTimeout = HttpConnectionParams.getSoTimeout(params);

			HttpConnectionParams.setConnectionTimeout(params, 500);
			HttpConnectionParams.setSoTimeout(params, 500);
			Log.d(TAG, "online check " + address);
			HttpResponse response = client.execute(new HttpGet(new URI(
					getUrlPrefix() + "/info.json")));

			HttpConnectionParams
					.setConnectionTimeout(params, connectionTimeout);
			HttpConnectionParams.setSoTimeout(params, soTimeout);

			Log.d(TAG, "online check result: "
					+ response.getStatusLine().getStatusCode());
			return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
		} catch (Exception e) {
			return false;
		}
	}

	public void sendKeystroke(String key) {
		try {
			client.execute(new HttpPost(new URI(getUrlPrefix() + "/remote/"
					+ key)));
		} catch (ClientProtocolException e) {
		} catch (IOException e) {

		} catch (URISyntaxException e) {
		}
	}

	public List<EpgElement> search(String query, int mode) {
		try {
			HttpEntity entity = new StringEntity("query=" + query + "&mode="
					+ mode, "UTF-8");

			HttpPost request = new HttpPost(new URI(getUrlPrefix()
					+ "/events/search.json?start=0&limit=10"));
			request.setEntity(entity);
			HttpResponse response = client.execute(request);

			String responseString = EntityUtils.toString(response.getEntity());
			JSONObject data = new JSONObject(responseString);

			if (data != null && data.getInt("count") > 0) {
				List<EpgElement> result = new ArrayList<EpgElement>();

				JSONArray ja = data.optJSONArray("events");
				if (ja != null) {
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
									epg.setImageUrl(urlPrefix
											+ "/events/image/" + epg.getId()
											+ "/");
								}
							}
							result.add(epg);
						} catch (Exception e) {
							Log.e(EpgOverview.class.toString(), "handle", e);
						}
					}
				}
				return result;
			}
			return null;
		} catch (Exception e) {
			Log.e(EpgOverview.class.toString(), "handle", e);
			return null;
		}
	}

	public JSONObject queryInfo(String query) {
		JSONClient client;
		client = JSONClient.create(getUrlPrefix() + query);
		client.setConnectionTimeout(3000);
		client.setSoTimeout(30000);
		try {
			return client.callSimple();
		} catch (Exception e) {
			return null;
		}
	}
}
