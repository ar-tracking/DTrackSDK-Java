/*
 * DTrackMeaRef: Java source file, A.R.T. GmbH
 *
 * DTrackMeaRef: class to define an object used for measurement reference data
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

/**
 * Measurement reference data (6DOF).
 *
 */
public class DTrackMeaRef
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
	 * Location (in [mm])
	 */
	private double[] loc;
	/**
	 * Rotation matrix (3x3-dimensional)
	 */
	private double[][] rot;


	/**
	 * Constructor for not tracked measurement reference.
	 *
	 * @param id ID number (starting with 0)
	 */
	protected DTrackMeaRef( int id )
	{
		this.id = id;
		this.quality = -1.0;
		this.loc = new double[ 3 ];
		this.rot = new double[ 3 ][ 3 ];
	}

	/**
	 * Constructor for tracked measurement reference.
	 *
	 * @param id ID number (starting with 0)
	 * @param quality Quality (0 &le; qu &le; 1, no tracking if -1)
	 * @param loc Location (in [mm])
	 * @param rot Rotation matrix (3x3-dimensional)
	 */
	protected DTrackMeaRef( int id, double quality, double[] loc, double[][] rot )
	{
		this.id = id;
		this.quality = quality;
		this.loc = loc;
		this.rot = rot;
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
	 * Returns if body is currently tracked.
	 *
	 * @return Body is tracked
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
	 * Returns location.
	 *
	 * @return Location (in [mm])
	 */
	public double[] getLoc()
	{
		return loc;
	}

	/**
	 * Returns orientation.
	 *
	 * @return Rotation matrix (3x3-dimensional)
	 */
	public double[][] getRot()
	{
		return rot;
	}


	/**
	 * Set to 'not tracked'.
	 */
	protected void setNotTracked()
	{
		quality = -1.0;
	}
}

