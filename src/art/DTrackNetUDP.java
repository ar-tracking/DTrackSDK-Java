/*
 * DTrackNetUDP: Java source file, A.R.T. GmbH
 *
 * DTrackNetUDP: functions for receiving and sending UDP/IP packets
 *
 * Copyright (c) 2018-2019, Advanced Realtime Tracking GmbH
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
 * Purpose: - ensures, that the newest available packet is read
 */

package art;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

class DTrackNetUDP
{
	private String udpPacketContent = null;

	private DatagramSocket socket;
	private ReceiverRunnable receiver;

	private Logger log = Logger.getLogger( DTrackSDK.class.getName() );

	/**
	 * Constructor for multicast UDP.
	 * 
	 * @param group Multicast IP to listen
	 * @param port Port number, 0 if to be chosen by the OS
	 */
	protected DTrackNetUDP( InetAddress group, int port )
	{
		socket = null;

		if ( ! group.isMulticastAddress() )
		{
			log.log( Level.SEVERE, "Not a multicast address" );
			return;
		}

		if ( port < 0 || port >= 65536 )
		{
			log.log( Level.SEVERE, "Invalid port number" );
			return;
		}

		MulticastSocket dgs = null;
		try
		{
			if ( port != 0 )
			{
				dgs = new MulticastSocket( port );
			} else {
				dgs = new MulticastSocket();
			}

			dgs.setReuseAddress( true );
			dgs.joinGroup( group );
		}
		catch ( IOException e )
		{
			dgs.close();
			log.log( Level.SEVERE, "Can't create multicast UDP socket", e );
			return;
		}

		socket = dgs;
	}

	/**
	 * Constructor for 'normal' UDP.
	 * 
	 * @param port Port number, 0 if to be chosen by the OS
	 */
	protected DTrackNetUDP( int port )
	{
		socket = null;

		if ( port < 0 || port >= 65536 )
		{
			log.log( Level.SEVERE, "Invalid port number" );
			return;
		}

		DatagramSocket dgs = null;
		try
		{
			if ( port != 0 )
			{
				dgs = new DatagramSocket( port );
			} else {
				dgs = new DatagramSocket();
			}
		}
		catch ( SocketException e )
		{
			log.log( Level.SEVERE, "Can't create UDP socket", e );
			return;
		}

		socket = dgs;
	}

	/**
	 * Deinitialize UDP/multicast UDP.
	 * 
	 * @return 0 ok, -1 error
	 */
	protected int close()
	{
		terminate();  // esp. to finish receiver thread

		if ( socket == null )
			return -1;

		if ( ! socket.isClosed() )  // close socket to gain consistent state
			socket.close();

		socket = null;
		return 0;
	}

	/**
	 * Returns if socket is valid and useable.
	 *
	 * @return Socket is valid
	 */
	protected boolean isValid()
	{
		return ( socket != null );
	}

	/**
	 * Returns currently used UDP port number.
	 *
	 * @return Port number
	 */
	protected int getPort()
	{
		return socket.getLocalPort();
	}


	/**
	 * Returns the latest received TCP packet.
	 * 
	 * @return Content of the latest received UDP packet
	 */
	protected String getPacketContent()
	{
		return udpPacketContent;
	}

	/**
	 * Receive Multicast/UDP data. Gets packets from the Runnable, which continuously receives packets
	 * in the background.
	 * 
	 * @return Number of received bytes, &lt;0 if error/timeout occurred
	 */
	protected int receive()
	{
		String packet = receiver.getNext();
		// if timeout or another error occurred
		if ( packet == null )
			return -1;

		udpPacketContent = packet;
		return packet.length();
	}

	/**
	 * Send custom UDP data.
	 * <p>
	 * Make sure that buffer terminates with a NULL character ({@code "\0"})
	 * 
	 * @param address Hostname or IP address to send data to
	 * @param port Port number to send data to
	 * @param buffer Data to send
	 * @return 0 if ok, &lt;0 if error/timeout occurred
	 */
	protected int send( InetAddress address, int port, String buffer )
	{
		byte[] data = buffer.getBytes();
		DatagramPacket packet = new DatagramPacket( data, data.length, address, port );

		try
		{
			socket.send( packet );
			return 0;
		}
		catch ( SocketTimeoutException e )
		{
			log.log( Level.WARNING, "UDP send timeout", e );
			return -1;
		}
		catch ( IOException e )
		{
			log.log( Level.SEVERE, "Can't send UDP data", e );
			return -2;
		}
	}

