/*
 * DTrackSDK: Java source file
 *
 * DTrackSDK: functions to receive and process DTrack UDP packets (ASCII protocol), as
 * well as to exchange DTrack2/DTrack3 TCP command strings.
 *
 * Copyright (c) 2018-2021 Advanced Realtime Tracking GmbH & Co. KG
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Version v2.7.0
 * 
 * Purpose:
 *  - receives DTrack UDP packets (ASCII protocol) and converts them into easier to handle data
 *  - sends and receives DTrack2/DTrack3 commands (TCP)
 *  - DTrack network protocol according to:
 *    'DTrack2 User Manual, Technical Appendix' or 'DTrack3 Programmer's Guide'
 */

package art;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DTrack SDK main class derived from DTrackParser.
 * <p>
 * All methods to access tracking data are located in DTrackParser.
 *
 */
public class DTrackSDK extends DTrackParser implements AutoCloseable
{
	private static final int DTRACK2_PORT_COMMAND = 50105;   // Controller port number (TCP) for 'dtrack2' commands
	private static final int DTRACK2_PORT_FEEDBACK = 50110;  // Controller port number (UDP) for feedback commands
	private static final int DTRACK2_PROT_MAXLEN = 200;      // max. length of 'dtrack2' command

	private static final int DEFAULT_TCP_TIMEOUT = 10000;  // default TCP timeout (in ms)
	private static final int DEFAULT_UDP_TIMEOUT = 1000;   // default UDP timeout (in ms)
	private static final int DEFAULT_UDP_BUFSIZE = 32768;  // default UDP buffer size (in bytes)

	/**
	 * Error codes
	 *
	 */
	public enum Errors
	{
		ERR_NET, ERR_NONE, ERR_PARSE, ERR_TIMEOUT
	}

	private int messageErrorId;  // recent DTrack2/DTrack3 event message
	private int messageFrameNr;
	private String messageMsg;
	private String messageOrigin;
	private String messageStatus;

	private int tcpTimeout = DEFAULT_TCP_TIMEOUT;  // TCP timeout (in ms)
	private DTrackNetTCP tcp;

	private int udpBufSize = DEFAULT_UDP_BUFSIZE;  // UDP buffer size (in bytes)
	private int udpTimeout = DEFAULT_UDP_TIMEOUT;  // UDP timeout (in ms)
	private DTrackNetUDP udp;
	private String udpBuf;

	private InetAddress controllerIP;  // IP of ART Controller (if known)

	private Errors lastDataError;  // recent error codes
	private Errors lastServerError;
	private int lastDTrackError;
	private String lastDTrackErrorString;

	private Logger log = Logger.getLogger( DTrackSDK.class.getName() );


	/**
	 * Universal constructor, can be used for any mode. Recommended for new applications.
	 * Refer to other constructors for details. Communicating mode just for DTrack2/DTrack3.
	 * <p>
	 * Examples for connection string:
	 * <ul>
	 * <li>"5000" : Port number (UDP), use for pure listening mode.</li>
	 * <li>"224.0.1.0:5000" : Multicast IP and port number (UDP), use for multicast listening mode.</li>
	 * <li>"atc-301422002:5000" : Hostname of Controller and port number (UDP), use for communicating mode.</li>
	 * <li>"192.168.0.1:5000" : IP address of Controller and port number (UDP), use for communicating mode.</li>
	 * </ul>
	 * <p>
	 * <b>Remember to insert {@link DTrackSDK#close()} after SDK usage or use try-with-resources to
	 * close all sockets.</b>
	 * 
	 * @param connection Connection string ("&lt;data port&gt;" or "&lt;ip/host&gt;:&lt;data port&gt;")
	 */
	public DTrackSDK( final String connection )
	{
		super();

		int ind = connection.lastIndexOf( ':' );
		if ( ind != -1 )
		{
			InetAddress ip;
			try
			{
				ip = InetAddress.getByName( connection.substring( 0, ind ) );
			}
			catch ( UnknownHostException e )
			{
				log.log( Level.SEVERE, "Can't get IP address", e );
				return;
			}

			int port = Integer.parseInt( connection.substring( ind + 1 ) );

			init( ip, port );
		}
		else
		{
			int port = Integer.parseInt( connection );

			init( null, port );
		}
	}

