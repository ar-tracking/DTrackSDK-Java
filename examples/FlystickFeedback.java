/*
 * DTrackSDK: Java example
 *
 * FlystickFeedback: Java example to control a Flystick with feedback
 *
 * Copyright (c) 2021 Advanced Realtime Tracking GmbH & Co. KG
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
 * Purpose:
 *  - example with or without DTrack2/DTrack3 remote commands
 *  - in communicating mode: starts measurement, collects frames and stops measurement again
 *  - in listening mode: please start measurement manually e.g. in DTrack frontend application
 *  - controls a Flystick2+ with feedback
 *  - for DTrackSDK v2.7.0 (or newer)
 */

import art.DTrackFlystick;
import art.DTrackSDK;
import art.DTrackSDK.Errors;

/**
 * Use in command line or terminal (FlystickFeedback [<server host/ip>:]<data port>) or
 * ensure that program arguments ([<server host/ip>:]<data port>) are set in your IDE.
 * <p>
 * Control the Flystick feedback using the Flystick itself:
 * <li>Upper buttons start vibration pattern,
 * <li>Joystick button starts a beep with variable duration and frequency,
 * <li>Pressing the trigger button stops the program.
 */

public class FlystickFeedback
{
	private static DTrackSDK sdk;

	private static boolean sentFeedback = false;

	public static void main( String[] args )
	{
		if ( args.length != 1 )
		{
			System.out.println( "Usage: FlystickFeedback [<server host/ip>:]<data port>" );
			return;
		}

		// initialization:

		sdk = new DTrackSDK( args[ 0 ] );
		if ( ! sdk.isDataInterfaceValid() )
		{
			System.err.println( "DTrackSDK init error" );
			return;
		}
		System.out.printf( "connected to ATC '%s', listening at local data port %s%n", args[ 0 ], sdk.getDataPort() );

//		sdk.setCommandTimeoutUS( 30000000 );  // NOTE: change here timeout for exchanging commands, if necessary
//		sdk.setDataTimeoutUS( 3000000 );      // NOTE: change here timeout for receiving tracking data, if necessary
//		sdk.setDataBufferSize( 100000 );      // NOTE: change here buffer size for receiving tracking data, if necessary

		// request some settings:

		if ( sdk.isCommandInterfaceValid() )
		{
			String par = sdk.getParam( "system", "access" );  // ensure full access for DTrack2 commands
			if ( par == null || par.compareTo( "full" ) != 0 )
			{
				System.err.println( "Full access to ATC required!" );  // maybe DTrack2/3 frontend is still connected to ATC
				errorToConsole();
				return;
			}
		}

		// measurement:

		if ( sdk.isCommandInterfaceValid() )
		{
			if ( ! sdk.startMeasurement() )  // start measurement
			{
				System.err.println( "Measurement start failed!" );
				messagesToConsole();
				return;
			}
		}

		int count = 0;
		boolean isRunning = true;
		while ( isRunning )  // collect frames
		{
			if ( sdk.receive() )
			{
				count++;

				int nfly = 0;
				for ( int id = 0; id < sdk.getNumFlystick(); id++ )
				{
					if ( ( sdk.getFlystick( id ).getNumButton() >= 8 ) && ( sdk.getFlystick( id ).getNumJoystick() >= 2 ) )
					{  // demo routine needs at least 8 buttons and 2 joystick values (e.g. Flystick2+)
						nfly++;

						if ( ! doFeedback( id ) )
							isRunning = false;
					}
				}

				if ( nfly == 0 )
				{
					System.err.println( "No suitable Flystick identified!" );
					isRunning = false;
				}
			} else {
				errorToConsole();
				if ( sdk.isCommandInterfaceValid() )  messagesToConsole();
			}

			if ( ( count % 100 == 0 ) && sdk.isCommandInterfaceValid() )
				messagesToConsole();
		}

		if ( sdk.isCommandInterfaceValid() )
		{
			sdk.stopMeasurement();  // stop measurement
			messagesToConsole();
		}

		sdk.close();
	}

	/**
	 * Process a frame and control Flystick feedback.
	 *
	 * @param flystickId Id of Flystick
	 * @return Continue measurement?
	 */
	private static boolean doFeedback( int flystickId )
	{
		DTrackFlystick fly = sdk.getFlystick( flystickId );

		if ( fly.getButton()[ 0 ] != 0 )  // stop program if trigger button pressed
			return false;

		// get beep feedback:

		if ( fly.getButton()[ 5 ] != 0 )  // joystick button of Flystick2+
		{
			double beepDuration = 500.0 + fly.getJoystick()[ 0 ] * 450.0;     // range 50 .. 950 ms
			double beepFrequency = 5000.0 + fly.getJoystick()[ 1 ] * 3000.0;  // range 2000 .. 8000 Hz

			if ( ! sentFeedback )  // prevents permanent sending of feedback commands as long as button is pressed
				sdk.flystickBeep( flystickId, beepDuration, beepFrequency );

			sentFeedback = true;
			return true;
		}

		// get vibration feedback:

		int vibrationPattern = 0;  // Flystick2+ supports up to 6 vibration pattern
		if ( fly.getButton()[ 1 ] != 0 )  vibrationPattern = 1;
		if ( fly.getButton()[ 2 ] != 0 )  vibrationPattern = 2;
		if ( fly.getButton()[ 3 ] != 0 )  vibrationPattern = 3;
		if ( fly.getButton()[ 4 ] != 0 )  vibrationPattern = 4;
		if ( fly.getButton()[ 6 ] != 0 )  vibrationPattern = 5;  // button '5' (joystick button) is already used
		if ( fly.getButton()[ 7 ] != 0 )  vibrationPattern = 6;

		if ( vibrationPattern > 0 )
		{
			if ( ! sentFeedback )  // prevents permanent sending of feedback commands as long as button is pressed
				sdk.flystickVibration( flystickId, vibrationPattern );

			sentFeedback = true;
			return true;
		}

		sentFeedback = false;
		return true;
	}


	private static boolean errorToConsole()
	{
		boolean ret = true;

		if ( sdk.getLastDataError() != Errors.ERR_NONE )
		{
			if ( sdk.getLastDataError() == Errors.ERR_TIMEOUT )
			{
				System.err.println( "--- timeout while waiting for tracking data" );
			}
			else if ( sdk.getLastDataError() == Errors.ERR_NET )
			{
				System.err.println( "--- error while receiving tracking data" );
			}
			else if ( sdk.getLastDataError() == Errors.ERR_PARSE )
			{
				System.err.println( "--- error while parsing tracking data" );
			}

			ret = false;
		}

		if ( sdk.getLastServerError() != Errors.ERR_NONE )
		{
			if ( sdk.getLastServerError() == Errors.ERR_TIMEOUT )
			{
				System.err.println( "--- timeout while waiting for Controller command" );
			}
			else if ( sdk.getLastServerError() == Errors.ERR_NET )
			{
				System.err.println( "--- error while receiving Controller command" );
			}
			else if ( sdk.getLastServerError() == Errors.ERR_PARSE )
			{
				System.err.println( "--- error while parsing Controller command" );
			}

			ret = false;
		}

		return ret;
	}

	private static void messagesToConsole()
	{
		while ( sdk.getMessage() )
		{
			System.err.printf( "ATC message: \"%s\" \"%s\"%n", sdk.getMessageStatus(), sdk.getMessageMsg() );
		}
	}
}

