/*
 * DTrackMeaTool: Java source file
 *
 * DTrackMeaTool: class to define an object used for Measurement tool data
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
 * Measurement tool data (6DOF + buttons).
 *
 */
public class DTrackMeaTool
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
	 * Radius of tip if applicable (in [mm])
	 */
	private double tipRadius;
	/**
	 * Button state (<b>1</b> - Pressed, <b>0</b> - Not pressed):
	 * <ul>
	 * <li><b>0</b> - Point measurement state
	 * <li><b>1...n-1</b> - Buttons
	 * </ul>
	 */
	private int[] button;
	/**
	 * Location (in [mm])
	 */
	private double[] loc;
	/**
	 * Rotation matrix (3x3-dimensional)
	 */
	private double[][] rot;
	/**
	 * Covariance of location (3x3-dimensional; in [mm&sup2;])
	 */
	private double[][] cov;


	/**
	 * Constructor for not tracked Measurement Tool, without tip radius and covariance.
	 *
	 * @param id ID number (starting with 0)
	 */
	protected DTrackMeaTool( int id )
	{
		this.id = id;
		this.quality = -1.0;
		this.tipRadius = 0.0;
		this.button = new int[ 0 ];
		this.loc = new double[ 3 ];
		this.rot = new double[ 3 ][ 3 ];
		this.cov = new double[ 3 ][ 3 ];
	}

	/**
	 * Constructor for tracked and not tracked Measurement Tool, with tip radius and covariance.
	 *
	 * @param id ID number (starting with 0)
	 * @param quality Quality (0 &le; qu &le; 1, no tracking if -1)
	 * @param tipradius Radius of tip if applicable (in [mm])
	 * @param button Button state (0, 1)
	 * @param loc Location (in [mm])
	 * @param rot Rotation matrix (3x3-dimensional)
	 * @param cov Covariance of location (3x3-dimensional; in [mm&sup2;])
	 */
	protected DTrackMeaTool( int id, double quality, double tipradius, int[] button,
			double[] loc, double[][] rot, double[][] cov )
	{
		this.id = id;
		this.quality = quality;
		this.tipRadius = tipradius;
		this.button = button;
		this.loc = loc;
		this.rot = rot;
		this.cov = cov;
	}

	/**
	 * Constructor for tracked and not tracked Measurement Tool, without tip radius and covariance.
	 *
	 * @param id ID number (starting with 0)
	 * @param quality Quality (0 &le; qu &le; 1, no tracking if -1)
	 * @param button Button state (0, 1)
	 * @param loc Location (in [mm])
	 * @param rot Rotation matrix (3x3-dimensional)
	 */
	protected DTrackMeaTool( int id, double quality, int[] button, double[] loc, double[][] rot )
	{
		this.id = id;
		this.quality = quality;
		this.tipRadius = 0.0;
		this.button = button;
		this.loc = loc;
		this.rot = rot;
		this.cov = new double[ 3 ][ 3 ];
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
	 * Returns if Measurement tool is currently tracked.
	 *
	 * @return Measurement tool is tracked
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
	 * Returns radius of tip.
	 *
	 * @return Radius of tip if applicable (in [mm])
	 */
	public double getTipRadius()
	{
		return tipRadius;
	}

	/**
	 * Returns number of buttons.
	 *
	 * @return Number of buttons
	 */
	public int getNumButton()
	{
		return button.length;
	}

	/**
	 * Returns state of buttons.
	 *
	 * @return Button state (<b>1</b> - Pressed, <b>0</b> - Not pressed):
	 *	        <ul>
	 *	        <li><b>0</b> - Point measurement state
	 *	        <li><b>1...n-1</b> - Buttons
	 *	        </ul>
	 */
	public int[] getButton()
	{
		return button;
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
	 * Returns covariance of tip.
	 *
	 * @return Covariance of location (3x3-dimensional; in [mm&sup2;])
	 */
	public double[][] getCov()
	{
		return cov;
	}


	/**
	 * Set to 'not tracked'.
	 */
	protected void setNotTracked()
	{
		quality = -1.0;
	}
}

