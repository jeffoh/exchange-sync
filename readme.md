Overview
========
Exchange Sync will read data from your Exchange account and export flagged emails (not Exchange tasks) to Remember The Milk,
and calendar appointments to Google Calendar.  The current version will not modify data in your Exchange account in any way.

Linux Usage Instructions
========================
1. Install Maven and Git: <code>sudo apt-get install maven git</code>
2. Download the source: <code>git clone https://github.com/gdenning/exchange-sync.git</code>
3. Change to the exchange-sync folder: <code>cd exchange-sync</code>
4. Set the permissions on the shell script: <code>sudo chmod u+x installEWSAPI.sh</code>
5. Install the EWS library: <code>./installEWSAPI.sh</code>
6. Modify exchangesync.properties as follows:
    - Set exchangeHost to the hostname you usually use to access Outlook Web Access.
    - Set exchangeDomain, exchangeUsername, exchangePassword to your Microsoft Exchange domain, username, and password.
    - Set rtmListName to the name of the Remember the Milk list that you want to export tasks to.
    - Set googleCalendarName to the name of the Google Calendar that you want to export appointments to.
7. Start the application: <code>mvn exec:java</code>
