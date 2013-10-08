package com.kurento.mscontrol.kas.mediacomponent.internal;

import com.kurento.mscontrol.kas.mediacomponent.MediaComponentAndroid;

public interface Recorder extends MediaComponentAndroid {

	public boolean isSynchronize();

	public void setSynchronize(boolean sync);

	public long getHeadTime();

	public long getEstimatedStartTime();

	public long getEstimatedFinishTime();

	/**
	 * 
	 * @return true if there are some media packets waiting to be recorder.
	 */
	public boolean hasMediaPacket();

	public void startRecord();

	public void startRecord(long time);

	public void stopRecord();

	public void flushTo(long time);

	public void flushAll();

}