	/**
	 * Constructor, use for pure listening mode. Using this constructor, only a UDP receiver to get
	 * tracking data from the Controller will be established. Please start measurement manually.
	 * <p>
	 * <b>Remember to insert {@link DTrackSDK#close()} after SDK usage or use try-with-resources to
	 * close all sockets.</b>
	 * 
	 * @param dataPort Port number (UDP) to receive tracking data from DTrack (0 if to be chosen by SDK)
	 */
	public DTrackSDK( int dataPort )
	{
		super();

		init( null, dataPort );
	}

	/**
	 * Constructor, use for multicast listening mode. Using this constructor, only a UDP receiver to get
	 * tracking data from the Controller will be established. Please start measurement manually.
	 * <p>
	 * <b>Remember to insert {@link DTrackSDK#close()} after SDK usage or use try-with-resources to
	 * close all sockets.</b>
	 * 
	 * @param multicastIp Multicast IP address to receive data from DTrack
	 * @param dataPort Port number (UDP) to receive tracking data from DTrack (0 if to be chosen by SDK)
	 */

	public DTrackSDK( final InetAddress multicastIp, int dataPort )
	{
		super();

		init( multicastIp, dataPort );
	}

	/**
	 * Constructor, use for communicating mode. Using this constructor, a UDP receiver to get tracking
	 * data from the Controller as well as a TCP connection with the Controller will be established.
	 * Automatically starts and stops measurement.
	 * <p>
	 * <b>Remember to insert {@link DTrackSDK#close()} after SDK usage or use try-with-resources to
	 * close all sockets.</b>
	 * 
	 * @param controllerHost Hostname or IP address of Controller
	 * @param dataPort Port number (UDP) to receive tracking data from DTrack (0 if to be chosen by SDK)
	 */
	public DTrackSDK( final String controllerHost, int dataPort )
	{
		super();

		InetAddress ip;
		try
		{
			ip = InetAddress.getByName( controllerHost );
		}
		catch ( UnknownHostException e )
		{
			log.log( Level.SEVERE, "Can't get IP address", e );
			return;
		}

		init( ip, dataPort );
	}

	/**
	 * Private init called by constructor. Initializes the network sockets and resets all values.
	 * 
	 * @param ip IP address of Controller or multicast IP address to receive tracking data (null if not used)
	 * @param dataPort Port number (UDP) to receive tracking data from DTrack (0 if to be chosen by SDK)
	 */
	private void init( final InetAddress ip, int dataPort )
	{
		boolean isController = false;
		boolean isMulticast = false;
		if ( ip != null )
		{
			if ( ip.isMulticastAddress() )
			{
				isMulticast = true;
			} else {
				isController = true;
			}
		}

		Locale.setDefault( Locale.US );  // ensure correct parsing of decimal points
		log.setLevel( Level.WARNING );

		lastDataError = Errors.ERR_NONE;
		lastServerError = Errors.ERR_NONE;
		setLastDTrackError();

		// initialize UDP socket
		if ( isMulticast )
		{
			udp = new DTrackNetUDP( ip, dataPort );
		} else {
			udp = new DTrackNetUDP( dataPort );
		}
		udpBuf = null;

		// initialize TCP connection
		try
		{
			if ( isController && ip.isReachable( tcpTimeout ) )
			{
				controllerIP = ip;
				tcp = new DTrackNetTCP( controllerIP, DTRACK2_PORT_COMMAND, tcpTimeout );
				if ( ! tcp.isValid() )
				{
					controllerIP = null;
					tcp = null;
				}
			}
		}
		catch ( IOException e )
		{
			log.log( Level.SEVERE, "TCP port on " + controllerIP + " is not reachable", e );
		}

		// reset current DTrack data:

		startFrame();

		messageErrorId = 0;
		messageFrameNr = 0;
		messageMsg = "";
		messageOrigin = "";
		messageStatus = "";
	}


