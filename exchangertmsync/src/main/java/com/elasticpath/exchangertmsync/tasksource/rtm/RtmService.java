package com.elasticpath.exchangertmsync.tasksource.rtm;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.elasticpath.exchangertmsync.tasksource.rtm.dto.NoteDto;
import com.elasticpath.exchangertmsync.tasksource.rtm.dto.TaskDto;

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
		} catch (UnsupportedEncodingException e) {
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
		} catch (UnsupportedEncodingException e) {
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
		} catch (UnsupportedEncodingException e) {
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
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unable to create timeline", e);
		}
	}

	/**
	 * Add a new task.
	 * 
	 * @param timelineId
	 * @param listId
	 * @param task
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
			Node idNode = response.selectSingleNode("/rsp/list/taskseries/task/@id");
			task.setRtmTaskId(idNode.getText());
			Node taskSeriesIdNode = response.selectSingleNode("/rsp/list/taskseries/@id");
			task.setRtmTimeSeriesId(taskSeriesIdNode.getText());
			
			// Set due date (if required)
			if (task.getDueDate() != null) {
				updateDueDate(timelineId, listId, task);
			}
			
			// Set completed (if required)
			if (task.isCompleted()) {
				completeTask(timelineId, listId, task);
			}
			
			// Add tags (if required)
			if (!task.getTags().isEmpty()) {
				addTags(timelineId, listId, task, task.getTags());
			}
			
			// Add notes (if required)
			if (!task.getNotes().isEmpty()) {
				for (final NoteDto note : task.getNotes()) {
					addNote(timelineId, listId, task, note);
				}
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unable to add task", e);
		}
	}

	/**
	 * Update a task's due date.
	 * 
	 * @param timelineId
	 * @param listId
	 * @param task
	 * @throws RtmServerException
	 * @throws UnsupportedEncodingException
	 */
	public void updateDueDate(final String timelineId, final String listId,
			final TaskDto task) throws RtmServerException,
			UnsupportedEncodingException {
		TreeMap<String, String> setDueDateParams = new TreeMap<String, String>();
		setDueDateParams.put("task_id", task.getRtmTaskId());
		setDueDateParams.put("taskseries_id", task.getRtmTimeSeriesId());
		setDueDateParams.put("timeline", timelineId);
		setDueDateParams.put("list_id", listId);
		setDueDateParams.put("due", DateFormatUtils.format(task.getDueDate(), "yyyy-MM-dd") + "T00:00:00Z");
		parseXML(getRtmMethodUri("rtm.tasks.setDueDate", setDueDateParams));
	}

	/**
	 * Add tags to a task.
	 * 
	 * @param timelineId
	 * @param listId
	 * @param task
	 * @throws RtmServerException
	 * @throws UnsupportedEncodingException
	 */
	public void addTags(final String timelineId, final String listId,
			final TaskDto task, final Set<String> tags) throws RtmServerException,
			UnsupportedEncodingException {
		TreeMap<String, String> addTagsParams = new TreeMap<String, String>();
		addTagsParams.put("task_id", task.getRtmTaskId());
		addTagsParams.put("taskseries_id", task.getRtmTimeSeriesId());
		addTagsParams.put("timeline", timelineId);
		addTagsParams.put("list_id", listId);
		addTagsParams.put("tags", StringUtils.join(tags, ","));
		parseXML(getRtmMethodUri("rtm.tasks.addTags", addTagsParams));
	}

	/**
	 * Add a note to a task.
	 * 
	 * @param timelineId
	 * @param listId
	 * @param task
	 * @throws RtmServerException
	 * @throws UnsupportedEncodingException
	 */
	public void addNote(final String timelineId, final String listId,
			final TaskDto task, final NoteDto note) throws RtmServerException,
			UnsupportedEncodingException {
		TreeMap<String, String> addNoteParams = new TreeMap<String, String>();
		addNoteParams.put("task_id", task.getRtmTaskId());
		addNoteParams.put("taskseries_id", task.getRtmTimeSeriesId());
		addNoteParams.put("timeline", timelineId);
		addNoteParams.put("list_id", listId);
		addNoteParams.put("note_title", note.getTitle());
		addNoteParams.put("note_text", note.getBody());
		parseXML(getRtmMethodUri("rtm.tasks.notes.add", addNoteParams));
	}

	/**
	 * Mark a task as completed.
	 * 
	 * @param timelineId
	 * @param listId
	 * @param task
	 * @throws RtmServerException
	 */
	public void completeTask(final String timelineId, final String listId, final TaskDto task) throws RtmServerException {
		try {
			TreeMap<String, String> setCompletedParams = new TreeMap<String, String>();
			setCompletedParams.put("task_id", task.getRtmTaskId());
			setCompletedParams.put("taskseries_id", task.getRtmTimeSeriesId());
			setCompletedParams.put("timeline", timelineId);
			setCompletedParams.put("list_id", listId);
			parseXML(getRtmMethodUri("rtm.tasks.complete", setCompletedParams));
		} catch (UnsupportedEncodingException e) {
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
				List<Node> tagNodes = taskSeriesNode.selectNodes("tags/tag");
				for (Node tagNode : tagNodes) {
					task.addTag(tagNode.getText());
				}
				List<Node> noteNodes = taskSeriesNode.selectNodes("notes/note");
				for (Node noteNode : noteNodes) {
					NoteDto note = new NoteDto();
					note.setTitle(noteNode.selectSingleNode("@title").getText());
					note.setBody(noteNode.getText());
					task.addNote(note);
				}
				results.add(task);
			}
			return results;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unable to add task", e);
		}
	}

	private boolean checkToken() throws RtmServerException {
		try {
			Document response = parseXML(getRtmMethodUri("rtm.auth.checkToken"));
			Node tokenNode = response.selectSingleNode("/rsp/auth/token");
			Node usernameNode = response.selectSingleNode("/rsp/auth/user/@username");
			System.out.println("Logged in to RTM as " + usernameNode.getText());
			if (tokenNode.getText().equals(settings.getAuthToken())) {
				return true;
			}
			return false;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unable to check token", e);
		}
	}

	private URI getRtmMethodUri(final String methodName) throws UnsupportedEncodingException {
		return getRtmMethodUri(methodName, new TreeMap<String, String>());
	}

	private URI getRtmMethodUri(final String methodName, final TreeMap<String, String> params) throws UnsupportedEncodingException {
		params.put("method", methodName);
		params.put("auth_token", settings.getAuthToken());
		return getRtmUri(REST_METHOD_PATH, params);
	}

	private URI getRtmUri(final String uriPath, final TreeMap<String, String> params) throws UnsupportedEncodingException {
		params.put("api_key", apiKey);
		StringBuffer uriString = new StringBuffer("http://" + REST_HOST + uriPath + "?");
		for (String key : params.keySet()) {
			uriString.append(key).append("=").append(URLEncoder.encode(params.get(key), "UTF-8")).append("&");
		}
		uriString.append("api_sig").append("=").append(getApiSig(params));
		return URI.create(uriString.toString());
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
		StringBuffer rawString = new StringBuffer(sharedSecret);
		for (String key : params.keySet()) {
			rawString.append(key);
			rawString.append(params.get(key));
		}
//		System.out.println("API_SIG RAW: " + rawString);
		try {
			byte[] bytesOfMessage = rawString.toString().getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] thedigest = md.digest(bytesOfMessage);
			return new String(Hex.encodeHex(thedigest));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Unable to create API signature", e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unable to create API signature", e);
		}
	}
}
