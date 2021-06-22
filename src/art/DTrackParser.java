/*
 * DTrackParser: Java source file
 *
 * DTrackParser: functions to process DTrack UDP packets (ASCII protocol)
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
 * Version v2.7.0
 */

package art;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

class DTrackParser
{

	private enum ParseError
	{
		BLOCK, DOUBLE, INT, INVALID;

		@Override
		public String toString()
		{
			return this.name().toLowerCase();
		}
	}

	private static final String D3 = "ddd";
	private static final String D6 = "dddddd";
	private static final String D9 = "ddddddddd";
	private static final String D21 = "ddddddddddddddddddddd";

	private static void reducedToFullCov( double[][] covFull, double[] covReduced, int dim )
	{
		for ( int i = 0; i < dim; i++ )
		{
			int k = i * ( i - 1 ) / 2;
			covFull[ i ][ i ] = covReduced[ i * dim - k ];
			for ( int j = i + 1; j < dim; j++ )
			{
				covFull[ i ][ j ] = covFull[ j ][ i ] = covReduced[ i * ( dim - 1 ) - k + j ];
			}
		}
	}

	private static void oneToTwoDimRot( double[][] rot3x3, double[] rot9 )
	{
		for ( int i = 0; i < 3; i++ )
		{
			for ( int j = 0; j < 3; j++ )
			{
				rot3x3[ i ][ j ] = rot9[ i + j * 3 ];
			}
		}
	}

	/**
	 * Frame counter of currently parsed frame.
	 */
	private int actFramecounter;
	/**
	 * Timestamp of currently parsed frame.
	 */
	private double actTimestamp;
	/**
	 * Standard bodies of currently parsed frame.
	 */
	private List< DTrackBody > actBody = new ArrayList<>();
	/**
	 * Flysticks of currently parsed frame.
	 */
	private List< DTrackFlystick > actFlystick = new ArrayList<>();
	/**
	 * FINGERTRACKING hands of currently parsed frame.
	 */
	private List< DTrackHand > actHand = new ArrayList<>();
	/**
	 * ART-Human models of currently parsed frame.
	 */
	private List< DTrackHuman > actHuman = new ArrayList<>();
	/**
	 * Hybrid bodies of currently parsed frame.
	 */
	private List< DTrackInertial > actInertial = new ArrayList<>();
	/**
	 * Single markers of currently parsed frame.
	 */
	private List< DTrackMarker > actMarker = new ArrayList<>();
	/**
	 * Measurement references of currently parsed frame.
	 */
	private List< DTrackMeaRef > actMearef = new ArrayList<>();
	/**
	 * Measurement tools of currently parsed frame.
	 */
	private List< DTrackMeaTool > actMeatool = new ArrayList<>();


	private int locNumBodycal;  // number of standard bodies (and Flysticks/measurement tools in old format line)
	private int locNumFlystickOld;  // number of Flysticks in old format line
	private int locNumMeatoolOld;  // number of measurement tools in old format line
	private int locNumHandcal;  // number of FINGERTRACKING hands

	private Logger log = Logger.getLogger( DTrackSDK.class.getName() );

	private void log( String format, ParseError error )
	{
		String[] str = { format, error.toString() };
		log.log( Level.WARNING, "{0}: No {1} found", str );
	}


	/**
	 * Constructor.
	 */
	protected DTrackParser()
	{
		actFramecounter = 0;
		actTimestamp = -1;
	}

	/**
	 * Set default values at start of a new frame.
	 */
	protected void startFrame()
	{
		actFramecounter = 0;
		actTimestamp = -1;
		locNumBodycal = locNumHandcal = -1;
		locNumFlystickOld = locNumMeatoolOld = 0;
	}

	/**
	 * Final adjustments after processing all data for a frame.
	 */
	protected void endFrame()
	{
		// correct number of standard bodies, if necessary:

		if ( locNumBodycal >= 0 )
		{
			int n = locNumBodycal - locNumFlystickOld - locNumMeatoolOld;

			if ( n != actBody.size() )
			{
				if ( n < actBody.size() )
				{
					actBody.subList( n, actBody.size() ).clear();
				}
				else
				{
					while ( actBody.size() < n )
						actBody.add( new DTrackBody( actBody.size() ) );
				}
			}
		}

		// correct number of FINGERTRACKING hands, if necessary:

		if ( locNumHandcal >= 0 && locNumHandcal != actHand.size() )
		{
			if ( locNumHandcal < actHand.size() )
			{
				actHand.subList( locNumHandcal, actHand.size() ).clear();
			}
			else
			{
				while ( actHand.size() < locNumHandcal )
					actHand.add( new DTrackHand( actHand.size() ) );
			}
		}
	}


	/**
	 * Get frame counter.
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @return Frame counter
	 */
	public final int getFrameCounter()
	{
		return actFramecounter;
	}