	/**
	 * Returns if UDP socket is open to receive tracking data on local machine.
	 * <p>
	 * Needed to receive DTrack UDP data, but does not guarantee this. Especially in case no data is sent to this port.
	 * 
	 * @return Socket is open?
	 */
	public final boolean isDataInterfaceValid()
	{
		if ( udp == null )  return false;

		return udp.isValid();
	}

	/**
	 * Get UDP data port where tracking data is received.
	 * 
	 * @return UDP data port.
	 */
	public final int getDataPort()
	{
		if ( udp == null )  return 0;

		return udp.getPort();
	}

	/**
	 * Returns if TCP connection for DTrack2/DTrack3 commands is active.
	 * 
	 * @return Command interface is active
	 */
	public final boolean isCommandInterfaceValid()
	{
		if ( tcp == null )  return false;

		return tcp.isValid();
	}

	/**
	 * Set UDP timeout for receiving tracking data.
	 * 
	 * @param timeout Timeout for receiving tracking data in us; 0 to set default (1.0 s)
	 * @return Successful?
	 */
	public boolean setDataTimeoutUS( int timeout )
	{
		if ( timeout <= 0 )
		{
			udpTimeout = DEFAULT_UDP_TIMEOUT;
		}
		else
		{
			udpTimeout = timeout / 1000;
		}
		return true;
	}

	/**
	 * Set TCP timeout for exchanging commands with Controller.
	 * 
	 * @param timeout Timeout for reply of Controller in us; 0 to set default (10.0 s)
	 * @return Successful?
	 */
	public boolean setCommandTimeoutUS( int timeout )
	{
		if ( timeout <= 0 )
		{
			tcpTimeout = DEFAULT_TCP_TIMEOUT;
		}
		else
		{
			tcpTimeout = timeout / 1000;
		}
		return true;
	}

	/**
	 * Set UDP buffer size for receiving tracking data.
	 * 
	 * @param bufSize Buffer size for receiving tracking data in bytes; 0 to set default (32768)
	 * @return Successful?
	 */
	public boolean setDataBufferSize( int bufSize )
	{
		if ( bufSize <= 0 )
			bufSize = DEFAULT_UDP_BUFSIZE;

		udpBufSize = bufSize;
		return true;
	}


	/**
	 * Receive and process one tracking data packet. This method waits until a data packet becomes
	 * available, but no longer than the timeout.
	 * <p>
	 * Updates internal data structures.
	 * 
	 * @return Receive succeeded?
	 */
	public boolean receive()
	{
		lastDataError = Errors.ERR_NONE;

		if ( ! isDataInterfaceValid() )
		{
			lastDataError = Errors.ERR_NET;
			return false;
		}

		if ( udp.isTerminated() )  // start UDP receiver, if not done yet
		{
			udp.start( udpTimeout, udpBufSize );
		}

		startFrame();

		int len = udp.receive();  // receive one packet
		udpBuf = udp.getPacketContent();

		if ( len <= 0 )  // some receive error occured
		{
			if ( len == -1 )  // just timeout
			{
				lastDataError = Errors.ERR_TIMEOUT;
			}
			else
			{
				lastDataError = Errors.ERR_NET;
			}
			return false;
		}

		if ( ! parse( udpBuf ) )
		{
			lastDataError = Errors.ERR_PARSE;
			return false;
		}

		endFrame();

		return true;
	}

