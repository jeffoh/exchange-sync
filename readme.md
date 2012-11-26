Overview
========
Exchange Sync will read data from your Exchange account and export flagged emails (not Exchange tasks) to Remember The Milk,
and calendar appointments to Google Calendar.  The current version will not modify data in your Exchange account in any way.

Linux Usage Instructions
========================
1. Install Maven and Git: <code>sudo apt-get install maven git</code>
2. Download the source: <code>git clone https://github.com/gdenning/exchange-sync.git</code>
3. Change to the exchange-sync folder: <code>cd exchange-sync</code>
2. Modify exchangesync.properties as follows:
    - Set exchangeHost to the hostname you usually use to access Outlook Web Access.
    - Set exchangeUsername and exchangePassword to your domain username and password.
    - Set rtmListName to the name of the Remember the Milk list that you want to export tasks to.
    - Set googleCalendarName to the name of the Google Calendar that you want to export appointments to.
3. Start the application: <code>mvn exec:java</code>
