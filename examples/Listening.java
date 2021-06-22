/*
 * DTrackSDK: Java example
 *
 * Listening: Java example using DTrackSDK for pure listening
 *
 * Copyright (c) 2018-2021, Advanced Realtime Tracking GmbH & Co. KG
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
 *  - example without DTrack2/DTrack3 remote commands: just collects frames
 *  - please start measurement manually e.g. in DTrack frontend application
 *  - for DTrackSDK v2.6.0 (or newer)
 */

import art.DTrackBody;
import art.DTrackFlystick;
import art.DTrackFinger;
import art.DTrackHand;
import art.DTrackJoint;
import art.DTrackHuman;
import art.DTrackInertial;
import art.DTrackMarker;
import art.DTrackMeaRef;
import art.DTrackMeaTool;
import art.DTrackSDK;
import art.DTrackSDK.Errors;

/**
 * Use in command line or terminal (Listening <data port>) or
 * ensure that program argument (<data port>) is set in your IDE.
 *
 */
public class Listening
{
	static DTrackSDK sdk;

	public static void main( String[] args )
	{
		if ( args.length != 1 )
		{
			System.out.println( "Usage: Listening <data port>" );
			return;
		}

		int port = Integer.parseInt( args[ 0 ] );
		if ( port <= 0 || port >= 65536 )
		{
			System.out.println( "Invalid port " + port );
			return;
		}

		// initialization:

		sdk = new DTrackSDK( port );
		if ( ! sdk.isDataInterfaceValid() )
		{
			System.out.println( "DTrackSDK init error" );
			return;
		}
		System.out.printf( "listening at local data port %s%n", sdk.getDataPort() );

//		sdk.setDataTimeoutUS( 3000000 );  // NOTE: change here timeout for receiving tracking data, if necessary
//		sdk.setDataBufferSize( 100000 );  // NOTE: change here buffer size for receiving tracking data, if necessary

		// measurement:

		if ( ! sdk.startMeasurement() )  // start listening
		{
			System.out.println( "Measurement start failed!" );
			return;
		}

		int count = 0;
		while ( count++ < 1000 )  // collect 1000 frames
		{
			if ( sdk.receive() )
			{
				output();
			} else {
				errorToConsole();
			}
		}

		sdk.stopMeasurement();  // stop listening
		sdk.close();
	}

