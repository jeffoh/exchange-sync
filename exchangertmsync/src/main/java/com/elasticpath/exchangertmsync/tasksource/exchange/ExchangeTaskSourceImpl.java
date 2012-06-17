package com.elasticpath.exchangertmsync.tasksource.exchange;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Observer;
import java.util.Set;

import microsoft.exchange.webservices.data.ExchangeCredentials;
import microsoft.exchange.webservices.data.ExchangeService;
import microsoft.exchange.webservices.data.ExtendedPropertyDefinition;
import microsoft.exchange.webservices.data.FindItemsResults;
import microsoft.exchange.webservices.data.FolderId;
import microsoft.exchange.webservices.data.Item;
import microsoft.exchange.webservices.data.ItemView;
import microsoft.exchange.webservices.data.MapiPropertyType;
import microsoft.exchange.webservices.data.SearchFilter;
import microsoft.exchange.webservices.data.WebCredentials;
import microsoft.exchange.webservices.data.WellKnownFolderName;

import com.elasticpath.exchangertmsync.tasksource.TaskDto;
import com.elasticpath.exchangertmsync.tasksource.TaskSource;

public class ExchangeTaskSourceImpl implements TaskSource {
	private String mailHost;
	private String username;
	private String password;
	
	public ExchangeTaskSourceImpl(String mailHost, String username, String password) {
		this.mailHost = mailHost;
		this.username = username;
		this.password = password;
	}

	@Override
	public Set<TaskDto> getAllTasks() {
		Set<TaskDto> results = new HashSet<TaskDto>();
		try {
			ExchangeCredentials credentials = new WebCredentials(username, password);
			ExchangeService service = new ExchangeService();
			service.setCredentials(credentials);
			service.setUrl(new URI("https://" + mailHost + "/EWS/Exchange.asmx"));
			service.setTraceEnabled(true);
			ExtendedPropertyDefinition PR_FLAG_STATUS = new ExtendedPropertyDefinition(4240, MapiPropertyType.Integer);
			SearchFilter sf = new SearchFilter.IsEqualTo(PR_FLAG_STATUS, "2");
			FindItemsResults<Item> items = service.findItems(new FolderId(WellKnownFolderName.Inbox), sf, new ItemView(10));
			for (Item email : items.getItems()) {
				TaskDto task = new TaskDto();
				task.setOtherId(email.getId().getUniqueId());
				task.setName(email.getSubject());
				results.add(task);
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}

	@Override
	public void addTaskAddedListener(Observer observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addTaskCompletedListener(Observer observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addTaskDeletedListener(Observer observer) {
		// TODO Auto-generated method stub

	}

}