	/**
	 * Get timestamp.
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @return Timestamp (-1 if information not available)
	 */
	public final double getTimeStamp()
	{
		return actTimestamp;
	}

	/**
	 * Get number of calibrated standard bodies (as far as known).
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @return Number of standard bodies
	 */
	public final int getNumBody()
	{
		return actBody.size();
	}

	/**
	 * Get a list containing all standard body data.
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @return List containing all standard body data
	 */
	public final List< DTrackBody > getBody()
	{
		return actBody;
	}

	/**
	 * Get standard body data.
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @param id Id, range 0 ..
	 * @return Id-th standard body data
	 */
	public final DTrackBody getBody( int id )
	{
		return actBody.get( id );
	}

	/**
	 * Get number of Flystick data.
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @return Number of Flysticks
	 */
	public final int getNumFlystick()
	{
		return actFlystick.size();
	}

	/**
	 * Get a list containing all Flystick data.
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @return List containing all Flystick data
	 */
	public final List< DTrackFlystick > getFlystick()
	{
		return actFlystick;
	}

	/**
	 * Get Flystick data.
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @param id Id, range 0 ..
	 * @return Id-th Flystick data
	 */
	public final DTrackFlystick getFlystick( int id )
	{
		return actFlystick.get( id );
	}

	/**
	 * Get number of measurement tools.
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @return Number of measurement tools
	 */
	public final int getNumMeaTool()
	{
		return actMeatool.size();
	}

	/**
	 * Get a list containing all measurement tool data.
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @return List containing all measurement tool data
	 */
	public final List< DTrackMeaTool > getMeaTool()
	{
		return actMeatool;
	}

	/**
	 * Get measurement tool data.
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @param id Id, range 0 ..
	 * @return Id-th measurement tool data
	 */
	public final DTrackMeaTool getMeaTool( int id )
	{
		return actMeatool.get( id );
	}

	/**
	 * Get number of measurement references.
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @return Number of measurement references
	 */
	public final int getNumMeaRef()
	{
		return actMearef.size();
	}

	/**
	 * Get a list containing all measurement reference data.
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @return List containing all measurement reference data
	 */
	public final List< DTrackMeaRef > getMeaRef()
	{
		return actMearef;
	}

	/**
	 * Get measurement reference data.
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @param id Id, range 0 ..
	 * @return Id-th measurement reference data
	 */
	public final DTrackMeaRef getMeaRef( int id )
	{
		return actMearef.get( id );
	}

	/**
	 * Get number of FINGERTRACKING hands (as far as known).
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @return Number of FINGERTRACKING hands
	 */
	public final int getNumHand()
	{
		return actHand.size();
	}

	/**
	 * Get a list containing all FINGERTRACKING hand data.
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @return List containing all FINGERTRACKING hand data
	 */
	public final List< DTrackHand > getHand()
	{
		return actHand;
	}

	/**
	 * Get FINGERTRACKING hand data.
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @param id Id, range 0 ..
	 * @return Id-th FINGERTRACKING hand data
	 */
	public final DTrackHand getHand( int id )
	{
		return actHand.get( id );
	}

	/**
	 * Get number of ART-Human models.
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @return Number of human models
	 */
	public final int getNumHuman()
	{
		return actHuman.size();
	}

	/**
	 * Get a list containing all ART-Human model data.
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @return List containing all human model data
	 */
	public final List< DTrackHuman > getHuman()
	{
		return actHuman;
	}

	/**
	 * Get ART-Human model data.
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @param id Id, range 0 ..
	 * @return Id-th human model data
	 */
	public final DTrackHuman getHuman( int id )
	{
		return actHuman.get( id );
	}

	/**
	 * Get number of hybrid bodies.
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @return Number of hybrid bodies
	 */
	public final int getNumInertial()
	{
		return actInertial.size();
	}

	/**
	 * Get a list containing all hybrid body data.
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @return List containing all hybrid body data
	 */
	public final List< DTrackInertial > getInertial()
	{
		return actInertial;
	}

	/**
	 * Get hybrid body data.
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @param id Id, range 0 ..
	 * @return Id-th hybrid body data
	 */
	public final DTrackInertial getInertial( int id )
	{
		return actInertial.get( id );
	}

	/**
	 * Get number of tracked single markers.
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @return Number of tracked single markers
	 */
	public final int getNumMarker()
	{
		return actMarker.size();
	}

	/**
	 * Get a list containing all marker data.
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @return List containing all marker data
	 */
	public final List< DTrackMarker > getMarker()
	{
		return actMarker;
	}

	/**
	 * Get single marker data.
	 * <p>
	 * Refers to last received frame.
	 * 
	 * @param index Index, range 0 ..
	 * @return Index-th single marker data
	 */
	public final DTrackMarker getMarker( int index )
	{
		return actMarker.get( index );
	}