	private static void output()
	{
		System.out.printf(
				"%nframe %s ts %s nbod %s nfly %s nmea %s nmearef %s nhand %s nmar %s nhuman %s ninertial %s%n",
				sdk.getFrameCounter(), sdk.getTimeStamp(), sdk.getNumBody(), sdk.getNumFlystick(),
				sdk.getNumMeaTool(), sdk.getNumMeaRef(), sdk.getNumHand(), sdk.getNumMarker(),
				sdk.getNumHuman(), sdk.getNumInertial() );

		// Standard bodies:
		for ( int i = 0; i < sdk.getNumBody(); i++ )
		{
			DTrackBody body = sdk.getBody( i );
			if ( ! body.isTracked() )
			{
				System.out.printf( "bod %s not tracked%n", body.getId() );
			} else {
				System.out.printf(
						"bod %s qu %.3f loc %.3f %.3f %.3f rot %.3f %.3f %.3f  %.3f %.3f %.3f  %.3f %.3f %.3f%n",
						body.getId(), body.getQuality(),
						body.getLoc()[ 0 ], body.getLoc()[ 1 ], body.getLoc()[ 2 ],
						body.getRot()[ 0 ][ 0 ], body.getRot()[ 1 ][ 0 ], body.getRot()[ 2 ][ 0 ],
						body.getRot()[ 0 ][ 1 ], body.getRot()[ 1 ][ 1 ], body.getRot()[ 2 ][ 1 ],
						body.getRot()[ 0 ][ 2 ], body.getRot()[ 1 ][ 2 ], body.getRot()[ 2 ][ 2 ] );
			}
		}

		// A.R.T. Flysticks:
		for ( int i = 0; i < sdk.getNumFlystick(); i++ )
		{
			DTrackFlystick flystick = sdk.getFlystick( i );
			if ( ! flystick.isTracked() )
			{
				System.out.printf( "fly %s not tracked%n", flystick.getId() );
			} else {
				System.out.printf(
						"fly %s qu %.3f loc %.3f %.3f %.3f rot %.3f %.3f %.3f  %.3f %.3f %.3f  %.3f %.3f %.3f%n",
						flystick.getId(), flystick.getQuality(),
						flystick.getLoc()[ 0 ], flystick.getLoc()[ 1 ], flystick.getLoc()[ 2 ],
						flystick.getRot()[ 0 ][ 0 ], flystick.getRot()[ 1 ][ 0 ], flystick.getRot()[ 2 ][ 0 ],
						flystick.getRot()[ 0 ][ 1 ], flystick.getRot()[ 1 ][ 1 ], flystick.getRot()[ 2 ][ 1 ],
						flystick.getRot()[ 0 ][ 2 ], flystick.getRot()[ 1 ][ 2 ], flystick.getRot()[ 2 ][ 2 ] );
			}
			System.out.printf( "fly %s btn", flystick.getId() );
			for ( int j = 0; j < flystick.getNumButton(); j++ )
			{
				System.out.printf( " %s", flystick.getButton()[ j ] );
			}
			System.out.print( " joy" );
			for ( int j = 0; j < flystick.getNumJoystick(); j++ )
			{
				System.out.printf( " %s", flystick.getJoystick()[ j ] );
			}
			System.out.print( "\n" );
		}

		// Measurement tools:
		for ( int i = 0; i < sdk.getNumMeaTool(); i++ )
		{
			DTrackMeaTool meatool = sdk.getMeaTool( i );
			if ( ! meatool.isTracked() )
			{
				System.out.printf( "mea %s not tracked%n", meatool.getId() );
			} else {
				System.out.printf(
						"mea %s qu %.3f loc %.3f %.3f %.3f rot %.3f %.3f %.3f  %.3f %.3f %.3f  %.3f %.3f %.3f%n",
						meatool.getId(), meatool.getQuality(),
						meatool.getLoc()[ 0 ], meatool.getLoc()[ 1 ], meatool.getLoc()[ 2 ],
						meatool.getRot()[ 0 ][ 0 ], meatool.getRot()[ 1 ][ 0 ], meatool.getRot()[ 2 ][ 0 ],
						meatool.getRot()[ 0 ][ 1 ], meatool.getRot()[ 1 ][ 1 ], meatool.getRot()[ 2 ][ 1 ],
						meatool.getRot()[ 0 ][ 2 ], meatool.getRot()[ 1 ][ 2 ], meatool.getRot()[ 2 ][ 2 ] );
			}
			System.out.printf( "mea %s btn", meatool.getId() );
			for ( int j = 0; j < meatool.getNumButton(); j++ )
			{
				System.out.printf( " %s", meatool.getButton()[ j ] );
			}
			if ( meatool.getTipRadius() != 0.0 )
			{
				System.out.printf( " radius %s", meatool.getTipRadius() );
			}
			System.out.print( "\n" );
		}

		// Measurement references:
		for ( int i = 0; i < sdk.getNumMeaRef(); i++ )
		{
			DTrackMeaRef mearef = sdk.getMeaRef( i );
			if ( ! mearef.isTracked() )
			{
				System.out.printf( "mearef %s not tracked%n", mearef.getId() );
			} else {
				System.out.printf(
						"mearef %s qu %.3f loc %.3f %.3f %.3f rot %.3f %.3f %.3f  %.3f %.3f %.3f  %.3f %.3f %.3f%n",
						mearef.getId(), mearef.getQuality(),
						mearef.getLoc()[ 0 ], mearef.getLoc()[ 1 ], mearef.getLoc()[ 2 ],
						mearef.getRot()[ 0 ][ 0 ], mearef.getRot()[ 1 ][ 0 ], mearef.getRot()[ 2 ][ 0 ],
						mearef.getRot()[ 0 ][ 1 ], mearef.getRot()[ 1 ][ 1 ], mearef.getRot()[ 2 ][ 1 ],
						mearef.getRot()[ 0 ][ 2 ], mearef.getRot()[ 1 ][ 2 ], mearef.getRot()[ 2 ][ 2 ] );
			}
		}

		// A.R.T. Fingertracking hands:
		for ( int i = 0; i < sdk.getNumHand(); i++ )
		{
			DTrackHand hand = sdk.getHand( i );
			if ( ! hand.isTracked() )
			{
				System.out.printf( "hand %s not tracked%n", hand.getId() );
			} else {
				System.out.printf(
						"hand %s qu %.3f lr %s nf %s loc %.3f %.3f %.3f rot %.3f %.3f %.3f  %.3f %.3f %.3f  %.3f %.3f %.3f%n",
						hand.getId(), hand.getQuality(), ( ( hand.getLr() == 0 ) ? "left" : "right" ),
						hand.getNumFinger(),
						hand.getLoc()[ 0 ], hand.getLoc()[ 1 ], hand.getLoc()[ 2 ],
						hand.getRot()[ 0 ][ 0 ], hand.getRot()[ 1 ][ 0 ], hand.getRot()[ 2 ][ 0 ],
						hand.getRot()[ 0 ][ 1 ], hand.getRot()[ 1 ][ 1 ], hand.getRot()[ 2 ][ 1 ],
						hand.getRot()[ 0 ][ 2 ], hand.getRot()[ 1 ][ 2 ], hand.getRot()[ 2 ][ 2 ] );
				for ( int j = 0; j < hand.getNumFinger(); j++ )
				{
					DTrackFinger finger = hand.getFinger( j );
					System.out.printf(
							"      fi %s loc %.3f %.3f %.3f rot %.3f %.3f %.3f  %.3f %.3f %.3f  %.3f %.3f %.3f%n", j,
							finger.getLoc()[ 0 ], finger.getLoc()[ 1 ], finger.getLoc()[ 2 ],
							finger.getRot()[ 0 ][ 0 ], finger.getRot()[ 1 ][ 0 ], finger.getRot()[ 2 ][ 0 ],
							finger.getRot()[ 0 ][ 1 ], finger.getRot()[ 1 ][ 1 ], finger.getRot()[ 2 ][ 1 ],
							finger.getRot()[ 0 ][ 2 ], finger.getRot()[ 1 ][ 2 ], finger.getRot()[ 2 ][ 2 ] );
					System.out.printf(
							"      fi %s tip %.3f pha %.3f %.3f %.3f ang %.3f %.3f%n", j,
							finger.getRadiusTip(), finger.getLengthPhalanx()[ 0 ],
							finger.getLengthPhalanx()[ 1 ], finger.getLengthPhalanx()[ 2 ],
							finger.getAnglePhalanx()[ 0 ], finger.getAnglePhalanx()[ 1 ] );
				}
			}
		}

		// Single markers:
		for ( int i = 0; i < sdk.getNumMarker(); i++ )
		{
			DTrackMarker marker = sdk.getMarker( i );
			System.out.printf( "mar %s qu %.3f loc %.3f %.3f %.3f%n", marker.getId(), marker.getQuality(),
					marker.getLoc()[ 0 ], marker.getLoc()[ 1 ], marker.getLoc()[ 2 ] );
		}

		// A.R.T. human model:
		if ( sdk.getNumHuman() < 1 )
		{
			System.out.println( "no human model data" );
		}
		for ( int i = 0; i < sdk.getNumHuman(); i++ )
		{
			DTrackHuman human = sdk.getHuman( i );
			if ( ! human.isTracked() )
			{
				System.out.printf( "human %s not tracked%n", human.getId() );
			} else {
				System.out.printf( "human %s njoints %s%n", human.getId(), human.getNumJoint() );
				for ( int j = 0; j < human.getNumJoint(); j++ )
				{
					DTrackJoint joint = human.getJoint( j );
					if ( ! joint.isTracked() )
					{
						System.out.printf( "joint %s not tracked%n", joint.getId() );
					} else {
						System.out.printf(
								"joint %s qu %.3f loc %.3f %.3f %.3f rot %.3f %.3f %.3f  %.3f %.3f %.3f  %.3f %.3f %.3f%n",
								joint.getId(), joint.getQuality(),
								joint.getLoc()[ 0 ], joint.getLoc()[ 1 ], joint.getLoc()[ 2 ],
								joint.getRot()[ 0 ][ 0 ], joint.getRot()[ 1 ][ 0 ], joint.getRot()[ 2 ][ 0 ],
								joint.getRot()[ 0 ][ 1 ], joint.getRot()[ 1 ][ 1 ], joint.getRot()[ 2 ][ 1 ],
								joint.getRot()[ 0 ][ 2 ], joint.getRot()[ 1 ][ 2 ], joint.getRot()[ 2 ][ 2 ] );
					}
				}
			}
		}

		// Hybrid bodies:
		if ( sdk.getNumInertial() < 1 )
		{
			System.out.println( "no inertial body data" );
		}
		for ( int i = 0; i < sdk.getNumInertial(); i++ )
		{
			DTrackInertial inertial = sdk.getInertial( i );
			System.out.printf( "inertial %s state %s error %.3f%n", inertial.getId(), inertial.getState(),
					inertial.getError() );
			if ( inertial.isTracked() )
			{
				System.out.printf( " loc %.3f %.3f %.3f rot %.3f %.3f %.3f  %.3f %.3f %.3f  %.3f %.3f %.3f%n",
						inertial.getLoc()[ 0 ], inertial.getLoc()[ 1 ], inertial.getLoc()[ 2 ],
						inertial.getRot()[ 0 ][ 0 ], inertial.getRot()[ 1 ][ 0 ], inertial.getRot()[ 2 ][ 0 ],
						inertial.getRot()[ 0 ][ 1 ], inertial.getRot()[ 1 ][ 1 ], inertial.getRot()[ 2 ][ 1 ],
						inertial.getRot()[ 0 ][ 2 ], inertial.getRot()[ 1 ][ 2 ], inertial.getRot()[ 2 ][ 2 ] );
			}
		}
	}

	private static boolean errorToConsole()
	{
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

			return false;
		}

		return true;
	}
}

