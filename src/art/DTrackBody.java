/*
 * DTrackBody: Java source file
 *
 * DTrackBody: class to define an object used for standard body data
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
 * Standard body data (6DOF).
 *
 */
public class DTrackBody
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
	 * 6x6-dimensional covariance matrix for the 6DOF pose (with 3DOF location in [mm],
	 * 3DOF euler angles in [rad]), if available
	 */
	private double[][] cov;
	/**
	 * Reference point of covariance (in [mm]), if available
	 */
	private double[] covref;


	/**
	 * Constructor for not tracked standard body.
	 *
	 * @param id ID number (starting with 0)
	 */
	protected DTrackBody( int id )
	{
		this.id = id;
		this.quality = -1.0;
		this.loc = new double[ 3 ];
		this.rot = new double[ 3 ][ 3 ];
		this.cov = new double[ 6 ][ 6 ];
		this.covref = new double[ 3 ];
	}

	/**
	 * Constructor for tracked standard body (only pose).
	 *
	 * @param id ID number (starting with 0)
	 * @param quality Quality (0 &le; qu &le; 1, no tracking if -1)
	 * @param loc Location (in [mm])
	 * @param rot Rotation matrix (3x3-dimensional)
	 */
	protected DTrackBody( int id, double quality, double[] loc, double[][] rot )
	{
		this.id = id;
		this.quality = quality;
		this.loc = loc;
		this.rot = rot;
		this.cov = new double[ 6 ][ 6 ];
		this.covref = new double[ 3 ];
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
	 * Returns covariance matrix, if available.
	 * 
	 * @return 6x6-dimensional covariance matrix for the 6DOF pose (with 3DOF location in [mm],
	 *         3DOF euler angles in [rad])
	 */
	public double[][] getCov()
	{
		return cov;
	}

	/**
	 * Returns reference point of covariance, if available.
	 * 
	 * @return Reference point of covariance (in [mm])
	 */
	public double[] getCovref()
	{
		return covref;
	}


	/**
	 * Set to 'not tracked'.
	 */
	protected void setNotTracked()
	{
		quality = -1.0;
	}

	/**
	 * Set covariance data.
	 *
	 * @param cov 6x6-dimensional covariance matrix for the 6DOF pose (with 3DOF location in [mm],
	 *            3DOF euler angles in [rad])
	 * @param covref Reference point of covariance (in [mm])
	 */
	protected void setCov( double[][] cov, double[] covref )
	{
		this.cov = cov;
		this.covref = covref;
	}
}

