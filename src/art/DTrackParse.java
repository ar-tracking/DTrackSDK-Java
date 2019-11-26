/*
 * DTrackParse: Java source file, A.R.T. GmbH
 *
 * DTrackParse: functions for processing ASCII data
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
 */

package art;

import java.util.logging.Level;
import java.util.logging.Logger;

class DTrackParse
{
	private char[] c;
	private int cPtr;

	private String nextStringword;
	private int nextInt;
	private double nextDouble;

	private Logger log = Logger.getLogger( DTrackSDK.class.getName() );

	/**
	 * Class to save information about a parsed block. Getters and setters are provided.
	 *
	 */
	class Block
	{
		private double[] ddat;
		private int[] idat;
		private int dCount = 0;
		private int iCount = 0;

		/**
		 * Initializes relevant values.
		 * 
		 * @param format format string used to parse correct data type
		 */
		private Block( String format )
		{
			char[] formatC = format.toCharArray();
			int iSize = 0;
			int dSize = 0;
			for ( char fc : formatC )  // used for correct array sizes
			{
				if ( fc == 'i' )
				{
					iSize++;
				}
				else if ( fc == 'd' )
				{
					dSize++;
				}
			}
			ddat = new double[ dSize ];
			idat = new int[ iSize ];
		}

		void addD( double d )
		{
			ddat[ dCount++ ] = d;
		}

		void addI( int i )
		{
			idat[ iCount++ ] = i;
		}

		double getD( int index )
		{
			return ddat[ index ];
		}

		int getI( int index )
		{
			return idat[ index ];
		}

		double[] getAllD()
		{
			return ddat;
		}

		int[] getAllI()
		{
			return idat;
		}
	}


	/**
	 * Constructor, using a String for parsing. Parses from the beginning.
	 * 
	 * @param text String to be parsed
	 */
	protected DTrackParse( String text )
	{
		this.c = text.toCharArray();
		cPtr = 0;
	}


	/**
	 * Proceedes to next line.
	 *
	 * @return {@code true}, if there is another line.
	 */
	protected boolean nextLine()
	{
		while ( cPtr < c.length && c[ cPtr ] != '\r' && c[ cPtr ] != '\n' )
		{
			cPtr++;
		}

		while ( cPtr < c.length && ( c[ cPtr ] == '\r' || c[ cPtr ] == '\n' || c[ cPtr ] == ' ' ) )
		{
			cPtr++;
		}
		if ( cPtr >= c.length )
		{
			return false;
		}

		return true;
	}

	/**
	 * Parses next ASCII word.
	 *
	 * @return {@code true}, if successful.
	 */
	protected boolean nextStringword()
	{
		while ( cPtr < c.length && c[ cPtr ] == ' ' )
		{
			cPtr++;
		}

		int cPtr0 = cPtr;
		// Searches end of word
		while ( cPtr < c.length && c[ cPtr ] != ' ' && c[ cPtr ] != '\r' && c[ cPtr ] != '\n' )
		{
			cPtr++;
		}
		if ( cPtr >= c.length )
		{
			return false;
		}

		nextStringword = new String( c, cPtr0, cPtr - cPtr0 );
		return true;
	}

	/**
	 * Parses next integer value.
	 * 
	 * @return {@code true}, if successful.
	 */
	protected boolean nextInt()
	{
		while ( cPtr < c.length && c[ cPtr ] == ' ' )
		{
			cPtr++;
		}

		boolean neg = false;
		if ( c[ cPtr ] == '-' )
		{
			neg = true;
			cPtr++;
		}

		int val = 0;
		while ( cPtr < c.length && c[ cPtr ] >= '0' && c[ cPtr ] <= '9' )
		{
			val = val * 10 + ( c[ cPtr ] - '0' );
			cPtr++;
		}

		if ( neg )
		{
			nextInt = -val;
		}
		else
		{
			nextInt = val;
		}
		return true;
	}

	/**
	 * Parses the next double value.
	 * 
	 * @return {@code true}, if successful
	 */
	protected boolean nextDouble()
	{
		while ( cPtr < c.length && c[ cPtr ] == ' ' )
		{
			cPtr++;
		}

		int cPtr0 = cPtr;
		// Searches end of double
		while ( cPtr < c.length && c[ cPtr ] != ' ' && c[ cPtr ] != ']' && c[ cPtr ] != '\r' && c[ cPtr ] != '\n' )
		{
			cPtr++;
		}
		if ( cPtr >= c.length )
		{
			return false;
		}

		// Creates a String object to parse double (more reliable and accurate)
		String str = new String( c, cPtr0, cPtr - cPtr0 );
		try
		{
			nextDouble = Double.valueOf( str );
		}
		catch ( NumberFormatException e)
		{
			return false;
		}

		return true;
	}

	/**
	 * Parses a block [...] using a format String.
	 * 
	 * @param format Information about the content of the block:
	 *        <ul>
	 *        <li><b>i</b>: Integer
	 *        <li><b>d</b>: Double
	 *        </ul>
	 *        Multiple characters describe multiple values: "id" &rArr; [0 1.000]
	 * @return A block object, containing all Doubles and Integers.
	 * @see Block
	 */
	protected Block parseBlock( String format )
	{
		while ( cPtr < c.length && c[ cPtr ] == ' ' )
		{
			cPtr++;
		}

		if ( cPtr < c.length && c[ cPtr ] != '[' )
		{
			while ( cPtr < c.length && c[ cPtr ] != ']' )
			{
				cPtr++;
			}
			log.log( Level.WARNING, "Skipped block (no square bracket found) ({0})", cPtr );
			return null;
		}
		cPtr++;

		Block b = new Block( format );
		char[] f = format.toCharArray();
		for ( int i = 0; i < format.length(); i++ )
		{
			char formC = f[ i ];
			switch ( formC )
			{
				case 'i':
					if ( ! nextInt() )
					{
						log.log( Level.WARNING, "Skipped block (int not found) ({0})", cPtr );
						while ( cPtr < c.length && c[ cPtr ] != ']' )
						{
							cPtr++;
						}
						return null;
					}
					b.addI( nextInt );
					break;

				case 'd':
					if ( ! nextDouble() )
					{
						log.log( Level.WARNING, "Skipped block (double not found) ({0})", cPtr );
						while ( cPtr < c.length && c[ cPtr ] != ']' )
						{
							cPtr++;
						}
						return null;
					}
					b.addD( nextDouble );
					break;

				default:
					log.log( Level.WARNING, "Skipped block (format \"{0}\" undefined)", formC );
					while ( cPtr < c.length && c[ cPtr ] != ']' )
					{
						cPtr++;
					}
					return null;
			}
		}

		while ( cPtr < c.length && c[ cPtr ] != ']' )
		{
			cPtr++;
		}
		cPtr++;

		log.fine( "Block parsed successfully" );
		return b;
	}


	/**
	 * @return the string value found by {@link DTrackParse#nextStringword()}.
	 */
	protected String getNextStringword()
	{
		return nextStringword;
	}

	/**
	 * @return the integer value found by {@link DTrackParse#nextInt()}.
	 */
	protected int getNextInt()
	{
		return nextInt;
	}

	/**
	 * @return the double value found by {@link DTrackParse#nextDouble()}.
	 */
	protected double getNextDouble()
	{
		return nextDouble;
	}
}