	/**
	 * Process a tracking packet manually. This requires no connection to a Controller.
	 * <p>
	 * Updates internal data structures.
	 * 
	 * @param data Data packet to be processed
	 * @return Processing succeeded?
	 */
	public boolean processPacket( String data )
	{
		lastDataError = Errors.ERR_NONE;

		startFrame();

		if ( data != null && ! data.isEmpty() )
		{
			lastDataError = Errors.ERR_PARSE;
			return false;
		}

		if ( ! parse( data ) )
		{
			lastDataError = Errors.ERR_PARSE;
			return false;
		}

		endFrame();

		return true;
	}

	/**
	 * Get content of the UDP buffer.
	 * 
	 * @return Content of buffer as string
	 */
	public final String getBuf()
	{
		return udpBuf;
	}


	/**
	 * Get last error at receiving tracking data (data transmission).
	 * 
	 * @return Error code
	 */
	public final Errors getLastDataError()
	{
		return lastDataError;
	}

	/**
	 * Get last error at exchanging commands with Controller (command transmission).
	 * 
	 * @return Error code
	 */
	public final Errors getLastServerError()
	{
		return lastServerError;
	}

	/**
	 * Get last DTrack2/DTrack3 command error code.
	 * 
	 * @return Error code
	 */
	public final int getLastDTrackError()
	{
		return lastDTrackError;
	}

	/**
	 * Get last DTrack2/DTrack3 command error description.
	 * 
	 * @return Error description
	 */
	public final String getLastDTrackErrorDescription()
	{
		return lastDTrackErrorString;
	}


	/**
	 * Start measurement.
	 * <p>
	 * Ensure via DTrack frontend that tracking data is sent to correct UDP data port.
	 * 
	 * @return Is command successful? If measurement is already running the return value is {@code false}
	 */
	public boolean startMeasurement()
	{
		if ( isCommandInterfaceValid() )
		{
			int err = sendDTrack2Command( "dtrack2 tracking start" );
			if ( err != 1 )
				return false;
		}

		udp.start( udpTimeout, udpBufSize );

		return true;
	}

	/**
	 * Stop measurement.
	 * 
	 * @return Is command successful? If measurement is not running return value is {@code true}
	 */
	public boolean stopMeasurement()
	{
		udp.terminate();

		if ( isCommandInterfaceValid() )
		{
			int err = sendDTrack2Command( "dtrack2 tracking stop" );
			if ( err != 1 )
				return false;
		}

		return true;
	}


	/**
	 * Get DTrack2/DTrack3 parameter.
	 * 
	 * @param category Parameter category
	 * @param name Parameter name
	 * @return Parameter value, {@code null} if unsuccessful (a DTrack error message is available)
	 */
	public String getParam( final String category, final String name )
	{
		return getParam( category + " " + name );
	}

	/**
	 * Get DTrack2/DTrack3 parameter using a string containing parameter category and name.
	 * 
	 * @param parameter Parameter key (category, name)
	 * @return Parameter value, {@code null} if unsuccessful (a DTrack error message is available)
	 */
	public String getParam( final String parameter )
	{
		StringBuilder res = new StringBuilder();
		int err = sendDTrack2Command( "dtrack2 get " + parameter, res );
		if ( err != 0 )
			return null;

		if ( ! res.toString().startsWith( "dtrack2 set " ) )
		{
			lastServerError = Errors.ERR_PARSE;
			return null;
		}

		String s = stringCmpParameter( res.toString().substring( 12 ), parameter );
		if ( s.isEmpty() )
		{
			lastServerError = Errors.ERR_PARSE;
			return null;
		}

		return s;
	}

	/**
	 * Set DTrack2/DTrack3 parameter.
	 * 
	 * @param category Parameter category
	 * @param name Parameter name
	 * @param value Parameter value
	 * @return Successful? (if not, a DTrack error message is available)
	 */
	public boolean setParam( final String category, final String name, final String value )
	{
		return setParam( category + " " + name + " " + value );
	}

	/**
	 * Set DTrack2/DTrack3 parameter using a string containing parameter category, name and new value.
	 * 
	 * @param parameter Parameter string (category, name, value)
	 * @return Successful? (if not, a DTrack error message is available)
	 */
	public boolean setParam( final String parameter )
	{
		return ( sendDTrack2Command( "dtrack2 set " + parameter ) == 1 );
	}


