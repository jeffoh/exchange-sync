package com.zerodes.exchangesync.exchange;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import microsoft.exchange.webservices.data.BasePropertySet;
import microsoft.exchange.webservices.data.ConflictResolutionMode;
import microsoft.exchange.webservices.data.EventType;
import microsoft.exchange.webservices.data.ExchangeCredentials;
import microsoft.exchange.webservices.data.ExchangeService;
import microsoft.exchange.webservices.data.ExtendedProperty;
import microsoft.exchange.webservices.data.ExtendedPropertyDefinition;
import microsoft.exchange.webservices.data.FindFoldersResults;
import microsoft.exchange.webservices.data.FindItemsResults;
import microsoft.exchange.webservices.data.Folder;
import microsoft.exchange.webservices.data.FolderId;
import microsoft.exchange.webservices.data.FolderSchema;
import microsoft.exchange.webservices.data.FolderTraversal;
import microsoft.exchange.webservices.data.FolderView;
import microsoft.exchange.webservices.data.Item;
import microsoft.exchange.webservices.data.ItemEvent;
import microsoft.exchange.webservices.data.ItemId;
import microsoft.exchange.webservices.data.ItemView;
import microsoft.exchange.webservices.data.LogicalOperator;
import microsoft.exchange.webservices.data.MapiPropertyType;
import microsoft.exchange.webservices.data.NotificationEvent;
import microsoft.exchange.webservices.data.NotificationEventArgs;
import microsoft.exchange.webservices.data.PropertySet;
import microsoft.exchange.webservices.data.SearchFilter;
import microsoft.exchange.webservices.data.SearchFilter.SearchFilterCollection;
import microsoft.exchange.webservices.data.ServiceLocalException;
import microsoft.exchange.webservices.data.StreamingSubscription;
import microsoft.exchange.webservices.data.StreamingSubscriptionConnection;
import microsoft.exchange.webservices.data.StreamingSubscriptionConnection.INotificationEventDelegate;
import microsoft.exchange.webservices.data.StreamingSubscriptionConnection.ISubscriptionErrorDelegate;
import microsoft.exchange.webservices.data.SubscriptionErrorEventArgs;
import microsoft.exchange.webservices.data.WebCredentials;
import microsoft.exchange.webservices.data.WellKnownFolderName;

import com.zerodes.exchangesync.TaskObserver;
import com.zerodes.exchangesync.dto.TaskDto;
import com.zerodes.exchangesync.tasksource.TaskSource;

public class ExchangeSourceImpl implements TaskSource {
	private static final int MAX_EXCHANGE_TASKS = 1000;
	private static final int SUBSCRIPTION_TIMEOUT = 30; // in minutes
	private static final boolean ENABLE_DEBUGGING = false;

	private static final UUID PROPERTY_SET_TASK = UUID.fromString("00062003-0000-0000-C000-000000000046");
	private static final UUID PROPERTY_SET_COMMON = UUID.fromString("00062008-0000-0000-C000-000000000046");

	private static final int PID_TAG_FLAG_STATUS = 0x1090; // http://msdn.microsoft.com/en-us/library/cc842307
	private static final int PID_LID_FLAG_REQUEST = 0x8018; // http://msdn.microsoft.com/en-us/library/cc815496
	private static final int PID_LID_TODO_ORDINAL_DATE = 0x8021; // http://msdn.microsoft.com/en-us/library/cc842320
	private static final int PID_LID_TODO_SUB_ORDINAL = 0x8022; // http://msdn.microsoft.com/en-us/library/cc839908
	private static final int PID_LID_TASK_COMPLETE = 0x8023; // http://msdn.microsoft.com/en-us/library/cc839514
	private static final int PID_LID_TASK_STATUS = 0x8024; // http://msdn.microsoft.com/en-us/library/cc842120
	private static final int PID_LID_TODO_TITLE = 0x8025; // http://msdn.microsoft.com/en-us/library/cc842303
	private static final int PID_LID_TASK_START_DATE = 0x802A; // http://msdn.microsoft.com/en-us/library/cc815922
	private static final int PID_LID_TASK_DUE_DATE = 0x8105; // http://msdn.microsoft.com/en-us/library/cc839641
	private static final int PID_LID_TASK_DATE_COMPLETED = 0x810F; // http://msdn.microsoft.com/en-us/library/cc815753
	private static final int PID_LID_PERCENT_COMPLETE = 0x802F; // http://msdn.microsoft.com/en-us/library/cc839932
	private static final int PID_LID_TASK_MODE = 0x8161; // http://msdn.microsoft.com/en-us/library/cc765719

