/*
 * DTrackHuman: Java source file
 *
 * DTrackHuman: class to define an object used for ART-Human human model data
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
 * ART-Human model (joints (6DOF) including optional Fingertracking).
 *
 */
public class DTrackHuman
{
	/**
	 * ID number (starting with 0)
	 */
	private int id;
	/**
	 * Location and orientation of the joints
	 */
	private DTrackJoint[] joint;


	/**
	 * Constructor for not tracked human model.
	 *
	 * @param id ID number (starting with 0)
	 */
	protected DTrackHuman( int id )
	{
		this.id = id;
		this.joint = new DTrackJoint[ 0 ];
	}

	/**
	 * Constructor for tracked human model.
	 *
	 * @param id ID number (starting with 0)
	 * @param joint Location and orientation of the joints
	 */
	protected DTrackHuman( int id, DTrackJoint[] joint )
	{
		this.id = id;
		this.joint = joint;
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
	 * Returns if human is currently tracked.
	 *
	 * @return Human is tracked
	 */
	public boolean isTracked()
	{
		if ( joint.length == 0 )  return false;

		return true;
	}

	/**
	 * Returns number of joints.
	 *
	 * @return Number of joints
	 */
	public int getNumJoint()
	{
		return joint.length;
	}

	/**
	 * Returns all joint data.
	 * 
	 * @return Location and orientation of all joints
	 * @see DTrackJoint
	 */
	public DTrackJoint[] getJoint()
	{
		return joint;
	}

	/**
	 * Returns a joint data at a specific index.
	 * 
	 * @param index Index of the joint
	 * @return Joint data at given index.
	 * @see DTrackJoint
	 */
	public DTrackJoint getJoint( int index )
	{
		return joint[ index ];
	}


	/**
	 * Set to 'not tracked'.
	 */
	protected void setNotTracked()
	{
		joint = new DTrackJoint[ 0 ];
	}
}

