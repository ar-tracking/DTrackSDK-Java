/*
 * DTrackFinger: Java source file, A.R.T. GmbH
 *
 * DTrackFinger: class to define an object used for FINGERTRACKING finger data
 *
 * Copyright (c) 2018-2019, Advanced Realtime Tracking GmbH
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
 * A.R.T FINGERTRACKING finger data used by FINGERTRACKING hand.
 * 
 * @see DTrackHand
 */
public class DTrackFinger
{
	/**
	 * Location (in [mm])
	 */
	private double[] loc;
	/**
	 * Rotation matrix (3x3-dimensional)
	 */
	private double[][] rot;
	/**
	 * Radius of tip (in [mm])
	 */
	private double radiusTip;
	/**
	 * Length of phalanxes (3 values, order: outermost, middle, innermost; in [mm])
	 */
	private double[] lengthPhalanx;
	/**
	 * Angle between phalanxes (2 values, order: outermost, innermost; in [deg])
	 */
	private double[] anglePhalanx;


	/**
	 * Constructor for tracked finger.
	 *
	 * @param radiusTip Radius of tip (in [mm])
	 * @param loc Location (in [mm])
	 * @param rot Rotation matrix (3x3-dimensional)
	 * @param lengthPhalanx Length of phalanxes (3 values, order: outermost, middle, innermost; in [mm])
	 * @param anglePhalanx Angle between phalanxes (2 values, order: outermost, innermost; in [deg])
	 */
	protected DTrackFinger( double radiusTip, double[] loc, double[][] rot,
			double[] lengthPhalanx, double[] anglePhalanx )
	{
		this.radiusTip = radiusTip;
		this.loc = loc;
		this.rot = rot;
		this.lengthPhalanx = lengthPhalanx;
		this.anglePhalanx = anglePhalanx;
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
	 * Returns radius of finger tip.
	 *
	 * @return Radius of tip (in [mm])
	 */
	public double getRadiusTip()
	{
		return radiusTip;
	}

	/**
	 * Returns length of phalanxes.
	 *
	 * @return Length of phalanxes (3 values, order: outermost, middle, innermost; in [mm])
	 */
	public double[] getLengthPhalanx()
	{
		return lengthPhalanx;
	}

	/**
	 * Returns angle between phalanxes.
	 *
	 * @return Angle between phalanxes (2 values, order: outermost, innermost; in [deg])
	 */
	public double[] getAnglePhalanx()
	{
		return anglePhalanx;
	}
}

