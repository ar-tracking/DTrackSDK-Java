/*
 * DTrackMarker: Java source file, A.R.T. GmbH
 *
 * DTrackMarker: class to define an object used for single marker data
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
 * Single marker data (3DOF).
 *
 */
public class DTrackMarker
{
	/**
	 * ID number (starting with 1)
	 */
	private int id;
	/**
	 * Quality (0 &le; qu &le; 1)
	 */
	private double quality;
	/**
	 * Location (in [mm])
	 */
	private double[] loc;


	/**
	 * Constructor for tracked single marker.
	 *
	 * @param id ID number (starting with 1)
	 * @param quality Quality (0.0 &le; qu &le; 1.0)
	 * @param loc Location (in [mm])
	 */
	protected DTrackMarker( int id, double quality, double[] loc )
	{
		this.id = id;
		this.quality = quality;
		this.loc = loc;
	}


	/**
	 * Returns ID number.
	 *
	 * @return ID number (starting with 1)
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * Returns quality.
	 *
	 * @return Quality (0 &le; qu &le; 1)
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
}

