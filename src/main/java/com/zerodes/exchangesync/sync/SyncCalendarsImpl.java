package com.zerodes.exchangesync.sync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zerodes.exchangesync.Pair;
import com.zerodes.exchangesync.calendarsource.CalendarSource;
import com.zerodes.exchangesync.dto.AppointmentDto;

public class SyncCalendarsImpl {
	
	private CalendarSource exchangeSource;
	private CalendarSource otherSource;

	public SyncCalendarsImpl(CalendarSource exchangeSource, CalendarSource otherSource) {
		this.exchangeSource = exchangeSource;
		this.otherSource = otherSource;
	}

	protected List<Pair<AppointmentDto, AppointmentDto>> generatePairs() {
		List<Pair<AppointmentDto, AppointmentDto>> results = new ArrayList<Pair<AppointmentDto, AppointmentDto>>();
		Collection<AppointmentDto> otherCalendarEntrys = otherSource.getAllAppointments();
		Collection<AppointmentDto> exchangeCalendarEntrys = exchangeSource.getAllAppointments();
		Map<String, AppointmentDto> otherCalendarEntrysMap = generateExchangeIdMap(otherCalendarEntrys);
		for (AppointmentDto exchangeCalendarEntry : exchangeCalendarEntrys) {
			results.add(generatePairForExchangeCalendarEntry(otherCalendarEntrysMap, exchangeCalendarEntry));
		}
		return results;
	}

	/**
	 * Take a matching exchange CalendarEntry and other CalendarEntry and determine what needs to be done to sync them.
	 *
	 * @param exchangeCalendarEntry Exchange CalendarEntry (or null if no matching CalendarEntry exists)
	 * @param otherCalendarEntry CalendarEntry from "other" data source (or null if no matching CalendarEntry exists)
	 */
	public void sync(AppointmentDto exchangeCalendarEntry, AppointmentDto otherCalendarEntry) {
		if (exchangeCalendarEntry != null && otherCalendarEntry == null) {
			otherSource.addAppointment(exchangeCalendarEntry);
		} else if (exchangeCalendarEntry == null && otherCalendarEntry != null && otherCalendarEntry.getExchangeId() != null) {
			otherSource.deleteAppointment(otherCalendarEntry);
		} else if (exchangeCalendarEntry != null && otherCalendarEntry != null) {
			if (exchangeCalendarEntry.getLastModified().after(otherCalendarEntry.getLastModified())) {
				// Exchange CalendarEntry has a more recent modified date, so modify other CalendarEntry
				otherSource.updateAppointment(exchangeCalendarEntry);
			} else {
				// Other CalendarEntry has a more recent modified date, so modify Exchange
			}
		}
	}

	public void syncAll() {
		// Generate matching pairs of CalendarEntrys
		List<Pair<AppointmentDto, AppointmentDto>> pairs = generatePairs();

		// Create/complete/delete as required
		for (Pair<AppointmentDto, AppointmentDto> pair : pairs) {
			sync(pair.getLeft(), pair.getRight());
		}
	}

	public Map<String, AppointmentDto> generateExchangeIdMap(Collection<AppointmentDto> CalendarEntrys) {
		Map<String, AppointmentDto> results = new HashMap<String, AppointmentDto>();
		for (AppointmentDto CalendarEntry : CalendarEntrys) {
			results.put(CalendarEntry.getExchangeId(), CalendarEntry);
		}
		return results;
	}

	public Pair<AppointmentDto, AppointmentDto> generatePairForExchangeCalendarEntry(Map<String, AppointmentDto> otherCalendarEntryIdMap, AppointmentDto exchangeCalendarEntry) {
		AppointmentDto otherCalendarEntry = otherCalendarEntryIdMap.get(exchangeCalendarEntry.getExchangeId());
		return new Pair<AppointmentDto, AppointmentDto>(exchangeCalendarEntry, otherCalendarEntry);
	}
}