	/**
	 * Starts receiving thread using a ReceiverRunnable object.
	 * 
	 * @param timeout Timeout for receiving data packets
	 * @param maxLen Length of buffer
	 * @return 0 if ok, &lt;0 if error occurred
	 */
	protected int start( int timeout, int maxLen )
	{
		if ( ! isTerminated() )  return 0;  // still running...

		receiver = new ReceiverRunnable( socket, maxLen, timeout );
		Thread t = new Thread( receiver );
		t.setDaemon( true );
		t.setName( "DTrackSDK Receiver Thread" );

		t.start();

		return 0;
	}

	/**
	 * Terminates the receiving thread.
	 */
	protected void terminate()
	{
		if ( receiver != null )
			receiver.terminate();
	}

	/**
	 * Returns if receiving thread is terminated or termination is demanded.
	 *
	 * @return Receiving thread is stopped
	 */
	protected boolean isTerminated()
	{
		if ( receiver != null )
			return receiver.isTerminated();

		return true;
	}


	/**
	 * Class containing thread to receive UDP data.
	 */
	private class ReceiverRunnable implements Runnable
	{
		private DatagramSocket socket;
		private int maxLen;
		private int timeout;

		private Lock lock = new ReentrantLock( true );
		private Condition cond;
		private boolean terminate = false;

		private String next = null;


		/**
		 * Constructor. Sets all important values used by the run method.
		 * 
		 * @param socket Datagram socket
		 * @param maxLen Maximum length of the resulting data
		 * @param timeout Timeout of the socket
		 */
		ReceiverRunnable( DatagramSocket socket, int maxLen, int timeout )
		{
			this.socket = socket;
			this.maxLen = maxLen;
			this.timeout = timeout;

			cond = lock.newCondition();
		}

		/**
		 * Terminates the receiving thread.
		 */
		protected void terminate()
		{
			terminate = true;
		}

		/**
		 * Returns if termination of receiving thread is already demanded.
		 *
		 * @return Termination of receiving thread is demanded
		 */
		protected boolean isTerminated()
		{
			return terminate;
		}


		/**
		 * Returns the most recent UDP data content.
		 * <p>
		 * If currently no data content is available (the last packet has been
		 * removed and no packet arrived) the calling thread waits until one packet becomes available. If
		 * the socket has been closed or another error occurred while waiting for the packet, {@code null}
		 * will be returned.
		 * 
		 * @return The most recent UDP data content, {@code null} if nothing available
		 */
		String getNext()
		{
			try
			{
				lock.lockInterruptibly();

				// wait, if the newest packet has been already removed
				if ( next == null )
				{
					// waits for a new packet, returns null if time elapsed
					if ( ! cond.await( timeout, TimeUnit.MILLISECONDS ) )
						return null;

					if ( socket.isClosed() )
						return null;
				}

				String temp = next;
				next = null;  // mark packet as removed
				return temp;
			}
			catch ( InterruptedException e )
			{
				log.log( Level.WARNING, "Error while returning next UDP packet", e );
				Thread.currentThread().interrupt();
			}
			finally
			{
				lock.unlock();  // makes sure all locks are unlocked (to prevent deadlocks)
			}

			return null;
		}

		/**
		 * Thread routine.
		 */
		@Override
		public void run()
		{
			if ( socket.isClosed() )
			{
				terminate = true;
				return;
			}

			try
			{
				socket.setSoTimeout( timeout );
			}
			catch ( SocketException e1 )
			{
				log.log( Level.WARNING, "Error while setting UDP socket timeout", e1 );
			}

			byte[] data = new byte[ maxLen ];
			DatagramPacket packet = new DatagramPacket( data, maxLen );

			while ( ! socket.isClosed() && ! terminate )
			{
				try
				{
					socket.receive( packet );

					lock.lock();

					// overwrite current content in any case, because we want to deliver the most recent data
					next = new String( packet.getData(), 0, packet.getLength() );

					// notify the (maybe) waiting thread that data is available
					cond.signal();
					lock.unlock();

					// sleep 1 ns to prevent performance problems
					Thread.sleep( 0, 1 );
				}
				catch ( SocketException e )
				{
					if ( socket.isClosed() )
						return;

					log.log( Level.SEVERE, "Error in UDP socket", e );
				}
				catch ( SocketTimeoutException e )
				{
					// no message at timeout
				}
				catch ( IOException e )
				{
					log.log( Level.SEVERE, "Error in UDP socket", e );
					return;
				}
				catch ( InterruptedException e )
				{
					log.log( Level.SEVERE, "Sleep while receiving UDP packet failed", e );
					Thread.currentThread().interrupt();
				}
			}
		}
	}
}

