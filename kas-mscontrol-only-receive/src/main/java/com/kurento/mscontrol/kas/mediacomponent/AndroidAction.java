package com.kurento.mscontrol.kas.mediacomponent;


/**
 * AndroidAction provide some types of actions that can be run on a
 * {@link MediaComponentAndroid} (if it supports the action).
 */
public enum AndroidAction {
	/**
	 * This action can be run on a {@link MediaComponentAndroid#VIDEO_PLAYER}
	 * object to focus the camera.
	 */
	CAMERA_AUTOFOCUS,

	/**
	 * This action can be run on a {@link MediaComponentAndroid#VIDEO_PLAYER}
	 * object to take a photo from the preview video.
	 */
	CAMERA_TAKEPHOTO,

	/**
	 * This action can be run on a {@link MediaComponentAndroid#VIDEO_PLAYER}
	 * object to put the flash at ON.
	 */
	CAMERA_FLASH_ON,

	/**
	 * This action can be run on a {@link MediaComponentAndroid#VIDEO_PLAYER}
	 * object to put the flash at OFF.
	 */
	CAMERA_FLASH_OFF
}
