Overview
========
Exchange Sync will read data from your Exchange account and export flagged emails (not Exchange tasks) to Remember The Milk,
and calendar appointments to Google Calendar. It will not modify data in your Exchange account in any way. Google has a similar
service for Windows users called Google Calendar Sync. This application can be considered Google Calendar Sync for Linux
and Mac (but will also work on Windows), with the additional Remember The Milk functionality.

Linux Usage Instructions
========================
1. Install Maven and Git: <code>sudo apt-get install maven git</code>
2. Change to your home folder: <code>cd ~</code>
3. Download the source: <code>git clone https://github.com/gdenning/exchange-sync.git</code>
4. Change to the exchange-sync folder: <code>cd exchange-sync</code>
5. Set the permissions on the shell script: <code>sudo chmod u+x installEWSAPI.sh</code>
6. Install the EWS library: <code>./installEWSAPI.sh</code>
7. Modify exchangesync.properties as follows:
    - Set exchangeHost to the hostname you usually use to access Outlook Web Access.
    - Set exchangeDomain, exchangeUsername, exchangePassword to your Microsoft Exchange domain, username, and password.
    - Set rtmListName to the name of the Remember the Milk list that you want to export tasks to.
    - Set googleCalendarName to the name of the Google Calendar that you want to export appointments to.
8. Copy exchangesync.properties to your home folder: <code>cp exchangesync.properties ~/exchangesync.properties</code>
9. Compile the application: <code>mvn install</code>
10. Create a symlink to the application in your home directory: <code>ln -s ~/exchangesync.jar ~/exchange-sync/target/exchangesync-0.0.1-SNAPSHOT-jar-with-dependencies.jar</code>
11. Add the following line to your /etc/crontab file: <code>*/5 *   * * *   user   java -jar ~/exchangesync.jar > ~/exchangesync.log 2>&1</code> (You will need to change "user" to your username.)
