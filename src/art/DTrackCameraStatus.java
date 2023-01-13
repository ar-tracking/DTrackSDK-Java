/* DTrackSDK in Java: DTrackCameraStatus.java
 *
 * Class to define DTrack camera status data.
 *
 * Copyright (c) 2022-2023 Advanced Realtime Tracking GmbH & Co. KG
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
 */

package art;

/**
 * DTrack camera status data.
 * <p>
 * Part of DTrack system status data.
 * 
 * @see DTrackStatus
 */
public class DTrackCameraStatus 
{
	/**
	 * ID number of the camera (starting with 0)
	 */
	private int idCamera;
	/**
	 * Number of 2DOF reflections seen by this camera
	 */
	private int numReflections;
	/**
	 * Number of seen 2DOF reflections used for 6DOF tracking
	 */
	private int numReflectionsUsed;
	/**
	 * Intensity of the brightest pixel (between 0 and 10)
	 */
	private int maxIntensity;


	/**
	 * Constructor for empty camera status data.
	 * 
	 * @param id ID number of the camera (starting with 0)
	 */
	protected DTrackCameraStatus( int id )
	{
		this.idCamera = id;
		this.numReflections = 0;
		this.numReflectionsUsed = 0;
		this.maxIntensity = 0;
	}

	/**
	 * Constructor for camera status data.
	 * 
	 * @param idCamera ID number of the camera (starting with 0)
	 * @param numReflections Number of 2DOF reflections seen by this camera
	 * @param numReflectionsUsed Number of seen 2DOF reflections used for 6DOF tracking
	 * @param maxIntensity Intensity of the brightest pixel (between 0 and 10)
	 */
	protected DTrackCameraStatus( int idCamera, int numReflections, int numReflectionsUsed, int maxIntensity )
	{
		this.idCamera = idCamera;
		this.numReflections = numReflections;
		this.numReflectionsUsed = numReflectionsUsed;
		this.maxIntensity = maxIntensity;
	}

	/**
	 * Returns ID number of the camera.
	 * 
	 * @return ID number of the camera (starting with 0)
	 */
	public int getIdCamera()
	{
		return idCamera;
	}

	/**
	 * Returns number of 2DOF reflections seen by this camera.
	 * 
	 * @return Number of 2DOF reflections seen by this camera
	 */
	public int getNumReflections()
	{
		return numReflections;
	}

	/**
	 * Returns number of seen 2DOF reflections used for 6DOF tracking.
	 * 
	 * @return Number of seen 2DOF reflections used for 6DOF tracking
	 */
	public int getNumReflectionsUsed()
	{
		return numReflectionsUsed;
	}

	/**
	 * Returns intensity of the brightest pixel.
	 * 
	 * @return Intensity of the brightest pixel (between 0 and 10)
	 */
	public int getMaxIntensity()
	{
		return maxIntensity;
	}
}