	private static final ExtendedPropertyDefinition PR_FLAG_STATUS = new ExtendedPropertyDefinition(PID_TAG_FLAG_STATUS, MapiPropertyType.Integer);
	private static final ExtendedPropertyDefinition PR_FLAG_REQUEST = new ExtendedPropertyDefinition(PROPERTY_SET_COMMON, PID_LID_FLAG_REQUEST, MapiPropertyType.String);
	private static final ExtendedPropertyDefinition PR_TODO_ORDINAL_DATE = new ExtendedPropertyDefinition(PROPERTY_SET_COMMON, PID_LID_TODO_ORDINAL_DATE, MapiPropertyType.SystemTime);
	private static final ExtendedPropertyDefinition PR_TODO_SUB_ORDINAL = new ExtendedPropertyDefinition(PROPERTY_SET_COMMON, PID_LID_TODO_SUB_ORDINAL, MapiPropertyType.String);
	private static final ExtendedPropertyDefinition PR_TASK_COMPLETE = new ExtendedPropertyDefinition(PROPERTY_SET_COMMON, PID_LID_TASK_COMPLETE, MapiPropertyType.Boolean);
	private static final ExtendedPropertyDefinition PR_TASK_STATUS = new ExtendedPropertyDefinition(PROPERTY_SET_COMMON, PID_LID_TASK_STATUS, MapiPropertyType.Integer);
	private static final ExtendedPropertyDefinition PR_TODO_TITLE = new ExtendedPropertyDefinition(PROPERTY_SET_COMMON, PID_LID_TODO_TITLE, MapiPropertyType.String);
	private static final ExtendedPropertyDefinition PR_TASK_START_DATE = new ExtendedPropertyDefinition(PROPERTY_SET_COMMON, PID_LID_TASK_START_DATE, MapiPropertyType.SystemTime);
	private static final ExtendedPropertyDefinition PR_TASK_DUE_DATE = new ExtendedPropertyDefinition(PROPERTY_SET_TASK, PID_LID_TASK_DUE_DATE, MapiPropertyType.SystemTime);
	private static final ExtendedPropertyDefinition PR_TASK_DATE_COMPLETED = new ExtendedPropertyDefinition(PROPERTY_SET_COMMON, PID_LID_TASK_DATE_COMPLETED, MapiPropertyType.SystemTime);
	private static final ExtendedPropertyDefinition PR_PERCENT_COMPLETE = new ExtendedPropertyDefinition(PROPERTY_SET_COMMON, PID_LID_PERCENT_COMPLETE, MapiPropertyType.Double);
	private static final ExtendedPropertyDefinition PR_TASK_MODE = new ExtendedPropertyDefinition(PROPERTY_SET_COMMON, PID_LID_TASK_MODE, MapiPropertyType.Integer);
	private static final ExtendedPropertyDefinition PR_ALL_FOLDERS = new ExtendedPropertyDefinition(13825, MapiPropertyType.Integer);

	private static final int PR_FLAG_STATUS_FOLLOWUP_COMPLETE = 1;
	private static final int PR_FLAG_STATUS_FOLLOWUP_FLAGGED = 2;

	private ExchangeService service;
	private ExchangeEventsHandler eventsHandler;