	/**
	 * Get DTrack2/DTrack3 event message from the Controller.
	 * <p>
	 * Updates internal message structures. Use the appropriate methods to get the contents of the
	 * message.
	 * 
	 * @return Message available?
	 */
	public boolean getMessage()
	{
		StringBuilder resB = new StringBuilder();
		int err = sendDTrack2Command( "dtrack2 getmsg", resB );
		if ( err != 0 )
			return false;

		String res = resB.toString();
		if ( ! res.startsWith( "dtrack2 msg " ) )
			return false;

		messageOrigin = messageMsg = messageStatus = "";
		messageFrameNr = messageErrorId = 0;

		int i0 = 11;
		int i1 = res.indexOf( ' ', i0 + 1 );  // origin (word)
		if ( i1 < 0 || i1 <= i0 )
			return false;

		messageOrigin = res.substring( i0 + 1, i1 );

		i0 = i1;
		i1 = res.indexOf( ' ', i0 + 1 );  // status (word)
		if ( i1 < 0 || i1 <= i0 )
			return false;

		messageStatus = res.substring( i0 + 1, i1 );

		i0 = i1;
		i1 = res.indexOf( ' ', i0 + 1 );  // frame number (integer)
		if ( i1 < 0 || i1 <= i0 )
			return false;

		try
		{
			messageFrameNr = Integer.parseInt( res.substring( i0 + 1, i1 ) );
		}
		catch ( NumberFormatException e )
		{
			return false;
		}

		i0 = res.indexOf( 'x', i1 + 1 );  // error code (hexadecimal number, trailing "0x")
		i1 = res.indexOf( ' ', i0 + 1 );
		if ( i0 < 0 || i1 < 0 || i1 <= i0 )
			return false;

		try
		{
			messageErrorId = Integer.parseUnsignedInt( res.substring( i0 + 1, i1 ), 16 );
		}
		catch ( NumberFormatException e )
		{
			return false;
		}

		i0 = res.indexOf( '"', i1 + 1 );  // message (quoted text)
		i1 = res.indexOf( '"', i0 + 1 );
		if ( i0 < 0 || i1 < 0 || i1 <= i0 )
			return false;

		messageMsg = res.substring( i0 + 1, i1 );

		return true;
	}

	/**
	 * Get frame counter of last DTrack2/DTrack3 event message.
	 * 
	 * @return Frame counter
	 */
	public final int getMessageFrameNr()
	{
		return messageFrameNr;
	}

	/**
	 * Get error id of last DTrack2/DTrack3 event message.
	 * 
	 * @return Error id
	 */
	public final int getMessageErrorId()
	{
		return messageErrorId;
	}

	/**
	 * Get origin of last DTrack2/DTrack3 event message.
	 * 
	 * @return Origin
	 */
	public final String getMessageOrigin()
	{
		return messageOrigin;
	}

	/**
	 * Get status of last DTrack2/DTrack3 event message.
	 * 
	 * @return Status
	 */
	public final String getMessageStatus()
	{
		return messageStatus;
	}

	/**
	 * Get message text of last DTrack2/DTrack3 event message.
	 * 
	 * @return Message text
	 */
	public final String getMessageMsg()
	{
		return messageMsg;
	}


