/*
 * DTrackNetTCP: Java source file, A.R.T. GmbH
 *
 * DTrackNetTCP: functions for receiving and sending TCP/IP packets
 *
 * Copyright (c) 2018, Advanced Realtime Tracking GmbH
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
 * Version v2.6.0
 * 
 */

package art;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

class DTrackNetTCP
{
	private String tcpPacketContent = null;

	private Socket socket;
	private OutputStreamWriter writer;
	private InputStreamReader reader;

	private static Logger log = Logger.getLogger( DTrackSDK.class.getName() );

	/**
	 * Constructor. Initialize client TCP socket and connect to TCP server.
	 * 
	 * @param address Hostname or IP address of TCP server
	 * @param port Port number of TCP server
	 * @param timeout Timeout in ms (milliseconds) to connect to TCP server
	 */
	protected DTrackNetTCP( InetAddress address, int port, int timeout )
	{
		socket = null;
		writer = null;
		reader = null;

		Socket s = new Socket();
		SocketAddress addr = new InetSocketAddress( address, port );

		OutputStreamWriter osw = null;
		InputStreamReader isr = null;

		try
		{
			s.setTcpNoDelay( true );

			s.connect( addr, timeout );

			osw = new OutputStreamWriter( s.getOutputStream(), "UTF-8" );
			isr = new InputStreamReader( s.getInputStream(), "UTF-8" );
		}
		catch ( IOException e )
		{
			log.log( Level.SEVERE, "TCP socket connection failed", e );

			try
			{
				s.close();
			}
			catch ( IOException e1 )
			{
				log.log( Level.WARNING, "Exception", e1 );
			}

			return;
		}

		socket = s;
		writer = osw;
		reader = isr;
	}

	/**
	 * Deinitialize TCP.
	 * 
	 * @return 0 if ok, -1 if error occured
	 */
	protected int close()
	{
		if ( socket == null )
			return 0;

		try
		{
			if ( ! socket.isInputShutdown() && ! socket.isClosed() )
				socket.shutdownInput();

			if ( ! socket.isOutputShutdown() && !socket.isClosed() )
				socket.shutdownOutput();

			if ( ! socket.isClosed() )
				socket.close();

			return 0;
		}
		catch ( IOException e )
		{
			log.log( Level.WARNING, "Can't close TCP socket", e );
			return -1;
		}
	}

	/**
	 * Returns if TCP connection is valid and useable.
	 *
	 * @return Connection is useable
	 */
	protected boolean isValid()
	{
		if ( socket == null )
			return false;

		return ! socket.isClosed();
	}


	/**
	 * Returns the latest received TCP packet content.
	 * 
	 * @return Content of the latest received TCP packet
	 */
	protected String getPacketContent()
	{
		return tcpPacketContent;
	}


	/**
	 * Receive TCP data.
	 * 
	 * @param maxLen Length of buffer
	 * @param timeout Timeout in ms (milliseconds)
	 * @return Number of received bytes, &lt;0 if error/timeout occurred,
	 */
	protected int receive( int maxLen, int timeout )
	{
		tcpPacketContent = null;

		if ( socket.isInputShutdown() )
			return -1;

		char[] buf = new char[ maxLen ];
		int n;
		try
		{
			socket.setSoTimeout( timeout );

			while ( ( n = reader.read( buf, 0, maxLen ) ) <= 0 )
			{
				if ( n < 0 ) return -2;  // socket was closed
			}
		}
		catch ( SocketTimeoutException e )
		{
			log.warning( "TCP receive timeout" );
			return -1;
		}
		catch ( IOException e )
		{
			log.log( Level.SEVERE, "Can't receive TCP data", e );
			return -2;
		}

		tcpPacketContent = new String( buf );
		return tcpPacketContent.length();
	}

	/**
	 * Send TCP data.
	 * 
	 * @param buffer Data to send
	 * @param timeout Timeout in ms (milliseconds)
	 * @return 0 if ok, &lt;0 if error/timeout occurred
	 */
	protected int send( String buffer, int timeout )
	{
		if ( socket.isOutputShutdown() )
			return -1;

		try
		{
			socket.setSoTimeout( timeout );

			writer.write( buffer, 0, buffer.length() );
			writer.flush();
		}
		catch ( SocketTimeoutException e )
		{
			log.log( Level.SEVERE, "TCP send timeout", e );
			return -1;
		}
		catch ( IOException e )
		{
			log.log( Level.SEVERE, "Can't send TCP data", e );
			return -2;
		}

		return 0;
	}
}