	/**
	 * Parses a DTrack String (eg. from a UDP package). Stops parsing when an error occurred or the String
	 * has been entirely processed.
	 * 
	 * @param string A DTrack String
	 * @return {@code true} String has been parsed correctly; if failed, see log for further information
	 */
	protected boolean parse( String string )
	{
		if ( string == null )
		{
			return false;
		}
		DTrackParse parse = new DTrackParse( string );

		do
		{
			if ( ! parseLine( parse ) )
			{
				log.log( Level.SEVERE, "Parsing failed" );
				return false;
			}
		}
		while ( parse.nextLine() );

		return true;
	}

	/**
	 * Parses a label and its contents. Saves objects in the relevant lists. <br>
	 * Unknown labels are ignored and skipped (see log).
	 * 
	 * @param parse Object parsing current line
	 * @return {@code true} Line has been parsed correctly; if failed, see log for further information
	 *         <b>Unknown labels always return {@code true}.</b>
	 */
	private boolean parseLine( DTrackParse parse )
	{
		if ( ! parse.nextStringword() )
		{
			return false;
		}
		String label = parse.getNextStringword();

		if ( label.compareTo( "fr" ) == 0 )
		{
			return parseFr( parse );
		}

		if ( label.compareTo( "ts" ) == 0 )
		{
			return parseTs( parse );
		}

		if ( label.compareTo( "6d" ) == 0 )
		{
			return parse6d( parse );
		}

		if ( label.compareTo( "6dcal" ) == 0 )
		{
			return parse6dcal( parse );
		}

		if ( label.compareTo( "6dcov" ) == 0 )
		{
			return parse6dcov( parse );
		}

		if ( label.compareTo( "6df" ) == 0 )
		{
			return parse6df( parse );
		}

		if ( label.compareTo( "6df2" ) == 0 )
		{
			return parse6df2( parse );
		}

		if ( label.compareTo( "6dmt" ) == 0 )
		{
			return parse6dmt( parse );
		}

		if ( label.compareTo( "6dmt2" ) == 0 )
		{
			return parse6dmt2( parse );
		}

		if ( label.compareTo( "6dmtr" ) == 0 )
		{
			return parse6dmtr( parse );
		}

		if ( label.compareTo( "glcal" ) == 0 )
		{
			return parseGlcal( parse );
		}

		if ( label.compareTo( "gl" ) == 0 )
		{
			return parseGl( parse );
		}

		if ( label.compareTo( "6dj" ) == 0 )
		{
			return parse6dj( parse );
		}

 		if ( label.compareTo( "6di" ) == 0 )
		{
			return parse6di( parse );
		}

		if ( label.compareTo( "3d" ) == 0 )
		{
			return parse3d( parse );
		}

		log.log( Level.WARNING, "Skipped unsupported label \"{0}\"", label );
		return true;
	}

