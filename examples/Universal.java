/* DTrackSDK in Java: Universal.java
 *
 * Java example using universal DTrackSDK constructor for all modes.
 *
 * Copyright (c) 2021-2024 Advanced Realtime Tracking GmbH & Co. KG
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
 *  - example with or without DTrack2/DTRACK3 remote commands
 *  - in communicating mode: starts measurement, collects some frames and stops measurement again
 *  - in listening mode: please start measurement manually e.g. in DTrack frontend application
 *  - for DTrackSDK v2.9.0 (or newer)
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
import art.DTrackCameraStatus;
import art.DTrackStatus;
import art.DTrackSDK;
import art.DTrackSDK.Errors;

/**
 * Use in command line or terminal (Universal [<server host/ip>:]<data port>) or
 * ensure that program arguments ([<server host/ip>:]<data port>[:fw]) are set in your IDE.
 *
 */
public class Universal
{
	private static DTrackSDK sdk;

	public static void main( String[] args )
	{
		if ( args.length != 1 )
		{
			System.out.println( "Usage: Universal [<server host/ip>:]<data port>[:fw]" );
			return;
		}

		// initialization:

		sdk = new DTrackSDK( args[ 0 ] );

		if ( ! sdk.isValid() )
		{
			if ( ! sdk.isDataInterfaceValid() )
			{
				System.err.println( "DTrackSDK fatal error: initializing data interface" );
			}
			else if ( ! sdk.isCommandInterfaceValid() )
			{
				System.err.println( "DTrackSDK error: cannot connect to ATC" );
			}
			else if ( ! sdk.isCommandInterfaceFullAccess() )  // ensure full access for DTrack2/3 commands, if in communicating mode
			{
				System.err.println( "DTrackSDK error: full access to ATC required" );
			}                                                 // maybe DTrack2/DTRACK3 frontend is still connected to ATC

			return;
		}

		System.out.printf( "connected to ATC '%s', listening at local data port %d%n", args[ 0 ], sdk.getDataPort() );

//		sdk.setCommandTimeoutUS( 30000000 );  // NOTE: change here timeout for exchanging commands, if necessary
//		sdk.setDataTimeoutUS( 3000000 );      // NOTE: change here timeout for receiving tracking data, if necessary
//		sdk.setDataBufferSize( 100000 );      // NOTE: change here buffer size for receiving tracking data, if necessary

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
		while ( count++ < 1000 )  // collect 1000 frames
		{
			if ( sdk.receive() )
			{
				output();
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

	private static void output()
	{
		System.out.printf(
				"%nframe %d ts %.6f ets %d.%06d lat %d%n",
				sdk.getFrameCounter(), sdk.getTimeStamp(), sdk.getTimeStampSec(), sdk.getTimeStampUsec(),
				sdk.getLatencyUsec() );

		System.out.printf(
				"      nbod %d nfly %d nmea %d nmearef %d nhand %d nmar %d nhuman %d ninertial %d status %s%n",
				sdk.getNumBody(), sdk.getNumFlystick(),
				sdk.getNumMeaTool(), sdk.getNumMeaRef(), sdk.getNumHand(), sdk.getNumMarker(),
				sdk.getNumHuman(), sdk.getNumInertial(),
				( sdk.isStatusAvailable() ? "yes" : "no" ) );

		// Standard bodies:
		for ( int i = 0; i < sdk.getNumBody(); i++ )
		{
			DTrackBody body = sdk.getBody( i );
			if ( ! body.isTracked() )
			{
				System.out.printf( "bod %d not tracked%n", body.getId() );
			} else {
				System.out.printf(
						"bod %d qu %.3f loc %.3f %.3f %.3f rot %.3f %.3f %.3f  %.3f %.3f %.3f  %.3f %.3f %.3f%n",
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
				System.out.printf( "fly %d not tracked%n", flystick.getId() );
			} else {
				System.out.printf(
						"fly %d qu %.3f loc %.3f %.3f %.3f rot %.3f %.3f %.3f  %.3f %.3f %.3f  %.3f %.3f %.3f%n",
						flystick.getId(), flystick.getQuality(),
						flystick.getLoc()[ 0 ], flystick.getLoc()[ 1 ], flystick.getLoc()[ 2 ],
						flystick.getRot()[ 0 ][ 0 ], flystick.getRot()[ 1 ][ 0 ], flystick.getRot()[ 2 ][ 0 ],
						flystick.getRot()[ 0 ][ 1 ], flystick.getRot()[ 1 ][ 1 ], flystick.getRot()[ 2 ][ 1 ],
						flystick.getRot()[ 0 ][ 2 ], flystick.getRot()[ 1 ][ 2 ], flystick.getRot()[ 2 ][ 2 ] );
			}
			System.out.printf( "fly %d btn", flystick.getId() );
			for ( int j = 0; j < flystick.getNumButton(); j++ )
			{
				System.out.printf( " %d", flystick.getButton()[ j ] );
			}
			System.out.print( " joy" );
			for ( int j = 0; j < flystick.getNumJoystick(); j++ )
			{
				System.out.printf( " %.2f", flystick.getJoystick()[ j ] );
			}
			System.out.print( "\n" );
		}

		// Measurement tools:
		for ( int i = 0; i < sdk.getNumMeaTool(); i++ )
		{
			DTrackMeaTool meatool = sdk.getMeaTool( i );
			if ( ! meatool.isTracked() )
			{
				System.out.printf( "mea %d not tracked%n", meatool.getId() );
			} else {
				System.out.printf(
						"mea %d qu %.3f loc %.3f %.3f %.3f rot %.3f %.3f %.3f  %.3f %.3f %.3f  %.3f %.3f %.3f%n",
						meatool.getId(), meatool.getQuality(),
						meatool.getLoc()[ 0 ], meatool.getLoc()[ 1 ], meatool.getLoc()[ 2 ],
						meatool.getRot()[ 0 ][ 0 ], meatool.getRot()[ 1 ][ 0 ], meatool.getRot()[ 2 ][ 0 ],
						meatool.getRot()[ 0 ][ 1 ], meatool.getRot()[ 1 ][ 1 ], meatool.getRot()[ 2 ][ 1 ],
						meatool.getRot()[ 0 ][ 2 ], meatool.getRot()[ 1 ][ 2 ], meatool.getRot()[ 2 ][ 2 ] );
			}
			System.out.printf( "mea %d btn", meatool.getId() );
			for ( int j = 0; j < meatool.getNumButton(); j++ )
			{
				System.out.printf( " %d", meatool.getButton()[ j ] );
			}
			if ( meatool.getTipRadius() != 0.0 )
			{
				System.out.printf( " radius %.3f", meatool.getTipRadius() );
			}
			System.out.print( "\n" );
		}

		// Measurement references:
		for ( int i = 0; i < sdk.getNumMeaRef(); i++ )
		{
			DTrackMeaRef mearef = sdk.getMeaRef( i );
			if ( ! mearef.isTracked() )
			{
				System.out.printf( "mearef %d not tracked%n", mearef.getId() );
			} else {
				System.out.printf(
						"mearef %d qu %.3f loc %.3f %.3f %.3f rot %.3f %.3f %.3f  %.3f %.3f %.3f  %.3f %.3f %.3f%n",
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
				System.out.printf( "hand %d not tracked%n", hand.getId() );
			} else {
				System.out.printf(
						"hand %d qu %.3f lr %s nf %d loc %.3f %.3f %.3f rot %.3f %.3f %.3f  %.3f %.3f %.3f  %.3f %.3f %.3f%n",
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
							"      fi %d loc %.3f %.3f %.3f rot %.3f %.3f %.3f  %.3f %.3f %.3f  %.3f %.3f %.3f%n", j,
							finger.getLoc()[ 0 ], finger.getLoc()[ 1 ], finger.getLoc()[ 2 ],
							finger.getRot()[ 0 ][ 0 ], finger.getRot()[ 1 ][ 0 ], finger.getRot()[ 2 ][ 0 ],
							finger.getRot()[ 0 ][ 1 ], finger.getRot()[ 1 ][ 1 ], finger.getRot()[ 2 ][ 1 ],
							finger.getRot()[ 0 ][ 2 ], finger.getRot()[ 1 ][ 2 ], finger.getRot()[ 2 ][ 2 ] );
					System.out.printf(
							"      fi %d tip %.3f pha %.3f %.3f %.3f ang %.3f %.3f%n", j,
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
			System.out.printf( "mar %d qu %.3f loc %.3f %.3f %.3f%n", marker.getId(), marker.getQuality(),
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
				System.out.printf( "human %d not tracked%n", human.getId() );
			} else {
				System.out.printf( "human %d njoints %d%n", human.getId(), human.getNumJoint() );
				for ( int j = 0; j < human.getNumJoint(); j++ )
				{
					DTrackJoint joint = human.getJoint( j );
					if ( ! joint.isTracked() )
					{
						System.out.printf( "joint %d not tracked%n", joint.getId() );
					} else {
						System.out.printf(
								"joint %d qu %.3f loc %.3f %.3f %.3f rot %.3f %.3f %.3f  %.3f %.3f %.3f  %.3f %.3f %.3f%n",
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
			System.out.printf( "inertial %d state %d error %.3f%n", inertial.getId(), inertial.getState(),
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

		// System status:
		if ( ! sdk.isStatusAvailable() )
		{
			System.out.println( "no system status data" );
		}
		else
		{
			DTrackStatus status = sdk.getStatus();

			// general status values
			System.out.printf( "status gen nc %d nb %d nm %d%n",
					status.getNumCameras(), status.getNumTrackedBodies(), status.getNumTrackedMarkers() );

			// message statistics
			System.out.printf( "status msg nce %d ncw %d noe %d now %d ni %d%n",
					status.getNumCameraErrorMessages(), status.getNumCameraWarningMessages(),
					status.getNumOtherErrorMessages(), status.getNumOtherWarningMessages(), status.getNumInfoMessages() );

			// camera status values
			for ( int i = 0; i < status.getNumCameras(); i++ )
			{
				DTrackCameraStatus cameraStatus = status.getCameraStatus( i );

				System.out.printf( "status cam %d ns %d nu %d mi %d%n",
						cameraStatus.getIdCamera(), cameraStatus.getNumReflections(), cameraStatus.getNumReflectionsUsed(),
						cameraStatus.getMaxIntensity() );
			}
		}
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

