package org.yavdr.yadroid.core.json;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.yavdr.yadroid.core.HttpClientFactory;

import android.util.Log;

public class JSONClient {

	protected int soTimeout = 3000, connectionTimeout = 3000;
	private String serviceUri;
	private static HttpClient httpClient;
	public static final String TAG = "de.wetter.JSONClien";

	// HTTP 1.0
	private static final ProtocolVersion PROTOCOL_VERSION = new ProtocolVersion(
			"HTTP", 1, 0);

	/**
	 * Create a JSONRPCClient from a given uri
	 * 
	 * @param uri
	 *            The URI of the JSON-RPC service
	 * @return a JSONRPCClient instance acting as a proxy for the web service
	 */
	public static JSONClient create(String uri) {
		return new JSONClient(uri);
	}

	/**
	 * Construct a JsonRPCClient with the given service uri
	 * 
	 * @param uri
	 *            uri of the service
	 */
	public JSONClient(String uri) {

		if (httpClient == null) { // Reuse client
			httpClient = HttpClientFactory.getThreadSafeClient();
/*			
			if (RTLApplication.useProxy) {
				HttpHost proxy = new HttpHost(RTLApplication.proxyHost,
						RTLApplication.proxyPort);
				httpClient.getParams().setParameter(
						ConnRoutePNames.DEFAULT_PROXY, proxy);
			}
*/
		}
		serviceUri = uri;
	}

	protected JSONObject doRequest(String method, Object[] params)
			throws JSONRPCException {
		// Copy method arguments in a json array
		JSONArray jsonParams = new JSONArray();
		for (int i = 0; i < params.length; i++) {
			if (params[i] instanceof String) {
				params[i] = UnicodeString.convert((String) params[i]);
			}
			jsonParams.put(params[i]);
		}
		// Create the json request object
		JSONObject jsonRequest = new JSONObject();
		try {
			// id hard-coded at 1 for now
			jsonRequest.put("id", (int) Math.random() * 1000);
			jsonRequest.put("method", method);
			jsonRequest.put("jsonrpc", "2.0");
			jsonRequest.put("params", jsonParams);
		} catch (JSONException e1) {
			throw new JSONRPCException("Invalid JSON request", e1);
		}
		return doJSONRequest(jsonRequest);
	}

	protected JSONObject doJSONRequest(JSONObject jsonRequest)
			throws JSONRPCException {

		// Create HTTP/POST request with a JSON entity containing the request
		HttpPost request = new HttpPost(serviceUri);

		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);

		HttpConnectionParams.setSoTimeout(params, soTimeout);
		HttpProtocolParams.setVersion(params, PROTOCOL_VERSION);
		request.setParams(params);