	/**
	 * Parses single marker data. <br>
	 * Updates or creates DTrackMarker objects in actMarker.
	 * 
	 * @param parse Object parsing current line
	 * @return {@code true} Line has been parsed correctly; if failed, see log for further information
	 * @see DTrackMarker
	 */
	private boolean parse3d( DTrackParse parse )
	{
		final String FORMAT = "3d";

		if ( ! parse.nextInt() )
		{
			log( FORMAT, ParseError.INT );
			return false;
		}
		int n = parse.getNextInt();  // number of tracked single markers

		if ( n < actMarker.size() )
		{
			actMarker.subList( n, actMarker.size() ).clear();
		}

		for ( int i = 0; i < n; i++ )
		{
			DTrackParse.Block b = parse.parseBlock( "id" );
			if ( b == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}

			DTrackParse.Block b2 = parse.parseBlock( D3 );
			if ( b2 == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			double[] loc = b2.getAllD();

			if ( actMarker.size() > i )
			{
				actMarker.set( i, new DTrackMarker( b.getI( 0 ), b.getD( 0 ), loc ) );
			} else {
				actMarker.add( i, new DTrackMarker( b.getI( 0 ), b.getD( 0 ), loc ) );
			}
		}

		return true;
	}

	/**
	 * Parses 6di inertial data. <br>
	 * Updates or creates DTrackInertial objects in actInertial.
	 * 
	 * @param parse Object parsing current line
	 * @return {@code true} Line has been parsed correctly; if failed, see log for further information
	 * @see DTrackInertial
	 */
	private boolean parse6di( DTrackParse parse )
	{
		final String FORMAT = "6di";

		if ( ! parse.nextInt() )
		{
			log( FORMAT, ParseError.INT );
			return false;
		}
		int n = parse.getNextInt();  // number of defined standard bodies

		if ( n != actInertial.size() )
		{
			if ( n < actInertial.size() )
			{
				actInertial.subList( n, actInertial.size() ).clear();
			}
			else
			{
				while ( actInertial.size() < n )
					actInertial.add( new DTrackInertial( actInertial.size() ) );
			}
		}

		for ( int i = 0; i < n; i++ )
		{
			DTrackParse.Block b = parse.parseBlock( "iid" );
			if ( b == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			int id = b.getI( 0 );

			if ( id < 0 || id >= n )
			{
				log( FORMAT, ParseError.INVALID );
				return false;
			}

			DTrackParse.Block b2 = parse.parseBlock( D3 );
			if ( b2 == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			double[] loc = b2.getAllD();

			DTrackParse.Block b3 = parse.parseBlock( D9 );
			if ( b3 == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			double[][] rot = new double[ 3 ][ 3 ];
			oneToTwoDimRot( rot, b3.getAllD() );

			actInertial.set( id, new DTrackInertial( id, b.getI( 1 ), b.getD( 0 ), loc, rot ) );
		}

		return true;
	}

	/**
	 * Parses 6dj ART-Human model data. <br>
	 * Updates or creates DTrackHuman objects in actHuman.
	 * 
	 * @param parse Object parsing current line
	 * @return {@code true} Line has been parsed correctly; if failed, see log for further information
	 * @see DTrackHuman
	 */
	private boolean parse6dj( DTrackParse parse )
	{
		final String FORMAT = "6dj";

		if ( ! parse.nextInt() )
		{
			log( FORMAT, ParseError.INT );
			return false;
		}
		int n = parse.getNextInt();  // number of defined human models

		if ( n != actHuman.size() )
		{
			if ( n < actHuman.size() )
			{
				actHuman.subList( n, actHuman.size() ).clear();
			}
			else
			{
				while ( actHuman.size() < n )
					actHuman.add( new DTrackHuman( actHuman.size() ) );
			}
		}

		for ( DTrackHuman hu : actHuman )
		{
			hu.setNotTracked();
		}

		if ( ! parse.nextInt() )
		{
			log( FORMAT, ParseError.INT );
			return false;
		}
		int m = parse.getNextInt();  // number of human models in following list

		for ( int i = 0; i < m; i++ )
		{
			DTrackParse.Block b = parse.parseBlock( "ii" );
			if ( b == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			int id = b.getI( 0 );
			int numJoints = b.getI( 1 );

			if ( id < 0 || id >= n )
			{
				log( FORMAT, ParseError.INVALID );
				return false;
			}

			DTrackJoint[] joints = new DTrackJoint[ numJoints ];
			for ( int j = 0; j < numJoints; j++ )
			{
				DTrackParse.Block b2 = parse.parseBlock( "id" );
				if ( b2 == null )
				{
					log( FORMAT, ParseError.BLOCK );
					return false;
				}

				DTrackParse.Block b3 = parse.parseBlock( D3 );
				if ( b3 == null )
				{
					log( FORMAT, ParseError.BLOCK );
					return false;
				}
				double[] loc = b3.getAllD();

				DTrackParse.Block b4 = parse.parseBlock( D9 );
				if ( b4 == null )
				{
					log( FORMAT, ParseError.BLOCK );
					return false;
				}
				double[][] rot = new double[ 3 ][ 3 ];
				oneToTwoDimRot( rot, b4.getAllD() );

				joints[ j ] = new DTrackJoint( b2.getI( 0 ), b2.getD( 0 ), loc, rot );
			}

			actHuman.set( id, new DTrackHuman( id, joints ) );
		}

		return true;
	}

	/**
	 * Parses standard body data. <br>
	 * Updates or creates DTrackBody objects in actBody.
	 * 
	 * @param parse Object parsing current line
	 * @return {@code true} Line has been parsed correctly; if failed, see log for further information
	 * @see DTrackBody
	 */
	private boolean parse6d( DTrackParse parse )
	{
		final String FORMAT = "6d";

		for ( DTrackBody bd : actBody )
		{
			bd.setNotTracked();
		}

		if ( ! parse.nextInt() )
		{
			log( FORMAT, ParseError.INT );
			return false;
		}
		int m = parse.getNextInt();  // number of standard bodies in following list

		for ( int i = 0; i < m; i++ )
		{
			DTrackParse.Block b = parse.parseBlock( "id" );
			if ( b == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			int id = b.getI( 0 );

			if ( id < 0 )
			{
				log( FORMAT, ParseError.INVALID );
				return false;
			}

			if ( id >= actBody.size() )
			{
				while ( actBody.size() <= id )
					actBody.add( new DTrackBody( actBody.size() ) );
			}

			DTrackParse.Block b2 = parse.parseBlock( D3 );
			if ( b2 == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			double[] loc = b2.getAllD();

			DTrackParse.Block b3 = parse.parseBlock( D9 );
			if ( b3 == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			double[][] rot = new double[ 3 ][ 3 ];
			oneToTwoDimRot( rot, b3.getAllD() );

			actBody.set( id, new DTrackBody( id, b.getD( 0 ), loc, rot ) );
		}

		return true;
	}

	/**
	 * Parses additional information about number of calibrated bodies. <br>
	 * Saves the result in locNumBodycal.
	 * 
	 * @param parse Object parsing current line
	 * @return {@code true} Line has been parsed correctly; if failed, see log for further information
	 */
	private boolean parse6dcal( DTrackParse parse )
	{
		final String FORMAT = "6dcal";

		if ( ! parse.nextInt() )
		{
			log( FORMAT, ParseError.INT );
			return false;
		}
		locNumBodycal = parse.getNextInt();

		return true;
	}

	/**
	 * Parses 6d covariance data. <br>
	 * Updates or creates DTrackBody objects in actBody.
	 * 
	 * @param parse Object parsing current line
	 * @return {@code true} Line has been parsed correctly; if failed, see log for further information
	 * @see DTrackBody
	 */
	private boolean parse6dcov( DTrackParse parse )
	{
		final String FORMAT = "6dcov";

		// tracking stati are reset in 'parse6d()'

		if ( ! parse.nextInt() )
		{
			log( FORMAT, ParseError.INT );
			return false;
		}
		int m = parse.getNextInt();  // number of standard bodies in following list

		for ( int i = 0; i < m; i++ )
		{
			DTrackParse.Block b = parse.parseBlock( "iddd" );
			if ( b == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			int id = b.getI( 0 );
			double[] covref = b.getAllD();

			if ( id < 0 )
			{
				log( FORMAT, ParseError.INVALID );
				return false;
			}

			if ( id >= actBody.size() )
			{
				while ( actBody.size() <= id )
					actBody.add( new DTrackBody( actBody.size() ) );
			}

			DTrackParse.Block b2 = parse.parseBlock( D21 );
			if ( b2 == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			double[][] cov = new double[ 6 ][ 6 ];
			reducedToFullCov( cov, b2.getAllD(), 6 );

			actBody.get( id ).setCov( cov, covref );
		}

		return true;
	}

	/**
	 * Parses Flystick data (older format). <br>
	 * Updates or creates DTrackFlystick objects in actFlystick.
	 * 
	 * @param parse Object parsing current line
	 * @return {@code true} Line has been parsed correctly; if failed, see log for further information
	 * @see DTrackFlystick
	 */
	private boolean parse6df( DTrackParse parse )
	{
		final String FORMAT = "6df";

		if ( ! parse.nextInt() )
		{
			log( FORMAT, ParseError.INT );
			return false;
		}
		int n = parse.getNextInt();  // number of defined Flysticks
		locNumFlystickOld = n;

		if ( n != actFlystick.size() )
		{
			if ( n < actFlystick.size() )
			{
				actFlystick.subList( n, actFlystick.size() ).clear();
			}
			else
			{
				while ( actFlystick.size() < n )
					actFlystick.add( new DTrackFlystick( actFlystick.size() ) );
			}
		}

		for ( int i = 0; i < n; i++ )
		{
			DTrackParse.Block b = parse.parseBlock( "idi" );
			if ( b == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			int id = b.getI( 0 );
			int bc = b.getI( 1 );

			if ( id < 0 || id >= n )
			{
				log( FORMAT, ParseError.INVALID );
				return false;
			}

			int numBut = 8;
			int[] but = new int[ numBut ];
			for ( int j = 0; j < numBut; j++ )
			{
				but[ j ] = bc & 0x01;
				bc >>= 1;
			}

			int numJoy = 2;
			double[] joy = new double[ numJoy ];
			if ( but[ 5 ] != 0 )
			{
				joy[ 0 ] = -1;
			}
			else if ( but[ 7 ] != 0 )
			{
				joy[ 0 ] = 1;
			}
			else
			{
				joy[ 0 ] = 0;
			}
			if ( but[ 4 ] != 0 )
			{
				joy[ 1 ] = -1;
			}
			else if ( but[ 6 ] != 0 )
			{
				joy[ 1 ] = 1;
			}
			else
			{
				joy[ 1 ] = 0;
			}

			DTrackParse.Block b2 = parse.parseBlock( D3 );
			if ( b2 == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			double[] loc = b2.getAllD();

			DTrackParse.Block b3 = parse.parseBlock( D9 );
			if ( b3 == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			double[][] rot = new double[ 3 ][ 3 ];
			oneToTwoDimRot( rot, b3.getAllD() );

			actFlystick.set( id, new DTrackFlystick( id, b.getD( 0 ), but, joy, loc, rot ) );
		}

		return true;
	}

	/**
	 * Parses Flystick data (newer format). <br>
	 * Updates or creates DTrackFlystick objects in actFlystick.
	 * 
	 * @param parse Object parsing current line
	 * @return {@code true} Line has been parsed correctly; if failed, see log for further information
	 * @see DTrackFlystick
	 */
	private boolean parse6df2( DTrackParse parse )
	{
		final String FORMAT = "6df2";

		if ( ! parse.nextInt() )
		{
			log( FORMAT, ParseError.INT );
			return false;
		}
		int n = parse.getNextInt();  // number of defined Flysticks

		if ( n != actFlystick.size() )
		{
			if ( n < actFlystick.size() )
			{
				actFlystick.subList( n, actFlystick.size() ).clear();
			}
			else
			{
				while ( actFlystick.size() < n )
					actFlystick.add( new DTrackFlystick( actFlystick.size() ) );
			}
		}

		for ( DTrackFlystick fly : actFlystick )
		{
			fly.setNotTracked();
		}

		if ( ! parse.nextInt() )
		{
			log( FORMAT, ParseError.INT );
			return false;
		}
		int m = parse.getNextInt();  // number of Flysticks in following list

		for ( int i = 0; i < m; i++ )
		{
			DTrackParse.Block b = parse.parseBlock( "idii" );
			if ( b == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			int id = b.getI( 0 );
			int numBut = b.getI( 1 );
			int numJoy = b.getI( 2 );

			if ( id < 0 || id >= n )
			{
				log( FORMAT, ParseError.INVALID );
				return false;
			}

			DTrackParse.Block b2 = parse.parseBlock( D3 );
			if ( b2 == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			double[] loc = b2.getAllD();

			DTrackParse.Block b3 = parse.parseBlock( D9 );
			if ( b3 == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			double[][] rot = new double[ 3 ][ 3 ];
			oneToTwoDimRot( rot, b3.getAllD() );

			String sfmt = "";
			int j = 0;
			while ( j < numBut )
			{
				sfmt = sfmt.concat( "i" );
				j += 32;
			}
			j = 0;
			while ( j < numJoy )
			{
				sfmt = sfmt.concat( "d" );
				j++;
			}
			DTrackParse.Block b4 = parse.parseBlock( sfmt );
			if ( b4 == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			double[] joy = b4.getAllD();

			int k = 0;
			int l = 32;
			int b32 = 0;
			int[] but = new int[ numBut ];
			for ( int iBut = 0; iBut < numBut; iBut++ )
			{
				if ( l == 32 )
				{
					b32 = b4.getI( k );
					k++;
					l = 0;
				}
				but[ iBut ] = b32 & 0x01;
				b32 >>= 1;
				l++;
			}

			actFlystick.set( id, new DTrackFlystick( id, b.getD( 0 ), but, joy, loc, rot ) );
		}

		return true;
	}

	/**
	 * Parses measurement tool data (older format). <br>
	 * Updates or creates DTrackMeaTool objects in actMeatool.
	 * 
	 * @param parse Object parsing current line
	 * @return {@code true} Line has been parsed correctly; if failed, see log for further information
	 * @see DTrackMeaTool
	 */
	private boolean parse6dmt( DTrackParse parse )
	{
		final String FORMAT = "6dmt";

		if ( ! parse.nextInt() )
		{
			log( FORMAT, ParseError.INT );
			return false;
		}
		int n = parse.getNextInt();  // number of defined measurement tools
		locNumMeatoolOld = n;

		if ( n != actMeatool.size() )
		{
			if ( n < actMeatool.size() )
			{
				actMeatool.subList( n, actMeatool.size() ).clear();
			}
			else
			{
				while ( actMeatool.size() < n )
					actMeatool.add( new DTrackMeaTool( actMeatool.size() ) );
			}
		}

		for ( int i = 0; i < n; i++ )
		{
			DTrackParse.Block b = parse.parseBlock( "idi" );
			if ( b == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			int id = b.getI( 0 );

			if ( id < 0 || id >= n )
			{
				log( FORMAT, ParseError.INVALID );
				return false;
			}

			int numBut = 4;
			int k = b.getI( 1 );
			int[] but = new int[ numBut ];
			for ( int j = 0; j < numBut; j++ )
			{
				but[ j ] = k & 0x01;
				k >>= 1;
			}

			DTrackParse.Block b2 = parse.parseBlock( D3 );
			if ( b2 == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			double[] loc = b2.getAllD();

			DTrackParse.Block b3 = parse.parseBlock( D9 );
			if ( b3 == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			double[][] rot = new double[ 3 ][ 3 ];
			oneToTwoDimRot( rot, b3.getAllD() );

			actMeatool.set( id, new DTrackMeaTool( id, b.getD( 0 ), but, loc, rot ) );
		}

		return true;
	}

	/**
	 * Parses measurement tool data (newer format). <br>
	 * Updates or creates DTrackMeaTool objects in actMeatool.
	 * 
	 * @param parse Object parsing current line
	 * @return {@code true} Line has been parsed correctly; if failed, see log for further information
	 * @see DTrackMeaTool
	 */
	private boolean parse6dmt2( DTrackParse parse )
	{
		final String FORMAT = "6dmt2";

		if ( ! parse.nextInt() )
		{
			log( FORMAT, ParseError.INT );
			return false;
		}
		int n = parse.getNextInt();  // number of defined measurement tools

		if ( n != actMeatool.size() )
		{
			if ( n < actMeatool.size() )
			{
				actMeatool.subList( n, actMeatool.size() ).clear();
			}
			else
			{
				while ( actMeatool.size() < n )
					actMeatool.add( new DTrackMeaTool( actMeatool.size() ) );
			}
		}

		for ( DTrackMeaTool mt : actMeatool )
		{
			mt.setNotTracked();
		}

		if ( ! parse.nextInt() )
		{
			log( FORMAT, ParseError.INT );
			return false;
		}
		int m = parse.getNextInt();  // number of measurement tools in following list

		for ( int i = 0; i < m; i++ )
		{
			DTrackParse.Block b = parse.parseBlock( "idid" );
			if ( b == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			int id = b.getI( 0 );

			if ( id < 0 || id >= n )
			{
				log( FORMAT, ParseError.INVALID );
				return false;
			}
			int numBut = b.getI( 1 );

			DTrackParse.Block b2 = parse.parseBlock( D3 );
			if ( b2 == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			double[] loc = b2.getAllD();

			DTrackParse.Block b3 = parse.parseBlock( D9 );
			if ( b3 == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			double[][] rot = new double[ 3 ][ 3 ];
			oneToTwoDimRot( rot, b3.getAllD() );

			String sfmt = "";
			int j = 0;
			while ( j < numBut )
			{
				sfmt = sfmt.concat( "i" );
				j += 32;
			}
			DTrackParse.Block b4 = parse.parseBlock( sfmt );
			if ( b4 == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}

			int k = 0;
			int l = 32;
			int b32 = 0;
			int[] but = new int[ numBut ];
			for ( int iBut = 0; iBut < numBut; iBut++ )
			{
				if ( l == 32 )
				{
					b32 = b4.getI( k );
					k++;
					l = 0;
				}
				but[ iBut ] = b32 & 0x01;
				b32 >>= 1;
				l++;
			}

			DTrackParse.Block b5 = parse.parseBlock( D6 );
			if ( b5 == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			double[][] cov = new double[ 3 ][ 3 ];
			reducedToFullCov( cov, b5.getAllD(), 3 );

			actMeatool.set( id, new DTrackMeaTool( id, b.getD( 0 ), b.getD( 1 ), but, loc, rot, cov ) );
		}

		return true;
	}

	/**
	 * Parses measurement reference data. <br>
	 * Updates or creates DTrackMeaRef objects in actMearef.
	 * 
	 * @param parse Object parsing current line
	 * @return {@code true} Line has been parsed correctly; if failed, see log for further information
	 * @see DTrackMeaRef
	 */
	private boolean parse6dmtr( DTrackParse parse )
	{
		final String FORMAT = "6dmtr";

		if ( ! parse.nextInt() )
		{
			log( FORMAT, ParseError.INT );
			return false;
		}
		int n = parse.getNextInt();  // number of defined measurement references

		if ( n != actMearef.size() )
		{
			if ( n < actMearef.size() )
			{
				actMearef.subList( n, actMearef.size() ).clear();
			}
			else
			{
				while ( actMearef.size() < n )
					actMearef.add( new DTrackMeaRef( actMearef.size() ) );
			}
		}

		for ( DTrackMeaRef mr : actMearef )
		{
			mr.setNotTracked();
		}

		if ( ! parse.nextInt() )
		{
			log( FORMAT, ParseError.INT );
			return false;
		}
		int m = parse.getNextInt();  // number of measurement references in following list

		for ( int i = 0; i < m; i++ )
		{
			DTrackParse.Block b = parse.parseBlock( "id" );
			if ( b == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			int id = b.getI( 0 );

			if ( id < 0 || id >= n )
			{
				log( FORMAT, ParseError.INVALID );
				return false;
			}

			DTrackParse.Block b2 = parse.parseBlock( D3 );
			if ( b2 == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			double[] loc = b2.getAllD();

			DTrackParse.Block b3 = parse.parseBlock( D9 );
			if ( b3 == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			double[][] rot = new double[ 3 ][ 3 ];
			oneToTwoDimRot( rot, b3.getAllD() );

			actMearef.set( id, new DTrackMeaRef( id, b.getD( 0 ), loc, rot ) );
		}

		return true;
	}

	/**
	 * Parses a frame counter paragraph. <br>
	 * Saves the result in actFramecounter.
	 * 
	 * @param parse Object parsing current line
	 * @return {@code true} Line has been parsed correctly; if failed, see log for further information
	 */
	private boolean parseFr( DTrackParse parse )
	{
		final String FORMAT = "fr";

		if ( ! parse.nextInt() )
		{
			log( FORMAT, ParseError.INT );
			return false;
		}

		actFramecounter = parse.getNextInt();
		return true;
	}

	/**
	 * Parses A.R.T. FINGERTRACKING hand data. <br>
	 * Updates or creates DTrackHand objects in actHand.
	 * 
	 * @param parse Object parsing current line
	 * @return {@code true} Line has been parsed correctly; if failed, see log for further information
	 * @see DTrackHand
	 */
	private boolean parseGl( DTrackParse parse )
	{
		final String FORMAT = "gl";

		for ( DTrackHand gl : actHand )
		{
			gl.setNotTracked();
		}

		if ( ! parse.nextInt() )
		{
			log( FORMAT, ParseError.INT );
			return false;
		}
		int m = parse.getNextInt();  // number of tracked hands in following list

		for ( int i = 0; i < m; i++ )
		{
			DTrackParse.Block b = parse.parseBlock( "idii" );
			if ( b == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			int id = b.getI( 0 );
			int numFinger = b.getI( 2 );

			if ( id < 0 )
			{
				log( FORMAT, ParseError.INVALID );
				return false;
			}

			if ( id >= actHand.size() )
			{
				while ( actHand.size() <= id )
					actHand.add( new DTrackHand( actHand.size() ) );
			}

			DTrackParse.Block b2 = parse.parseBlock( D3 );
			if ( b2 == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			double[] loc = b2.getAllD();

			DTrackParse.Block b3 = parse.parseBlock( D9 );
			if ( b3 == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			double[][] rot = new double[ 3 ][ 3 ];
			oneToTwoDimRot( rot, b3.getAllD() );

			DTrackFinger[] finger = new DTrackFinger[ numFinger ];
			if ( ! parseFingerGl( parse, id, numFinger, finger ) )
			{
				return false;
			}

			actHand.set( id, new DTrackHand( id, b.getI( 1 ), b.getD( 0 ), loc, rot, finger ) );
		}

		return true;
	}

	private boolean parseFingerGl( DTrackParse parse, int handID, int nFinger, DTrackFinger[] finger )
	{
		final String FORMAT = "gl";

		for ( int j = 0; j < nFinger; j++ )
		{
			DTrackParse.Block b4 = parse.parseBlock( D3 );
			if ( b4 == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			double[] loc = b4.getAllD();

			DTrackParse.Block b5 = parse.parseBlock( D9 );
			if ( b5 == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			double[][] rot = new double[ 3 ][ 3 ];
			oneToTwoDimRot( rot, b5.getAllD() );

			DTrackParse.Block b6 = parse.parseBlock( D6 );
			if ( b6 == null )
			{
				log( FORMAT, ParseError.BLOCK );
				return false;
			}
			double[] lengthphalanx = { b6.getD( 1 ), b6.getD( 3 ), b6.getD( 5 ) };
			double[] anglephalanx = { b6.getD( 2 ), b6.getD( 4 ) };

			finger[ j ] = new DTrackFinger( b6.getD( 0 ), loc, rot, lengthphalanx, anglephalanx );
		}

		return true;
	}

	/**
	 * Parses additional information about the number of FINGERTRACKING hands. <br>
	 * Updates locNumHandcal.
	 * 
	 * @param parse Object parsing current line
	 * @return {@code true} Line has been parsed correctly; if failed, see log for further information
	 */
	private boolean parseGlcal( DTrackParse parse )
	{
		final String FORMAT = "glcal";

		if ( ! parse.nextInt() )
		{
			log( FORMAT, ParseError.INT );
			return false;
		}

		locNumHandcal = parse.getNextInt();
		return true;
	}

	/**
	 * Parses a timestamp paragraph. <br>
	 * Updates actTimestamp.
	 * 
	 * @param parse Object parsing current line
	 * @return {@code true} Line has been parsed correctly; if failed, see log for further information
	 */
	private boolean parseTs( DTrackParse parse )
	{
		final String FORMAT = "ts";

		if ( ! parse.nextDouble() )
		{
			log( FORMAT, ParseError.DOUBLE );
			return false;
		}

		actTimestamp = parse.getNextDouble();
		return true;
	}
}

