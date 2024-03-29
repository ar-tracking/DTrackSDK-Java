/*
 * DTrackSDK: Java example
 *
 * TactileFlystick: Java example using Flystick to control a tactile FINGERTRACKING device
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
 * Purpose:
 *  - example with or without DTrack2/DTrack3 remote commands
 *  - in communicating mode: starts measurement, collects frames and stops measurement again
 *  - in listening mode: please start measurement manually e.g. in DTrack frontend application
 *  - uses a Flystick to control an ART tactile FINGERTRACKING device
 *  - for DTrackSDK v2.7.0 (or newer)
 */

import art.DTrackFlystick;
import art.DTrackSDK;
import art.DTrackSDK.Errors;

/**
 * Use in command line or terminal (TactileFlystick [<server host/ip>:]<data port> <Flystick id> <hand id>) or
 * ensure that program arguments ([<server host/ip>:]<data port> <Flystick id> <hand id>) are set in your IDE.
 * <p>
 * Control the tactile feedback device using the Flystick:
 * <li>Upper buttons set feedback for a finger with fixed strength,
 * <li>Joystick creates feedback for one or two fingers with variable strength,
 * <li>Pressing the trigger button stops the program.
 */

public class TactileFlystick
{
	private static DTrackSDK sdk;

	private static final int NUMBER_OF_FINGERS = 3;  // for 3 fingers
	private static double[] strength = new double[ NUMBER_OF_FINGERS ];

	private static final long REPEAT_PERIOD = 1000;  // period (in milliseconds) to repeat tactile command
	private static long lastTimeMillis;

	public static void main( String[] args )
	{
		if ( args.length != 3 )
		{
			System.out.println( "Usage: TactileFlystick [<server host/ip>:]<data port> <Flystick id> <hand id>" );
			return;
		}

		int flystickId = Integer.parseInt( args[ 1 ] );
		int handId = Integer.parseInt( args[ 2 ] );

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

		for ( int i = 0; i < NUMBER_OF_FINGERS; i++ )
			strength[ i ] = 0.0;

		lastTimeMillis = System.currentTimeMillis();

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

				if ( flystickId >= sdk.getNumFlystick() || handId >= sdk.getNumHand() )
				{
					System.err.println( "Flystick ID or Hand ID doesn't exist!" );
					isRunning = false;
				}

				if ( ! doTactile( flystickId, handId ) )
					isRunning = false;
			} else {
				errorToConsole();
				if ( sdk.isCommandInterfaceValid() )  messagesToConsole();
			}

			if ( ( count % 100 == 0 ) && sdk.isCommandInterfaceValid() )
				messagesToConsole();
		}

		sdk.tactileHandOff( handId, NUMBER_OF_FINGERS );

		if ( sdk.isCommandInterfaceValid() )
		{
			sdk.stopMeasurement();  // stop measurement
			messagesToConsole();
		}

		sdk.close();
	}

	/**
	 * Process a frame and control tactile feedback device.
	 *
	 * @param flystickId Id of Flystick
	 * @param handId Id of ART tactile feedback device
	 * @return Continue measurement?
	 */
	private static boolean doTactile( int flystickId, int handId )
	{
		DTrackFlystick fly = sdk.getFlystick( flystickId );

		if ( fly.getButton()[ 0 ] != 0 )  // stop program if trigger button pressed
			return false;

		// get new feedback strengths:

		double newStrength[] = new double[ NUMBER_OF_FINGERS ];

		for ( int i = 0; i < NUMBER_OF_FINGERS; i++ )
		{
			newStrength[ i ] = 0.0;

			if ( fly.getButton()[ i + 1 ] != 0 )  // fixed strength if pressing upper buttons
				newStrength[ i ] = 0.5;
		}

		double joy = fly.getJoystick()[ 0 ];
		if ( joy > 0.0 )  // variable strength if using joystick
		{
			newStrength[ 0 ] = joy;
		}
		else if ( joy < 0.0 )
		{
			newStrength[ 2 ] = -joy;
		}

		joy = fly.getJoystick()[ 1 ];
		if ( joy > 0.0 )
		{
			newStrength[ 1 ] = joy;
		}

		// check if sending of tactile command is necessary:

		boolean dosend = false;
		for ( int i = 0; i < NUMBER_OF_FINGERS; i++ )
		{
			if ( newStrength[ i ] != strength[ i ] )
			{
				strength[ i ] = newStrength[ i ];
				dosend = true;
			}
		}

		long timeMillis = System.currentTimeMillis();
		if ( timeMillis - lastTimeMillis >= REPEAT_PERIOD )  // repeat tactile command
			dosend = true;

		// send tactile command:

		if ( dosend )
		{
			sdk.tactileHand( handId, strength );

			lastTimeMillis = timeMillis;
		}

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