	/**
	 * Compare strings regarding DTrack2/DTrack3 parameter rules
	 * 
	 * @param str String
	 * @param par Parameter string
	 * @return String behind parameter in str; {@code null} in case of error
	 */
	private String stringCmpParameter( String str, String par )
	{
		int indStr = 0;
		int indPar = 0;
		boolean lastwasdigit = false;

		while ( indPar < par.length() )
		{
			if ( indStr >= str.length() )
				return null;

			char cPar = par.charAt( indPar );
			char cStr = str.charAt( indStr );

			if ( ! lastwasdigit && ( cPar == '0' || cStr == '0' ) )  // ignore leading zeros
			{
				while ( indPar < par.length() && par.charAt( indPar ) == '0' )
					indPar++;

				while ( indStr < str.length() && str.charAt( indStr ) == '0' )
					indStr++;

				lastwasdigit = true;
				continue;
			}

			if ( cPar == ' ' || cStr == ' ' )  // ignore whitespace
			{
				while ( indPar < par.length() && par.charAt( indPar ) == ' ' )
					indPar++;

				while ( indStr < str.length() && str.charAt( indStr ) == ' ' )
					indStr++;

				lastwasdigit = false;
				continue;
			}

			if ( cPar != cStr )
				return null;

			lastwasdigit = ( cPar >= '0' && cPar <= '9' );

			indPar++;
			indStr++;
		}

		while ( indStr < str.length() && str.charAt( indStr ) == ' ' )
			indStr++;

 		return str.substring( indStr ).replaceAll( "\0", "" );
	}


	/**
	 * Send DTrack2/DTrack3 command to DTrack (TCP command interface).
	 * <p>
	 * Answers like "dtrack2 ok" and "dtrack2 err .." are processed. Both cases are reflected in the
	 * return value. {@linkplain DTrackSDK#getLastDTrackError()} and
	 * {@linkplain DTrackSDK#getLastDTrackErrorDescription()} will return more information.
	 * 
	 * @param dtrack2Command DTrack2/DTrack3 command string
	 * @return 0&nbsp; specific answer, needs to be parsed <br>
	 *         1&nbsp; answer is "dtrack2 ok" <br>
	 *         2&nbsp; answer is "dtrack2 err .."; refer to {@link DTrackSDK#getLastDataError()}
	 *         and {@link DTrackSDK#getLastDTrackErrorDescription()} <br>
	 *         &lt;0 if error occured (-1 receive timeout, -2 wrong system type, -3 command too
	 *         long, -9 broken tcp connection, -10 parser error)
	 */
	public int sendDTrack2Command( String dtrack2Command )
	{
		return sendDTrack2Command( dtrack2Command, null );
	}

	/**
	 * Send DTrack2/DTrack3 command to DTrack and receive answer (TCP command interface).
	 * <p>
	 * Answers like "dtrack2 ok" and "dtrack2 err .." are processed. Both cases are reflected in the
	 * return value. {@linkplain DTrackSDK#getLastDTrackError()} and
	 * {@linkplain DTrackSDK#getLastDTrackErrorDescription()} will return more information.
	 * 
	 * @param dtrack2Command DTrack2/DTrack3 command string
	 * @param dtrack2Response DTrack2/DTrack3 answer string
	 * @return 0&nbsp; specific answer, needs to be parsed <br>
	 *         1&nbsp; answer is "dtrack2 ok" <br>
	 *         2&nbsp; answer is "dtrack2 err .."; refer to {@link DTrackSDK#getLastDataError()}
	 *         and {@link DTrackSDK#getLastDTrackErrorDescription()} <br>
	 *         &lt;0 if error occured (-1 receive timeout, -2 wrong system type, -3 command too
	 *         long, -9 broken tcp connection, -10 parser error)
	 */
	public int sendDTrack2Command( String dtrack2Command, StringBuilder dtrack2Response )
	{
		if ( ! isCommandInterfaceValid() )
		{
			lastServerError = Errors.ERR_NET;
			return -9;
		}

		lastServerError = Errors.ERR_NONE;
		setLastDTrackError();

		if ( dtrack2Command.length() > DTRACK2_PROT_MAXLEN )
		{
			lastServerError = Errors.ERR_NET;
			return -3;
		}

		// send command:

		if ( ! dtrack2Command.endsWith( "\0" ) )
			dtrack2Command = dtrack2Command.concat( "\0" );

		if ( tcp.send( dtrack2Command, tcpTimeout ) != 0 )
		{
			lastServerError = Errors.ERR_NET;
			return -9;
		}

		// receive response:

		int err = tcp.receive( DTRACK2_PROT_MAXLEN, tcpTimeout );
		String ans = tcp.getPacketContent();

		if ( err < 0 )
		{
			if ( err == -1 )
			{
				lastServerError = Errors.ERR_TIMEOUT;
			} else {
				lastServerError = Errors.ERR_NET;
				err = -9;
			}
			return err;
		}

		// parse response:

		if ( ans.startsWith( "dtrack2 ok" ) )
			return 1;

		if ( ans.startsWith( "dtrack2 err" ) )
		{
			err = 2;
			int dterr = 0;

			String msg = ans.substring( 12 );
			int i0 = msg.indexOf( ' ' );
			try
			{
				dterr = Integer.parseInt( msg.substring( 0, i0 ) );
			}
			catch ( NumberFormatException e )
			{
				err = -10;
				dterr = -1100;
				lastServerError = Errors.ERR_PARSE;
			}

			i0 = msg.indexOf( '"', i0 + 1 );
			int i1 = msg.indexOf( '"', i0 + 1 );
			if ( i0 >= 0 && i1 > i0 )
			{
				setLastDTrackError( dterr, msg.substring( i0 + 1, i1 ) );
			} else {
				err = -10;
				setLastDTrackError( -1100, "SDK parser error" );
				lastServerError = Errors.ERR_PARSE;
			}
			return err;
		}

		if ( dtrack2Response != null )
		{
			dtrack2Response.append( ans.replaceAll( "\0", "" ) );
		}
		return 0;
	}

