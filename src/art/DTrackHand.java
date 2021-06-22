/*
 * DTrackHand: Java source file
 *
 * DTrackHand: class to define an object used for FINGERTRACKING hand data
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

/**
 * A.R.T FINGERTRACKING hand data (6DOF + fingers).
 *
 */
public class DTrackHand
{
	/**
	 * ID number (starting with 0)
	 */
	private int id;
	/**
	 * Quality (0 &le; qu &le; 1, no tracking if -1)
	 */
	private double quality;
	/**
	 * Left (<b>0</b>) or right (<b>1</b>) hand
	 */
	private int lr;
	/**
	 * Location of back of the hand (in [mm])
	 */
	private double[] loc;
	/**
	 * Rotation matrix of back of the hand (3x3-dimensional)
	 */
	private double[][] rot;
	/**
	 * Finger data (order: thumb, index finger, middle finger, ...)
	 * 
	 * @see DTrackFinger
	 */
	private DTrackFinger[] finger;


	/**
	 * Constructor for not tracked hand.
	 *
	 * @param id ID number (starting with 0)
	 */
	protected DTrackHand( int id )
	{
		this.id = id;
		this.lr = 0;
		this.quality = -1.0;
		this.loc = new double[ 3 ];
		this.rot = new double[ 3 ][ 3 ];
		this.finger = new DTrackFinger[ 0 ];
	}

	/**
	 * Constructor for tracked hand.
	 *
	 * @param id ID number (starting with 0)
	 * @param lr Left (<b>0</b>) or right (<b>1</b>) hand
	 * @param quality Quality (0 &le; qu &le; 1)
	 * @param loc Location of back of the hand (in [mm])
	 * @param rot Rotation matrix of back of the hand (3x3-dimensional)
	 * @param finger Finger data (order: thumb, index finger, middle finger, ...)
	 */
	protected DTrackHand( int id, int lr, double quality, double[] loc, double[][] rot,
			DTrackFinger[] finger )
	{
		this.id = id;
		this.lr = lr;
		this.quality = quality;
		this.loc = loc;
		this.rot = rot;
		this.finger = finger;
	}


	/**
	 * Returns ID number.
	 *
	 * @return ID number (starting with 0)
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * Returns if hand is currently tracked.
	 *
	 * @return Hand is tracked
	 */
	public boolean isTracked()
	{
		if ( quality < 0.0 )  return false;

		return true;
	}

	/**
	 * Returns quality.
	 *
	 * @return Quality (0 &le; qu &le; 1, no tracking if -1)
	 */
	public double getQuality()
	{
		return quality;
	}

	/**
	 * Returns handedness.
	 *
	 * @return Left (<b>0</b>) or right (<b>1</b>) hand
	 */
	public int getLr()
	{
		return lr;
	}

	/**
	 * Returns number of fingers.
	 *
	 * @return Number of fingers (max. 5)
	 */
	public int getNumFinger()
	{
		return finger.length;
	}

	/**
	 * Returns location of back of the hand.
	 *
	 * @return Location (in [mm])
	 */
	public double[] getLoc()
	{
		return loc;
	}

	/**
	 * Returns orientation of back of the hand.
	 *
	 * @return Rotation matrix (3x3-dimensional)
	 */
	public double[][] getRot()
	{
		return rot;
	}

	/**
	 * Returns all finger data.
	 * 
	 * @return All finger data (order: thumb, index finger, middle finger, ...)
	 * @see DTrackFinger
	 */
	public DTrackFinger[] getFinger()
	{
		return finger;
	}

	/**
	 * Returns a finger data at a specific index.
	 * 
	 * @param index Index of the finger
	 * @return Finger data at given index
	 * @see DTrackFinger
	 */
	public DTrackFinger getFinger( int index )
	{
		return finger[ index ];
	}


	/**
	 * Set to 'not tracked'.
	 */
	protected void setNotTracked()
	{
		quality = -1.0;
	}
}

