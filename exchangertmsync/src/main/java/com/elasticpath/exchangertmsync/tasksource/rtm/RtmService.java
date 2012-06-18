package com.elasticpath.exchangertmsync.tasksource.rtm;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
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
			Node node = response.selectSingleNode("/rsp/frob");
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
			Node node = response.selectSingleNode("/rsp/auth/token");
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
			List<Node> listNodesList = response.selectNodes("/rsp/lists/list");
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
			Node node = response.selectSingleNode("/rsp/timeline");
			return node.getText();
		} catch (URISyntaxException e) {
			throw new RuntimeException("Unable to create timeline", e);
		}
	}

	/**
	 * Add a new task.
	 *
	 * @throws RtmServerException
	 */
	public void addTask(final String timelineId, final String listId, final TaskDto task) throws RtmServerException {
		try {
			// Add task
			TreeMap<String, String> addTaskParams = new TreeMap<String, String>();
			addTaskParams.put("timeline", timelineId);
			addTaskParams.put("list_id", listId);
			addTaskParams.put("name", task.getName());
			Document response = parseXML(getRtmMethodUri("rtm.tasks.add", addTaskParams));
			System.out.println(response.asXML());
			Node idNode = response.selectSingleNode("/rsp/list/taskseries/task/@id");
			task.setRtmTaskId(idNode.getText());
			Node taskSeriesIdNode = response.selectSingleNode("/rsp/list/taskseries/@id");
			task.setRtmTimeSeriesId(taskSeriesIdNode.getText());

			// Set due date (if required)
			if (task.getDueDate() != null) {
				TreeMap<String, String> setDueDateParams = new TreeMap<String, String>();
				setDueDateParams.put("task_id", task.getRtmTaskId());
				setDueDateParams.put("taskseries_id", task.getRtmTimeSeriesId());
				setDueDateParams.put("timeline", timelineId);
				setDueDateParams.put("list_id", listId);
				setDueDateParams.put("due", DateFormatUtils.format(task.getDueDate(), "yyyy-MM-dd") + "T00:00:00Z");
				setDueDateParams.put("has_due_time", "0");
				parseXML(getRtmMethodUri("rtm.tasks.setDueDate", setDueDateParams));
			}

			// Set completed (if required)
			if (task.isCompleted()) {
				completeTask(timelineId, listId, task);
			}
		} catch (URISyntaxException e) {
			throw new RuntimeException("Unable to add task", e);
		}
	}

	/**
	 * Mark a task as completed
	 *
	 * @throws RtmServerException
	 */
	public void completeTask(final String timelineId, final String listId, final TaskDto task) throws RtmServerException {
		try {
			TreeMap<String, String> setCompletedParams = new TreeMap<String, String>();
			setCompletedParams.put("task_id", task.getRtmTaskId());
			setCompletedParams.put("taskseries_id ", task.getRtmTimeSeriesId());
			setCompletedParams.put("timeline", timelineId);
			setCompletedParams.put("list_id", listId);
			parseXML(getRtmMethodUri("rtm.tasks.complete", setCompletedParams));
		} catch (URISyntaxException e) {
			throw new RuntimeException("Unable to add task", e);
		}
	}

	/**
	 * Retrieve a list of tasks in a specific list.
	 *
	 * @param listId the list from which to retrieve tasks
	 * @return a list of tasks
	 * @throws RtmServerException
	 */
	public List<TaskDto> getTasks(final String listId) throws RtmServerException {
		try {
			List<TaskDto> results = new ArrayList<TaskDto>();
			TreeMap<String, String> params = new TreeMap<String, String>();
			params.put("list_id", listId);
			Document response = parseXML(getRtmMethodUri("rtm.tasks.getList", params));
			List<Node> taskSeriesNodesList = response.selectNodes("/rsp/tasks/list/taskseries");
			for (Node taskSeriesNode : taskSeriesNodesList) {
				Node timeSeriesIdNode = taskSeriesNode.selectSingleNode("@id");
				Node nameNode = taskSeriesNode.selectSingleNode("@name");
				Node idNode = taskSeriesNode.selectSingleNode("task/@id");
				Node completedNode = taskSeriesNode.selectSingleNode("task/@completed");
				TaskDto task = new TaskDto();
				task.setRtmTaskId(idNode.getText());
				task.setRtmTimeSeriesId(timeSeriesIdNode.getText());
				task.setName(nameNode.getText());
				task.setCompleted(StringUtils.isNotEmpty(completedNode.getText()));
				results.add(task);
			}
			return results;
		} catch (URISyntaxException e) {
			throw new RuntimeException("Unable to add task", e);
		}
	}

	private boolean checkToken() throws RtmServerException {
		try {
			Document response = parseXML(getRtmMethodUri("rtm.auth.checkToken"));
			Node tokenNode = response.selectSingleNode("/rsp/auth/token");
			Node usernameNode = response.selectSingleNode("/rsp/auth/user/@username");
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
			// Remove characters that RTM doesn't seem to support
			params.put(key, params.get(key).replaceAll("[^\\w\\s\\.]", ""));
			builder.setParameter(key, params.get(key));
		}
		builder.setParameter("api_sig", getApiSig(params));
		return builder.build();
	}

	private Document parseXML(final URI uri) throws RtmServerException {
		SAXReader reader = new SAXReader();
		Document response;
		try {
			response = reader.read(uri.toURL());
			Node status = response.selectSingleNode("/rsp/@stat");
			if (status != null) {
				if (status.getText().equals("fail")) {
					System.out.println("REQUEST: " + uri.toString());
					System.out.println("RESPONSE: " + response.asXML());
					Node errCode = response.selectSingleNode("/rsp/err/@code");
					Node errMessage = response.selectSingleNode("/rsp/err/@msg");
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

	private String getApiSig(final TreeMap<String, String> params) {
		byte[] thedigest = null;
		StringBuffer rawString = new StringBuffer(sharedSecret);
		for (String key : params.keySet()) {
			rawString.append(key);
			rawString.append(params.get(key));
		}
//		System.out.println("API_SIG RAW: " + rawString);
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
