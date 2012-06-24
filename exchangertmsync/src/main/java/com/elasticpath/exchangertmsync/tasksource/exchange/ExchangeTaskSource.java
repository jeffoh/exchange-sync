package com.elasticpath.exchangertmsync.tasksource.exchange;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import microsoft.exchange.webservices.data.BasePropertySet;
import microsoft.exchange.webservices.data.EventType;
import microsoft.exchange.webservices.data.ExchangeCredentials;
import microsoft.exchange.webservices.data.ExchangeService;
import microsoft.exchange.webservices.data.ExtendedProperty;
import microsoft.exchange.webservices.data.ExtendedPropertyDefinition;
import microsoft.exchange.webservices.data.FindItemsResults;
import microsoft.exchange.webservices.data.FolderId;
import microsoft.exchange.webservices.data.Item;
import microsoft.exchange.webservices.data.ItemEvent;
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

import com.elasticpath.exchangertmsync.TaskObserver;
import com.elasticpath.exchangertmsync.tasksource.exchange.dto.ExchangeTaskDto;

public class ExchangeTaskSource {
	private static final UUID PROPERTY_SET_TASK = UUID.fromString("00062003-0000-0000-C000-000000000046");
	private static final int PID_TAG_FLAG_STATUS = 0x1090; // http://msdn.microsoft.com/en-us/library/cc842307
	private static final int PID_TAG_TASK_DUE_DATE = 0x8105; // http://msdn.microsoft.com/en-us/library/cc839641
	
	private static final ExtendedPropertyDefinition PR_FLAG_STATUS = new ExtendedPropertyDefinition(PID_TAG_FLAG_STATUS, MapiPropertyType.Integer);
	private static final ExtendedPropertyDefinition PR_TASK_DUE_DATE = new ExtendedPropertyDefinition(PROPERTY_SET_TASK, PID_TAG_TASK_DUE_DATE, MapiPropertyType.SystemTime);

	private static final boolean ENABLE_DEBUGGING = false;

	private ExchangeService service;
	private ExchangeEventsHandler eventsHandler;

	public ExchangeTaskSource(String mailHost, String username, String password) throws Exception {
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
		List<FolderId> folderIds = new ArrayList<FolderId>();
		folderIds.add(new FolderId(WellKnownFolderName.Inbox));
		StreamingSubscription streamingsubscription = service.subscribeToStreamingNotifications(
				folderIds,
				EventType.Modified);

		StreamingSubscriptionConnection connection = new StreamingSubscriptionConnection(service, 1);
		connection.addSubscription(streamingsubscription);
		eventsHandler = new ExchangeEventsHandler();
		connection.addOnDisconnect(eventsHandler);
		connection.addOnNotificationEvent(eventsHandler);
		connection.addOnSubscriptionError(eventsHandler);
		connection.open();
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
		
		public void notifyTaskEventListeners(final ExchangeTaskDto task) {
			for (TaskObserver observer : taskEventObservers) {
				observer.taskChanged(task);
			}
		}
	}

	public Set<ExchangeTaskDto> getAllTasks() {
		Set<ExchangeTaskDto> results = new HashSet<ExchangeTaskDto>();
		try {
			// Take a look at http://blogs.planetsoftware.com.au/paul/archive/2010/05/20/exchange-web-services-ews-managed-api-ndash-part-2.aspx
			SearchFilterCollection searchFilterCollection = new SearchFilterCollection(LogicalOperator.Or);
			searchFilterCollection.add(new SearchFilter.IsEqualTo(PR_FLAG_STATUS, "1"));
			searchFilterCollection.add(new SearchFilter.IsEqualTo(PR_FLAG_STATUS, "2"));
			ItemView itemView = new ItemView(100);
			itemView.setPropertySet(createEmailPropertySet());
			FindItemsResults<Item> items = service.findItems(new FolderId(WellKnownFolderName.Inbox), searchFilterCollection, itemView);
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

	public ExchangeTaskDto convertExchangeEmailToTaskDto(Item email) throws ServiceLocalException {
		Integer flagValue = null;
		Date dueDate = null;
		for (ExtendedProperty extendedProperty : email.getExtendedProperties()) {
			if (extendedProperty.getPropertyDefinition().getTag() != null && extendedProperty.getPropertyDefinition().getTag() == 16) {
				flagValue = (Integer) extendedProperty.getValue();
			} else if (extendedProperty.getPropertyDefinition().getId() != null && extendedProperty.getPropertyDefinition().getId() == PID_TAG_TASK_DUE_DATE) {
				dueDate = (Date) extendedProperty.getValue();
			}
		}
		ExchangeTaskDto task = new ExchangeTaskDto();
		task.setExchangeId(email.getId().getUniqueId());
		task.setName(email.getSubject());
		if (flagValue != null && flagValue == 1) {
			task.setCompleted(true);
		}
		task.setDueDate(dueDate);
		return task;
	}

	private PropertySet createEmailPropertySet() {
		ExtendedPropertyDefinition[] extendedPropertyDefinitions = new ExtendedPropertyDefinition[] { PR_FLAG_STATUS, PR_TASK_DUE_DATE };
		return new PropertySet(BasePropertySet.FirstClassProperties, extendedPropertyDefinitions);
	}

	public void addTaskEventListener(final TaskObserver observer) {
		eventsHandler.addTaskEventListener(observer);
	}
}
