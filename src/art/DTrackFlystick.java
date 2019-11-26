/*
 * DTrackFlystick: Java source file, A.R.T. GmbH
 *
 * DTrackFlystick: class to define an object used for Flystick data
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
 * A.R.T Flystick data (6DOF + buttons).
 *
 */
public class DTrackFlystick
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
	 * Button state (<b>1</b> - Pressed, <b>0</b> - Not pressed):
	 * <ul>
	 * <li><b>0</b> - Front
	 * <li><b>1...n-1</b> - Right to left
	 * </ul>
	 */
	private int[] button;
	/**
	 * Joystick value (-1 &le; joystick &le; 1):
	 * <ul>
	 * <li><b>0</b> - Horizontal
	 * <li><b>1</b> - Vertical
	 * </ul>
	 */
	private double[] joystick;
	/**
	 * Location (in [mm])
	 */
	private double[] loc;
	/**
	 * Rotation matrix (3x3-dimensional)
	 */
	private double[][] rot;


	/**
	 * Constructor for not tracked Flystick.
	 *
	 * @param id ID number (starting with 0)
	 */
	protected DTrackFlystick( int id )
	{
		this.id = id;
		this.quality = -1.0;
		this.button = new int[ 0 ];
		this.joystick = new double[ 0 ];
		this.loc = new double[ 3 ];
		this.rot = new double[ 3 ][ 3 ];
	}

	/**
	 * Constructor for tracked and not tracked Flystick.
	 *
	 * @param id ID number (starting with 0)
	 * @param quality Quality (0 &le; qu &le; 1, no tracking if -1)
	 * @param button Button states (0, 1)
	 * @param joystick Joystick values (-1 &le; joystick &le; 1)
	 * @param loc Location (in [mm])
	 * @param rot Rotation matrix (3x3-dimensional)
	 */
	protected DTrackFlystick( int id, double quality, int[] button, double[] joystick, double[] loc, double[][] rot )
	{
		this.id = id;
		this.quality = quality;
		this.button = button;
		this.joystick = joystick;
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
	 * Returns if Flystick is currently tracked.
	 *
	 * @return Flystick is tracked
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
	 * Returns number of buttons.
	 *
	 * @return Number of buttons
	 */
	public int getNumButton()
	{
		return button.length;
	}

	/**
	 * Returns number of joystick values.
	 *
	 * @return Number of joystick values
	 */
	public int getNumJoystick()
	{
		return joystick.length;
	}

	/**
	 * Returns state of buttons.
	 *
	 * @return Button state (<b>1</b> - Pressed, <b>0</b> - Not pressed):
	 *         <ul>
	 *         <li><b>0</b> - Front</li>
	 *         <li><b>1...n-1</b> - Right to left</li>
	 *         </ul>
	 */
	public int[] getButton()
	{
		return button;
	}

	/**
	 * Returns state of joystick values.
	 *
	 * @return Joystick value (-1 &le; joystick &le; 1):
	 *         <ul>
	 *         <li><b>0</b> - Horizontal
	 *         <li><b>1</b> - Vertical
	 *         </ul>
	 */
	public double[] getJoystick()
	{
		return joystick;
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