	/**
	 * Set last DTrack2/DTrack3 command error to default values.
	 */
	private void setLastDTrackError()
	{
		setLastDTrackError( 0, "" );
	}

	/**
	 * Set last DTrack2/DTrack3 command error.
	 * 
	 * @param newError New error code for last operation
	 * @param newErrorString Corresponding error string if exists
	 */
	private void setLastDTrackError( int newError, final String newErrorString )
	{
		lastDTrackError = newError;
		lastDTrackErrorString = newErrorString;
	}


	/**
	 * Send tactile FINGERTRACKING command to set feedback on a specific finger of a specific hand.
	 * <p>
	 * Sends command to the sender IP address of the latest received UDP data, if no hostname or IP address
	 * of a Controller is defined.
	 * 
	 * @param handId Hand id, range 0 ..
	 * @param fingerId Finger id, range 0 ..
	 * @param strength Strength of feedback, between 0.0 and 1.0
	 * @return Successful?
	 */
	public boolean tactileFinger( int handId, int fingerId, double strength )
	{
		StringBuilder sb = new StringBuilder();
		sb.append( "tfb 1 " );

		if ( strength > 1.0 || strength < 0.0 )
		{
			log.log( Level.SEVERE, "tactile strength not in range (0.0 - 1.0)" );
			return false;
		}

		sb.append( "[" ).append( handId ).append( " " ).append( fingerId );
		sb.append( " 1.0 " ).append( strength ).append( "]" );

		sb.append( "\0" );

		return sendFeedbackCommand( sb.toString() );
	}

	/**
	 * Send tactile FINGERTRACKING command to set tactile feedback on all fingers of a specific hand.
	 * <p>
	 * Sends command to the sender IP address of the latest received UDP data, if no hostname or IP address
	 * of a Controller is defined.
	 * 
	 * @param handId Hand id, range 0 ..
	 * @param strength Strength of feedback on all fingers, between 0.0 and 1.0
	 * @return Successful?
	 */
	public boolean tactileHand( int handId, double[] strength )
	{
		StringBuilder sb = new StringBuilder();
		sb.append( "tfb " ).append( strength.length ).append( " " );

		for ( int i = 0; i < strength.length; i++ )
		{
			if ( strength[ i ] > 1.0 || strength[ i ] < 0.0 )
			{
				log.log( Level.SEVERE, "tactile strength not in range (0.0 - 1.0)" );
				return false;
			}

			sb.append( "[" ).append( handId ).append( " " ).append( i );
			sb.append( " 1.0 " ).append( strength[ i ] ).append( "]" );
		}

		sb.append( "\0" );

		return sendFeedbackCommand( sb.toString() );
	}

