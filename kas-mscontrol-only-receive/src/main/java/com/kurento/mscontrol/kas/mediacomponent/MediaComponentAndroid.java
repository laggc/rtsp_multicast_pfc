/*
 * Kurento Android MSControl: MSControl implementation for Android.
 * Copyright (C) 2011  Tikal Technologies
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kurento.mscontrol.kas.mediacomponent;

import android.view.ViewGroup;

import com.kurento.commons.config.Parameter;
import com.kurento.mscontrol.commons.Configuration;
import com.kurento.mscontrol.commons.MsControlException;
import com.kurento.mscontrol.commons.mediacomponent.MediaComponent;

/**
 * MediaComponentAndroid is an interface that extends MediaComponent.<br>
 * 
 * Defines a set of Configuration<MediaComponent> to create a concrete
 * MediaComponent. They must be used in
 * createMediaComponent(Configuration<MediaComponent> predefinedConfig,
 * Parameters params)
 */
public interface MediaComponentAndroid extends MediaComponent {

	/**
	 * To create a component that record audio from microphone.
	 */
	public static final Configuration<MediaComponent> AUDIO_PLAYER = new Configuration<MediaComponent>() {
	};

	/**
	 * To create a component that play audio.
	 */
	public static final Configuration<MediaComponent> AUDIO_RECORDER = new Configuration<MediaComponent>() {
	};

	/**
	 * Parameter whose value must be an Integer indicates the stream type in
	 * Android, for example AudioManager.STREAM_MUSIC.
	 */
	public static final Parameter<Integer> STREAM_TYPE = new Parameter<Integer>(
			"STREAM_TYPE");

	/**
	 * To create a component that record video from camera.
	 */
	public static final Configuration<MediaComponent> VIDEO_PLAYER = new Configuration<MediaComponent>() {
	};

	/**
	 * Parameter whose value must be an Integer as Camera_Facing_Back or
	 * Camera_Facing_Front
	 */
	public static final Parameter<Integer> CAMERA_FACING = new Parameter<Integer>(
			"CAMERA_FACING");
	
	/**
	 * Parameter whose value must be an Android View to preview the camera
	 * video.
	 */
	public static final Parameter<ViewGroup> PREVIEW_SURFACE_CONTAINER = new Parameter<ViewGroup>(
			"PREVIEW_SURFACE_CONTAINER");

	/**
	 * To create a component that show video in a display.
	 */
	public static final Configuration<MediaComponent> VIDEO_RECORDER = new Configuration<MediaComponent>() {
	};

	/**
	 * Parameter whose value must be an Android View to view the received video.
	 */
	public static final Parameter<ViewGroup> VIEW_SURFACE_CONTAINER = new Parameter<ViewGroup>(
			"VIEW_SURFACE_CONTAINER");

	/**
	 * Parameter whose value must be an Integer value that indicate the display
	 * width.
	 * 
	 * Deprecated, this parameter is ignored
	 */
	@Deprecated
	public static final Parameter<Integer> DISPLAY_WIDTH = new Parameter<Integer>(
			"DISPLAY_WIDTH");

	/**
	 * Parameter whose value must be an Integer value that indicate the display
	 * height.
	 * 
	 * Deprecated, this parameter is ignored
	 */
	@Deprecated
	public static final Parameter<Integer> DISPLAY_HEIGHT = new Parameter<Integer>(
			"DISPLAY_HEIGHT");
	
	/**
	 * Parameter whose value must be an Integer value that indicate the display
	 * orientation
	 */
	public static final Parameter<Integer> DISPLAY_ORIENTATION = new Parameter<Integer>(
			"DISPLAY_ORIENTATION");

	public boolean isStarted();

	/**
	 * Run the action requested on the component if it is supported.
	 * 
	 * @param action
	 * @throws MsControlException
	 */
	public void onAction(AndroidAction action) throws MsControlException;

	/**
	 * @param info
	 * @return the info of the media component requested if the media component
	 *         can provide this type of info.
	 * @throws MsControlException
	 */
	public Object getInfo(AndroidInfo info) throws MsControlException;

}
