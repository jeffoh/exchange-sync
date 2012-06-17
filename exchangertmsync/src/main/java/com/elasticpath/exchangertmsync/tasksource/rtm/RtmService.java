package com.elasticpath.exchangertmsync.tasksource.rtm;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.elasticpath.exchangertmsync.tasksource.TaskDto;

public class RtmService {
	private static final String REST_HOST = "api.rememberthemilk.com";
	private static final String REST_AUTH_PATH = "/services/auth/";
	private static final String REST_METHOD_PATH = "/services/rest/";
	
	private String apiKey;
	private String sharedSecret;
	private RtmSettings settings;
	
	public RtmService(final String apiKey, final String sharedSecret, final RtmSettings settings) {
		this.apiKey = apiKey;
		this.sharedSecret = sharedSecret;
		this.settings = settings;
	}
	
	public RtmAuthStatus getAuthStatus() throws RtmServerException {
		if (StringUtils.isNotEmpty(settings.getFrob())) {
			return RtmAuthStatus.NEEDS_AUTH_TOKEN;
		}
		if (StringUtils.isEmpty(settings.getAuthToken()) || !checkToken()) {
			return RtmAuthStatus.NEEDS_USER_APPROVAL;
		}
		return RtmAuthStatus.AUTHORIZED;
	}
	
	public URL getAuthenticationUrl(final String perms) throws RtmServerException {
		try {
			// Call getFrob
			TreeMap<String, String> getFrobParams = new TreeMap<String, String>();
			getFrobParams.put("method", "rtm.auth.getFrob");
			Document response = parseXML(getRtmUri(REST_METHOD_PATH, getFrobParams));
			Node node = response.selectSingleNode("//rsp/frob");
			settings.setFrob(node.getText());
			
			// Generate url
			TreeMap<String, String> params = new TreeMap<String, String>();
			params.put("perms", perms);
			params.put("frob", settings.getFrob());
			return getRtmUri(REST_AUTH_PATH, params).toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException("Unable to get authentication url", e);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Unable to get authentication url", e);
		}
	}
	
	public void completeAuthentication() throws RtmServerException {
		if (settings.getFrob() == null) {
			throw new RuntimeException("Unable to complete authentication unless in NEEDS_AUTH_TOKEN status.");
		}
		try {
			TreeMap<String, String> params = new TreeMap<String, String>();
			params.put("method", "rtm.auth.getToken");
			params.put("frob", settings.getFrob());
			Document response = parseXML(getRtmUri(REST_METHOD_PATH, params));
			Node node = response.selectSingleNode("//auth/token");
			settings.setAuthToken(node.getText());
		} catch (URISyntaxException e) {
			throw new RuntimeException("Unable to complete authentication", e);
		} finally {
			settings.setFrob(null);
		}
	}
	
	public String getIdForListName(final String listName) throws RtmServerException {
		try {
			Document response = parseXML(getRtmMethodUri("rtm.lists.getList"));
			List<Node> listNodesList = response.selectNodes("//lists/list");
			for (Node listNode : listNodesList) {
				Node nameNode = listNode.selectSingleNode("@name");
				Node idNode = listNode.selectSingleNode("@id");
				if (nameNode.getText().equals(listName)) {
					return idNode.getText();
				}
			}
			throw new RuntimeException("Unable to find list named " + listName);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Unable to retrieve list of lists", e);
		}
	}
	
	/**
	 * Create a new timeline.
	 * 
	 * @return timeline id
	 * @throws RtmServerException
	 */
	public String createTimeline() throws RtmServerException {
		try {
			Document response = parseXML(getRtmMethodUri("rtm.timelines.create"));
			Node node = response.selectSingleNode("//timeline");
			return node.getText();
		} catch (URISyntaxException e) {
			throw new RuntimeException("Unable to create timeline", e);
		}
	}
	
	/**
	 * Add a new task.
	 * 
	 * @return task id
	 * @throws RtmServerException
	 */
	public String addTask(final String timelineId, final String listId, final TaskDto task) throws RtmServerException {
		try {
			TreeMap<String, String> params = new TreeMap<String, String>();
			params.put("timeline", timelineId);
			params.put("list_id", listId);
			params.put("name", task.getSmartAdd());
			params.put("parse", "1");
			Document response = parseXML(getRtmMethodUri("rtm.tasks.add", params));
			Node node = response.selectSingleNode("//list/taskseries/task/@id");
			return node.getText();
		} catch (URISyntaxException e) {
			throw new RuntimeException("Unable to add task", e);
		}
	}
	
	private boolean checkToken() throws RtmServerException {
		try {
			Document response = parseXML(getRtmMethodUri("rtm.auth.checkToken"));
			Node tokenNode = response.selectSingleNode("//auth/token");
			Node usernameNode = response.selectSingleNode("//auth/user/@username");
			System.out.println("Logged in as " + usernameNode.getText());
			if (tokenNode.getText().equals(settings.getAuthToken())) {
				return true;
			}
			return false;
		} catch (URISyntaxException e) {
			throw new RuntimeException("Unable to check token", e);
		}
	}

	private URI getRtmMethodUri(final String methodName) throws URISyntaxException {
		return getRtmMethodUri(methodName, new TreeMap<String, String>());
	}

	private URI getRtmMethodUri(final String methodName, final TreeMap<String, String> params) throws URISyntaxException {
		params.put("method", methodName);
		params.put("auth_token", settings.getAuthToken());
		return getRtmUri(REST_METHOD_PATH, params);
	}
	
	private URI getRtmUri(final String uriPath, final TreeMap<String, String> params) throws URISyntaxException {
		params.put("api_key", apiKey);
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(REST_HOST).setPath(uriPath);
		for (String key : params.keySet()) {
			builder.setParameter(key, params.get(key));
		}
		builder.setParameter("api_sig", getApiSig(builder.build()));
		return builder.build();
	}

	private Document parseXML(final URI uri) throws RtmServerException {
		SAXReader reader = new SAXReader();
		Document response;
		try {
			response = reader.read(uri.toURL());
			Node status = response.selectSingleNode("//rsp/@stat");
			if (status != null) {
				if (status.getText().equals("fail")) {
					System.out.println(response.asXML());
					Node errCode = response.selectSingleNode("//rsp/err/@code");
					Node errMessage = response.selectSingleNode("//rsp/err/@msg");
					throw new RtmServerException(Integer.valueOf(errCode.getText()), errMessage.getText());
				}
			}
			return response;
		} catch (MalformedURLException e) {
			throw new RuntimeException("A malformed URL was specified", e);
		} catch (DocumentException e) {
			throw new RuntimeException("There was a problem parsing the response from RTM", e);
		}
	}

	private String getApiSig(final URI uri) {
		byte[] thedigest = null;
		String rawQuery =  uri.getRawQuery();
		rawQuery = rawQuery.replaceAll("&", "");
		rawQuery = rawQuery.replaceAll("=", "");
		String rawString = sharedSecret + rawQuery;
		try {
			byte[] bytesOfMessage = rawString.toString().getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			thedigest = md.digest(bytesOfMessage);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Unable to create API signature", e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unable to create API signature", e);
		}
		return new String(Hex.encodeHex(thedigest));
	}
}
