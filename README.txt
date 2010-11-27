= WebSocketRemote

WebsocketRemote is a remote control application that looks like VNC.
Only web browser(supported WebSocket) is necessary, and plug-in and applet are unnecessary in client side.

== DOWNLOAD

from http://github.com/Kanasansoft/WebSocketRemote/downloads

== RUN

Double click WebSocketRemote executable file or run command on terminal on server side.

executable file(x.x.x is version number)
* for Windows:WebSocketRemote-x.x.x.exe.
* for Macintosh:WebSocketRemote-x.x.x.app.

command(x.x.x is version number)

% java -jar WebSocketRemote-x.x.x.jar 

Running sign is display WebSocketRemote icon.

* for Windows:in task tray
* for Macintosh:in menu bar
* for Linux:in menu bar

== HOW TO USE

Access "http:[server address]:40320/" by Web Browser from client side.
Access "http:[server address]:40320/ios.html" by Web Browser from iOS(>=v4.2.1).

== MODE for iOS

Access "http:[server address]:40320/ios.html?mode=remote" by remote mode. (default)
Access "http:[server address]:40320/ios.html?mode=mouse" by mouse mode. (not receive capture image)

== QUIT

Right click WebSocketRemote icon.
Select Quit.

== REQUIREMENTS

=== SERVER SIDE

Java SE 6

=== CLIENT SITE

Web Browser(supported WebSocket)

== NOTE

Current version is supporting mouse event only.
Current version is cannot change port number.