		HttpEntity entity;
		try {
			entity = new JSONEntity(jsonRequest);
		} catch (UnsupportedEncodingException e1) {
			throw new JSONRPCException("Unsupported encoding", e1);
		}
		request.setEntity(entity);
		long start = System.currentTimeMillis();
		try {
			// Execute the request and try to decode the JSON Response

			HttpResponse response = httpClient.execute(request);
			long duration = System.currentTimeMillis() - start;
			// Log.i(TAG, "Request time (in millis): " + duration);
			String responseString = EntityUtils.toString(response.getEntity());
			responseString = responseString.trim();
			JSONObject jsonResponse = new JSONObject(responseString);
			// Check for remote errors
			if (jsonResponse.has("error")) {
				Object jsonError = jsonResponse.get("error");
				if (!jsonError.equals(null))
					throw new JSONRPCException(jsonResponse.get("error"));
				return jsonResponse; // JSON-RPC 1.0
			} else {
				return jsonResponse; // JSON-RPC 2.0
			}
		}
		// Underlying errors are wrapped into a JSONRPCException instance
		catch (ClientProtocolException e) {
			long duration = System.currentTimeMillis() - start;
			throw new JSONRPCException("HTTP error (duration: " + duration
					+ ")", e);
		} catch (IOException e) {
			long duration = System.currentTimeMillis() - start;
			throw new JSONRPCException("IO error (duration: " + duration + ")",
					e);
		} catch (JSONException e) {
			long duration = System.currentTimeMillis() - start;
			throw new JSONRPCException("Invalid JSON response (duration: "
					+ duration + ")", e);
		}
	}

	/**
	 * Get the socket operation timeout in milliseconds
	 */
	public int getSoTimeout() {
		return soTimeout;
	}

	/**
	 * Set the socket operation timeout
	 * 
	 * @param soTimeout
	 *            timeout in milliseconds
	 */
	public void setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;

	}

	/**
	 * Get the connection timeout in milliseconds
	 */
	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	/**
	 * Set the connection timeout
	 * 
	 * @param connectionTimeout
	 *            timeout in milliseconds
	 */
	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	/**
	 * Perform a remote JSON-RPC method call
	 * 
	 * @param method
	 *            The name of the method to invoke
	 * @param params
	 *            Arguments of the method
	 * @return The result of the RPC
	 * @throws JSONRPCException
	 *             if an error is encountered during JSON-RPC method call
	 */
	public Object call(String method, Object... params) throws JSONRPCException {
		try {
			return doRequest(method, params).get("result");
		} catch (JSONException e) {
			throw new JSONRPCException("Cannot convert result", e);
		}
	}

	/**
	 * Perform a remote JSON-RPC method call
	 * 
	 * @param method
	 *            The name of the method to invoke
	 * @param params
	 *            Arguments of the method
	 * @return The result of the RPC as a String
	 * @throws JSONRPCException
	 *             if an error is encountered during JSON-RPC method call
	 */
	public String callString(String method, Object... params)
			throws JSONRPCException {
		try {
			return doRequest(method, params).getString("result");
		} catch (JSONException e) {
			throw new JSONRPCException("Cannot convert result to String", e);
		}
	}

	/**
	 * Perform a remote JSON-RPC method call
	 * 
	 * @param method
	 *            The name of the method to invoke
	 * @param params
	 *            Arguments of the method
	 * @return The result of the RPC as an int
	 * @throws JSONRPCException
	 *             if an error is encountered during JSON-RPC method call
	 */
	public int callInt(String method, Object... params) throws JSONRPCException {
		try {
			return doRequest(method, params).getInt("result");
		} catch (JSONException e) {
			throw new JSONRPCException("Cannot convert result to int", e);
		}
	}

	/**
	 * Perform a remote JSON-RPC method call
	 * 
	 * @param method
	 *            The name of the method to invoke
	 * @param params
	 *            Arguments of the method
	 * @return The result of the RPC as a long
	 * @throws JSONRPCException
	 *             if an error is encountered during JSON-RPC method call
	 */
	public long callLong(String method, Object... params)
			throws JSONRPCException {
		try {
			return doRequest(method, params).getLong("result");
		} catch (JSONException e) {
			throw new JSONRPCException("Cannot convert result to long", e);
		}
	}

	/**
	 * Perform a remote JSON-RPC method call
	 * 
	 * @param method
	 *            The name of the method to invoke
	 * @param params
	 *            Arguments of the method
	 * @return The result of the RPC as a boolean
	 * @throws JSONRPCException
	 *             if an error is encountered during JSON-RPC method call
	 */
	public boolean callBoolean(String method, Object... params)
			throws JSONRPCException {
		try {
			return doRequest(method, params).getBoolean("result");
		} catch (JSONException e) {
			throw new JSONRPCException("Cannot convert result to boolean", e);
		}
	}

	/**
	 * Perform a remote JSON-RPC method call
	 * 
	 * @param method
	 *            The name of the method to invoke
	 * @param params
	 *            Arguments of the method
	 * @return The result of the RPC as a double
	 * @throws JSONRPCException
	 *             if an error is encountered during JSON-RPC method call
	 */
	public double callDouble(String method, Object... params)
			throws JSONRPCException {
		try {
			return doRequest(method, params).getDouble("result");
		} catch (JSONException e) {
			throw new JSONRPCException("Cannot convert result to double", e);
		}
	}

	/**
	 * Perform a remote JSON-RPC method call
	 * 
	 * @param method
	 *            The name of the method to invoke
	 * @param params
	 *            Arguments of the method
	 * @return The result of the RPC as a JSONObject
	 * @throws JSONRPCException
	 *             if an error is encountered during JSON-RPC method call
	 */
	public JSONObject callJSONObject(String method, Object... params)
			throws JSONRPCException {
		try {
			return doRequest(method, params).getJSONObject("result");
		} catch (JSONException e) {
			throw new JSONRPCException("Cannot convert result to JSONObject", e);
		}
	}

	/**
	 * Perform a remote JSON-RPC method call
	 * 
	 * @param method
	 *            The name of the method to invoke
	 * @param params
	 *            Arguments of the method
	 * @return The result of the RPC as a JSONArray
	 * @throws JSONRPCException
	 *             if an error is encountered during JSON-RPC method call
	 */
	public JSONArray callJSONArray(String method, Object... params)
			throws JSONRPCException {
		try {
			return doRequest(method, params).getJSONArray("result");
		} catch (JSONException e) {
			throw new JSONRPCException("Cannot convert result to JSONArray", e);
		}
	}

	public JSONObject callSimple() throws URISyntaxException, JSONRPCException {

		URI uri = new URI(serviceUri);

		HttpGet request = new HttpGet(uri);

		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
		HttpConnectionParams.setSoTimeout(params, soTimeout);
		HttpProtocolParams.setVersion(params, PROTOCOL_VERSION);
		request.setParams(params);
		long start = System.currentTimeMillis();
		try {
			// Execute the request and try to decode the JSON Response

			HttpResponse response = httpClient.execute(request);
			//long duration = System.currentTimeMillis() - start;
			// Log.i(TAG, "Request Time (in millis): " + duration);
			String responseString = EntityUtils.toString(response.getEntity());
			responseString = responseString.trim();
			return new JSONObject(responseString);
		}
		// Underlying errors are wrapped into a JSONRPCException instance
		catch (ClientProtocolException e) {
			long duration = System.currentTimeMillis() - start;
			throw new JSONRPCException("HTTP error (duration: " + duration
					+ ")", e);
		} catch (IOException e) {
			long duration = System.currentTimeMillis() - start;
			throw new JSONRPCException("IO error (duration: " + duration + ")",
					e);
		} catch (JSONException e) {
			long duration = System.currentTimeMillis() - start;
			throw new JSONRPCException("Invalid JSON response (duration: "
					+ duration + ")", e);
		}
	}

	public JSONArray callSimpleArray() throws URISyntaxException,
			JSONRPCException {

		URI uri = new URI(serviceUri);

		HttpPost request = new HttpPost(uri);

		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
		HttpConnectionParams.setSoTimeout(params, soTimeout);
		HttpProtocolParams.setVersion(params, PROTOCOL_VERSION);
		request.setParams(params);
		long start = System.currentTimeMillis();
		try {
			// Execute the request and try to decode the JSON Response

			HttpResponse response = httpClient.execute(request);
			long duration = System.currentTimeMillis() - start;
			// Log.i(TAG, "Request Time (in millis): " + duration);
			String responseString = EntityUtils.toString(response.getEntity());
			responseString = responseString.trim();
			return new JSONArray(responseString);
		}
		// Underlying errors are wrapped into a JSONRPCException instance
		catch (ClientProtocolException e) {
			long duration = System.currentTimeMillis() - start;
			throw new JSONRPCException("HTTP error (duration: " + duration
					+ ")", e);
		} catch (IOException e) {
			long duration = System.currentTimeMillis() - start;
			throw new JSONRPCException("IO error (duration: " + duration + ")",
					e);
		} catch (JSONException e) {
			long duration = System.currentTimeMillis() - start;
			throw new JSONRPCException("Invalid JSON response (duration: "
					+ duration + ")", e);
		}
	}

}
