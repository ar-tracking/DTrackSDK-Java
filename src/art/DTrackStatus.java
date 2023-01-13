/* DTrackSDK in Java: DTrackStatus.java
 *
 * Class to define DTrack system status data.
 *
 * Copyright (c) 2022 Advanced Realtime Tracking GmbH & Co. KG
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

import java.util.ArrayList;
import java.util.List;

/**
 * DTrack system status data.
 */
public class DTrackStatus 
{
	// general status values:

	/**
	 * Number of cameras
	 */
	private int numCameras;
	/**
	 * Number of currently tracked 6DOF bodies
	 */
	private int numTrackedBodies;
	/**
	 * Number of currently found additional 3DOF markers
	 */
	private int numTrackedMarkers;

	// message statistics:

	/**
	 * Number of camera-related error messages (since booting)
	 */
	private int numCameraErrorMessages;
	/**
	 * Number of camera-related warning messages (since booting)
	 */
	private int numCameraWarningMessages;
	/**
	 * Number of other error messages (since booting)
	 */
	private int numOtherErrorMessages;
	/**
	 * Number of other warning messages (since booting)
	 */
	private int numOtherWarningMessages;
	/**
	 * Number of info messages (since booting)
	 */
	private int numInfoMessages;

	/**
	 * Camera status
	 *
	 * @see DTrackCameraStatus
	 */
	private List< DTrackCameraStatus > cameraStatus = new ArrayList<>();


	/**
	 * Constructor for system status data.
	 * 
	 * @param numCameras Number of cameras
	 * @param numTrackedBodies Number of currently tracked 6DOF bodies
	 * @param numTrackedMarkers Number of currently found additional 3DOF markers
	 * @param numCameraErrorMessages Number of camera-related error messages (since booting)
	 * @param numCameraWarningMessages Number of camera-related warning messages (since booting)
	 * @param numOtherErrorMessages Number of other error messages (since booting)
	 * @param numOtherWarningMessages Number of other warning messages (since booting)
	 * @param numInfoMessages Number of info messages (since booting)
	 * @param cameraStatus Camera status
	 */
	protected DTrackStatus( int numCameras, int numTrackedBodies, int numTrackedMarkers,
	                        int numCameraErrorMessages, int numCameraWarningMessages, int numOtherErrorMessages,
	                        int numOtherWarningMessages, int numInfoMessages, List< DTrackCameraStatus > cameraStatus )
	{
		this.numCameras = numCameras;
		this.numTrackedBodies = numTrackedBodies;
		this.numTrackedMarkers = numTrackedMarkers;

		this.numCameraErrorMessages = numCameraErrorMessages;
		this.numCameraWarningMessages = numCameraWarningMessages;
		this.numOtherErrorMessages = numOtherErrorMessages;
		this.numOtherWarningMessages = numOtherWarningMessages;
		this.numInfoMessages = numInfoMessages;

		this.cameraStatus = cameraStatus;
	}


	/**
	 * Returns number of cameras.
	 * 
	 * @return Number of cameras
	 */
	public int getNumCameras()
	{
		return numCameras;
	}

	/**
	 * Returns number of currently tracked 6DOF bodies.
	 * 
	 * @return Number of currently tracked 6DOF bodies
	 */
	public int getNumTrackedBodies()
	{
		return numTrackedBodies;
	}

	/**
	 * Returns number of currently found additional 3DOF markers.
	 * 
	 * @return Number of currently found additional 3DOF markers
	 */
	public int getNumTrackedMarkers()
	{
		return numTrackedMarkers;
	}

	/**
	 * Returns number of camera-related error messages (since booting).
	 * 
	 * @return Number of camera-related error messages (since booting)
	 */
	public int getNumCameraErrorMessages()
	{
		return numCameraErrorMessages;
	}

	/**
	 * Returns number of camera-related warning messages (since booting).
	 * 
	 * @return Number of camera-related warning messages (since booting)
	 */
	public int getNumCameraWarningMessages()
	{
		return numCameraWarningMessages;
	}

	/**
	 * Returns number of other error messages (since booting).
	 * 
	 * @return Number of other error messages (since booting)
	 */
	public int getNumOtherErrorMessages()
	{
		return numOtherErrorMessages;
	}

	/**
	 * Returns number of other warning messages (since booting).
	 * 
	 * @return Number of other warning messages (since booting)
	 */
	public int getNumOtherWarningMessages()
	{
		return numOtherWarningMessages;
	}

	/**
	 * Returns number of info messages (since booting).
	 * 
	 * @return Number of info messages (since booting)
	 */
	public int getNumInfoMessages()
	{
		return numInfoMessages;
	}

	/**
	 * Returns camera status of all cameras.
	 * 
	 * @return Camera status of all cameras
	 */
	public List< DTrackCameraStatus > getCameraStatus()
	{
		return cameraStatus;
	}

	/**
	 * Returns camera status of one camera.
	 * 
	 * @param idCamera ID number of the camera (starting with 0)
	 * @return Camera status of one camera
	 */
	public DTrackCameraStatus getCameraStatus( int idCamera )
	{
		return cameraStatus.get( idCamera );
	}
}