	/**
	 * Send tactile FINGERTRACKING command to turn off tactile feedback on all fingers of a specific hand.
	 * <p>
	 * Sends command to the sender IP address of the latest received UDP data, if no hostname or IP address
	 * of a Controller is defined.
	 * 
	 * @param handId Hand id, range 0 ..
	 * @param numFinger Number of fingers
	 * @return Successful?
	 */
	public boolean tactileHandOff( int handId, int numFinger )
	{
		double[] strength = new double[ numFinger ];
		for ( int i = 0; i < numFinger; i++ )
		{
			strength[ i ] = 0;
		}

		return tactileHand( handId, strength );
	}

	/**
	 * Send Flystick feedback command to start a beep on a specific Flystick.
	 * <p>
	 * Sends command to the sender IP address of the latest received UDP data, if no hostname or IP address
	 * of a Controller is defined.
	 *
	 * @param flystickId Flystick id, range 0 ..
	 * @param durationMs Time duration of the beep (in milliseconds)
	 * @param frequencyHz Frequency of the beep (in Hertz)
	 * @return Successful?
	 */
	public boolean flystickBeep( int flystickId, double durationMs, double frequencyHz )
	{
		StringBuilder sb = new StringBuilder();
		sb.append( "ffb 1 " );

		sb.append( "[" ).append( flystickId ).append( " " ).append( ( int )durationMs );
		sb.append( " " ).append( ( int )frequencyHz ).append( " 0 0][]" );

		sb.append( "\0" );

		return sendFeedbackCommand( sb.toString() );
	}

	/**
	 * Send Flystick feedback command to start a vibration pattern on a specific Flystick.
	 * <p>
	 * Sends command to the sender IP address of the latest received UDP data, if no hostname or IP address
	 * of a Controller is defined.
	 *
	 * @param flystickId Flystick id, range 0 ..
	 * @param vibrationPattern Vibration pattern id, range 1 ..
	 * @return Successful?
	 */
	public boolean flystickVibration( int flystickId, int vibrationPattern )
	{
		StringBuilder sb = new StringBuilder();
		sb.append( "ffb 1 " );

		sb.append( "[" ).append( flystickId ).append( " 0 0 " ).append( vibrationPattern );
		sb.append( " 0][]" );

		sb.append( "\0" );

		return sendFeedbackCommand( sb.toString() );
	}

	/**
	 * Send feedback command via UDP.
	 * 
	 * @param command Command string
	 * @return Successful?
	 */
	private boolean sendFeedbackCommand( String command )
	{
		InetAddress ip = controllerIP;
		if ( ip == null )  // if IP of Controller is not known, try IP of latest received UDP data
		{
			ip = udp.getRemoteIp();

			if ( ip == null )
				return false;
		}

		if ( udp.send( ip, DTRACK2_PORT_FEEDBACK, command ) != 0 )
			return false;

		return true;
	}


	/**
	 * Set level of the logger. Use {@linkplain Level#OFF} to disable output.
	 * 
	 * @param level New level of the logger
	 * @see Level
	 */
	public void setLogLevel( Level level )
	{
		log.setLevel( level );
	}

	/**
	 * Closes all opened sockets.
	 * <p>
	 * Use try-with-resources to automatically close the sdk after usage:
	 * <pre>
	 * try ( DTrackSDK sdk = new DTrackSDK( ... ) )
	 * {
	 *    // Use sdk here
	 * }
	 * </pre>
	 */
	@Override
	public void close()
	{
		if ( udp != null )
			udp.close();

		if ( tcp != null )
			tcp.close();
	}
}

