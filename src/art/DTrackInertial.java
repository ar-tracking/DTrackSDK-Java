/*
 * DTrackInertial: Java source file, A.R.T. GmbH
 *
 * DTrackInertial: class to define an object used for hybrid body data
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
 * Hybrid (optical-inertial) body data (6DOF).
 *
 */
public class DTrackInertial
{
	/**
	 * ID number (starting with 0)
	 */
	private int id;
	/**
	 * State of hybrid body (0: not tracked, 1: inertial tracking, 2: optical tracking,
	 * 3: inertial and optical tracking)
	 */
	private int state;
	/**
	 * Drift error estimate (only during inertial tracking, in [deg])
	 */
	private double error;
	/**
	 * Location (in [mm])
	 */
	private double[] loc;
	/**
	 * Rotation matrix (3x3-dimensional)
	 */
	private double[][] rot;


	/**
	 * Constructor for not tracked hybrid body.
	 *
	 * @param id ID number (starting with 0)
	 */
	protected DTrackInertial( int id )
	{
		this.id = id;
		this.state = 0;
		this.error = 0;
		this.loc = new double[ 3 ];
		this.rot = new double[ 3 ][ 3 ];
	}

	/**
	 * Constructor for tracked hybrid body.
	 *
	 * @param id ID number (starting with 0)
	 * @param state State of hybrid body (0, 1, 2 or 3)
	 * @param error Drift error estimate (only during inertial tracking, in [deg])
	 * @param loc Location (in [mm])
	 * @param rot Rotation matrix (3x3-dimensional)
	 */
	protected DTrackInertial( int id, int state, double error, double[] loc, double[][] rot )
	{
		this.id = id;
		this.state = state;
		this.error = error;
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
		if ( state == 0 )  return false;

		return true;
	}

	/**
	 * Returns state of hybrid body.
	 *
	 * @return State of hybrid body (0: not tracked, 1: inertial tracking, 2: optical tracking,
	 *         3: inertial and optical tracking)
	 */
	public int getState()
	{
		return state;
	}

	/**
	 * Returns drift error estimate.
	 *
	 * @return Drift error estimate (only during inertial tracking, in [deg])
	 */
	public double getError()
	{
		return error;
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
}

