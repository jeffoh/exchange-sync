package com.elasticpath.exchangertmsync.tasksource.exchange;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashSet;
import java.util.Observer;
import java.util.Set;
import java.util.UUID;

import microsoft.exchange.webservices.data.BasePropertySet;
import microsoft.exchange.webservices.data.ExchangeCredentials;
import microsoft.exchange.webservices.data.ExchangeService;
import microsoft.exchange.webservices.data.ExtendedProperty;
import microsoft.exchange.webservices.data.ExtendedPropertyDefinition;
import microsoft.exchange.webservices.data.FindItemsResults;
import microsoft.exchange.webservices.data.FolderId;
import microsoft.exchange.webservices.data.Item;
import microsoft.exchange.webservices.data.ItemView;
import microsoft.exchange.webservices.data.LogicalOperator;
import microsoft.exchange.webservices.data.MapiPropertyType;
import microsoft.exchange.webservices.data.PropertySet;
import microsoft.exchange.webservices.data.SearchFilter;
import microsoft.exchange.webservices.data.SearchFilter.SearchFilterCollection;
import microsoft.exchange.webservices.data.WebCredentials;
import microsoft.exchange.webservices.data.WellKnownFolderName;

import com.elasticpath.exchangertmsync.tasksource.exchange.dto.ExchangeTaskDto;

public class ExchangeTaskSource {
	private static final int PID_TAG_FLAG_STATUS = 0x1090; // http://msdn.microsoft.com/en-us/library/cc842307
	private static final int PID_TAG_TASK_DUE_DATE = 0x8105; // http://msdn.microsoft.com/en-us/library/cc839641
	private static final UUID PROPERTY_SET_TASK = UUID.fromString("00062003-0000-0000-C000-000000000046");

	private String mailHost;
	private String username;
	private String password;

	public ExchangeTaskSource(String mailHost, String username, String password) {
		this.mailHost = mailHost;
		this.username = username;
		this.password = password;
	}

	public Set<ExchangeTaskDto> getAllTasks() {
		Set<ExchangeTaskDto> results = new HashSet<ExchangeTaskDto>();
		try {
			ExchangeCredentials credentials = new WebCredentials(username, password);
			ExchangeService service = new ExchangeService();
			service.setCredentials(credentials);
			service.setUrl(new URI("https://" + mailHost + "/EWS/Exchange.asmx"));
//			service.setTraceEnabled(true);
			// Take a look at http://blogs.planetsoftware.com.au/paul/archive/2010/05/20/exchange-web-services-ews-managed-api-ndash-part-2.aspx
			ExtendedPropertyDefinition PR_FLAG_STATUS = new ExtendedPropertyDefinition(PID_TAG_FLAG_STATUS, MapiPropertyType.Integer);
			ExtendedPropertyDefinition PR_TASK_DUE_DATE = new ExtendedPropertyDefinition(PROPERTY_SET_TASK, PID_TAG_TASK_DUE_DATE, MapiPropertyType.SystemTime); // Tag num: 0x802C0040
			SearchFilterCollection searchFilterCollection = new SearchFilterCollection(LogicalOperator.Or);
			searchFilterCollection.add(new SearchFilter.IsEqualTo(PR_FLAG_STATUS, "1"));
			searchFilterCollection.add(new SearchFilter.IsEqualTo(PR_FLAG_STATUS, "2"));
			ItemView itemView = createItemView(100, PR_FLAG_STATUS, PR_TASK_DUE_DATE);
			FindItemsResults<Item> items = service.findItems(new FolderId(WellKnownFolderName.Inbox), searchFilterCollection, itemView);
			for (Item email : items.getItems()) {
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
				results.add(task);
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}

	private ItemView createItemView(int maxResults, ExtendedPropertyDefinition... extendedFields) {
		ItemView itemView = new ItemView(maxResults);
		PropertySet extendedPropertySet = new PropertySet(BasePropertySet.FirstClassProperties, extendedFields); //BasePropertySet.IdOnly?
		itemView.setPropertySet(extendedPropertySet);
		return itemView;
	}

	public void addTaskAddedListener(Observer observer) {
		// TODO Auto-generated method stub

	}

	public void addTaskCompletedListener(Observer observer) {
		// TODO Auto-generated method stub

	}

	public void addTaskDeletedListener(Observer observer) {
		// TODO Auto-generated method stub

	}

}
