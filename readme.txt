Copyright (c) 2018-2021 Advanced Realtime Tracking GmbH & Co. KG


License
-------

This library is distributed under the BSD 3-clause License. 
You can modify the sources and/or include them into own software (for details see 
'license.txt').


Purpose of DTrackSDK
--------------------

A set of functions to provide an interface to DTrack tracking systems.
The functions receive and process DTrack measurement data packets (UDP; ASCII),
and send/exchange DTrack2/DTrack3 command strings (UDP/TCP; ASCII).


How to receive and process DTrack tracking data
-----------------------------------------------

DTrack uses Ethernet (UDP/IP datagrams) to send measurement data to other
applications. It uses an ASCII data format.
In its most simple operating mode DTrackSDK is just receiving and processing these data. In
this case DTrackSDK just needs to know the port number where the data are arriving; all necessary
settings have to be done manually in the DTrack frontend software.

DTrack2/DTrack3 also provides a way to control the tracking system through a command interface via
ethernet. DTrack2/DTrack3 uses ASCII command strings which are sent via a TCP/IP connection.

The formats and all other necessary definitions are described in
'DTrack2 User Manual: Technical Appendix' or 'DTrack3 Programmer's Guide'.


Sample source codes for an own interface
----------------------------------------

The sample source code files show how to use the DTrackSDK:

  Listening:           pure listening for measurement data
  ListeningMulticast:  multicast listening for measurement data
  Communicating:       additional controlling the tracking system through remote commands
  TactileFlystick:     controlling a tactile FINGERTRACKING device using a Flystick
  FlystickFeedback:    controlling a Flystick with feedback


Source Documentation
--------------------

Please refer to the Javadoc located at ./doc/index.html
  

Development with DTrackSDK
--------------------------

Create a new project using the DTrackSDK:

In an IDE:
  - add 'DTrackSDK.jar' to Java build path of the project in your IDE:
    - e.g. Eclipse: Right click on Project -> Properties -> Java Build Path -> Libraries -> Add External JARs...

Or alternatively in command line or terminal:
  - to compile an example or your application:
    [PROMPT] javac <file-to-compile.java> -extdirs <path-to-DTrackSDK.jar>
  - to run an example or your application:
    - on Linux: [PROMPT] java -cp "<path-to-compiled-file.class>:<path-to-DTrackSDK.jar>/DTrackSDK.jar" <class-to-run>
    - on Windows: [PROMPT] java -cp "<path-to-compiled-file.class>;<path-to-DTrackSDK.jar>\DTrackSDK.jar" <class-to-run>


Company details
---------------

Advanced Realtime Tracking GmbH & Co. KG
Am Oeferl 6
D-82362 Weilheim
Germany

http://www.ar-tracking.de/