	public ExchangeSourceImpl(String mailHost, String username, String password) throws Exception {
		System.out.println("Connecting to Exchange (" + mailHost + ") as " + username + "...");

		ExchangeCredentials credentials = new WebCredentials(username, password);
		service = new ExchangeService();
		service.setCredentials(credentials);
		try {
			service.setUrl(new URI("https://" + mailHost + "/EWS/Exchange.asmx"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		service.setTraceEnabled(ENABLE_DEBUGGING);

		// Setup a streaming subscription
		// http://blogs.msdn.com/b/exchangedev/archive/2010/12/22/working-with-streaming-notifications-by-using-the-ews-managed-api.aspx
		StreamingSubscription streamingsubscription = service.subscribeToStreamingNotificationsOnAllFolders(EventType.Modified);
		StreamingSubscriptionConnection connection = new StreamingSubscriptionConnection(service, SUBSCRIPTION_TIMEOUT);
		connection.addSubscription(streamingsubscription);
		eventsHandler = new ExchangeEventsHandler();
		connection.addOnDisconnect(eventsHandler);
		connection.addOnNotificationEvent(eventsHandler);
		connection.addOnSubscriptionError(eventsHandler);
		connection.open();
		
		System.out.println("Connected to Exchange");
	}

	private class ExchangeEventsHandler implements ISubscriptionErrorDelegate, INotificationEventDelegate {

		private Set<TaskObserver> taskEventObservers = new HashSet<TaskObserver>();

		@Override
		public void subscriptionErrorDelegate(Object sender, SubscriptionErrorEventArgs args) {
			StreamingSubscriptionConnection connection = (StreamingSubscriptionConnection) sender;
			if (args.getException() != null) {
				args.getException().printStackTrace();
			} else {
				try {
					System.out.println("Reconnected to Exchange streaming service.");
					connection.open();
				} catch (ServiceLocalException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void notificationEventDelegate(Object sender, NotificationEventArgs args) {
			try {
				for (NotificationEvent event : args.getEvents()) {
					if (event instanceof ItemEvent) {
						ItemEvent itemEvent = (ItemEvent) event;
						Item email = Item.bind(service, itemEvent.getItemId(), createEmailPropertySet());
						if (email != null) {
							notifyTaskEventListeners(convertExchangeEmailToTaskDto(email));
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void addTaskEventListener(final TaskObserver observer) {
			taskEventObservers.add(observer);
		}

		public void notifyTaskEventListeners(final TaskDto task) {
			for (TaskObserver observer : taskEventObservers) {
				observer.taskChanged(task);
			}
		}
	}

	@Override
	public void addTask(TaskDto task) {
		throw new UnsupportedOperationException("Unable to add new tasks to Exchange");
	}

	@Override
	public Set<TaskDto> getAllTasks() {
		Set<TaskDto> results = new HashSet<TaskDto>();
		try {
			// Take a look at http://blogs.planetsoftware.com.au/paul/archive/2010/05/20/exchange-web-services-ews-managed-api-ndash-part-2.aspx
			SearchFilterCollection searchFilterCollection = new SearchFilterCollection(LogicalOperator.Or);
			searchFilterCollection.add(new SearchFilter.IsEqualTo(PR_FLAG_STATUS, "1"));
			searchFilterCollection.add(new SearchFilter.IsEqualTo(PR_FLAG_STATUS, "2"));
			ItemView itemView = new ItemView(MAX_EXCHANGE_TASKS);
			itemView.setPropertySet(createEmailPropertySet());
			FindItemsResults<Item> items = getAllItemsFolder().findItems(searchFilterCollection, itemView);
			for (Item email : items.getItems()) {

				results.add(convertExchangeEmailToTaskDto(email));
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}

	private Folder getAllItemsFolder() throws Exception {
		FolderId rootFolderId = new FolderId(WellKnownFolderName.Root);
		FolderView folderView = new FolderView(1000);
		folderView.setTraversal(FolderTraversal.Shallow);

		SearchFilter searchFilter1 = new SearchFilter.IsEqualTo(PR_ALL_FOLDERS, "2");
		SearchFilter searchFilter2 = new SearchFilter.IsEqualTo(FolderSchema.DisplayName, "allitems");
		SearchFilter.SearchFilterCollection searchFilterCollection = new SearchFilter.SearchFilterCollection(LogicalOperator.And);
		searchFilterCollection.add(searchFilter1);
		searchFilterCollection.add(searchFilter2);

		FindFoldersResults findFoldersResults = service.findFolders(
				rootFolderId, searchFilterCollection, folderView);

		if (findFoldersResults.getFolders().size() == 0) {
			return null;
		}
		return findFoldersResults.getFolders().iterator().next();
	}

	public TaskDto convertExchangeEmailToTaskDto(Item email) throws ServiceLocalException {
		Integer flagValue = null;
		Date dueDate = null;
		for (ExtendedProperty extendedProperty : email.getExtendedProperties()) {
			if (extendedProperty.getPropertyDefinition().getTag() != null && extendedProperty.getPropertyDefinition().getTag() == 16) {
				flagValue = (Integer) extendedProperty.getValue();
			} else if (extendedProperty.getPropertyDefinition().getId() != null && extendedProperty.getPropertyDefinition().getId() == PID_LID_TASK_DUE_DATE) {
				dueDate = (Date) extendedProperty.getValue();
			}
		}
		TaskDto task = new TaskDto();
		task.setExchangeId(email.getId().getUniqueId());
		task.setLastModified(getCorrectedExchangeTime(email.getLastModifiedTime()));
		task.setName(email.getSubject());
		if (flagValue == null) {
			throw new RuntimeException("Found email without follow-up flag!");
		} else if (flagValue == PR_FLAG_STATUS_FOLLOWUP_COMPLETE) {
			task.setCompleted(true);
		}
		task.setDueDate(dueDate);
		return task;
	}

	/**
	 * There is a bug in the Java EWS in which time is returned in GMT but with local timezone.
	 * This function fixes those times.
	 *
	 * @param theDate the date returned from EWS
	 * @return theDate converted to local time
	 */
	private Date getCorrectedExchangeTime(Date theDate) {
		TimeZone tz = Calendar.getInstance().getTimeZone();

		long msFromEpochGmt = theDate.getTime();

		// gives you the current offset in ms from GMT at the current date
		int offsetFromUTC = tz.getOffset(msFromEpochGmt);

		// create a new calendar in GMT timezone, set to this date and add the
		// offset
		Calendar newTime = Calendar.getInstance();
		newTime.setTime(theDate);
		newTime.add(Calendar.MILLISECOND, offsetFromUTC);
		return newTime.getTime();
	}

	private PropertySet createEmailPropertySet() {
		ExtendedPropertyDefinition[] extendedPropertyDefinitions = new ExtendedPropertyDefinition[] { PR_FLAG_STATUS, PR_TASK_DUE_DATE };
		return new PropertySet(BasePropertySet.FirstClassProperties, extendedPropertyDefinitions);
	}

	public void addTaskEventListener(final TaskObserver observer) {
		eventsHandler.addTaskEventListener(observer);
	}

	@Override
	public void updateDueDate(final TaskDto task) {
		try {
			ItemId itemId = new ItemId(task.getExchangeId());
			Item email = Item.bind(service, itemId, createEmailPropertySet());
			if (task.getDueDate() == null) {
				email.removeExtendedProperty(PR_TASK_DUE_DATE);
			} else {
				email.setExtendedProperty(PR_TASK_DUE_DATE, task.getDueDate());
			}
			email.update(ConflictResolutionMode.AlwaysOverwrite);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void updateCompletedFlag(final TaskDto task) {
		try {
			ItemId itemId = new ItemId(task.getExchangeId());
			Item email = Item.bind(service, itemId, createEmailPropertySet());
			email.setExtendedProperty(PR_TODO_TITLE, task.getName());
			email.setExtendedProperty(PR_TASK_MODE, 0); // Task is not assigned
			if (task.isCompleted()) {
				email.removeExtendedProperty(PR_FLAG_REQUEST);
				email.setExtendedProperty(PR_TASK_COMPLETE, true);
				email.setExtendedProperty(PR_PERCENT_COMPLETE, 1d);
				email.setExtendedProperty(PR_TASK_DATE_COMPLETED, new Date());
				email.setExtendedProperty(PR_TASK_STATUS, 2); // User's work on this task is complete
				email.setExtendedProperty(PR_FLAG_STATUS, PR_FLAG_STATUS_FOLLOWUP_COMPLETE);
			} else {
				email.setExtendedProperty(PR_TASK_START_DATE, new Date());
				email.setExtendedProperty(PR_FLAG_REQUEST, "Follow up");
				email.setExtendedProperty(PR_TODO_ORDINAL_DATE, new Date());
				email.setExtendedProperty(PR_TODO_SUB_ORDINAL, "5555555");
				email.setExtendedProperty(PR_TASK_COMPLETE, false);
				email.setExtendedProperty(PR_PERCENT_COMPLETE, 0d);
				email.removeExtendedProperty(PR_TASK_DATE_COMPLETED);
				email.setExtendedProperty(PR_TASK_STATUS, 0);
				email.setExtendedProperty(PR_FLAG_STATUS, PR_FLAG_STATUS_FOLLOWUP_FLAGGED);
			}
			email.update(ConflictResolutionMode.AlwaysOverwrite);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String convertDateToString(Date theDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.format(theDate) + "T23:00:00Z";
	}
}
