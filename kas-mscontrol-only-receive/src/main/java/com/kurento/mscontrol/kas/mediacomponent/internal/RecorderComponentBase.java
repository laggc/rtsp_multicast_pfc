package com.kurento.mscontrol.kas.mediacomponent.internal;

import java.util.concurrent.BlockingQueue;

import com.kurento.kas.media.rx.RxPacket;
import com.kurento.mscontrol.commons.MsControlException;
import com.kurento.mscontrol.kas.mediacomponent.AndroidInfo;

public abstract class RecorderComponentBase extends MediaComponentBase
		implements Recorder {

	private long estimatedStartTime;
	private long targetTime;
	private long lastPtsNorm;
	protected boolean isSynchronized;

	protected BlockingQueue<RxPacket> packetsQueue;

	private long n = 0;

	private boolean isRecording = false;
	protected final Object controll = new Object();

	private int maxDelay;
	private boolean syncMediaStreams;

	public RecorderComponentBase(int maxDelay, boolean syncMediaStreams) {
		this.maxDelay = maxDelay;
		this.syncMediaStreams = syncMediaStreams;
		this.estimatedStartTime = -1;
		this.isSynchronized = false;
	}

	protected synchronized boolean isRecording() {
		return isRecording;
	}

	protected synchronized void setRecording(boolean isRecording) {
		this.isRecording = isRecording;
	}

	@Override
	public synchronized boolean isSynchronize() {
		return this.isSynchronized;
	}

	@Override
	public synchronized void setSynchronize(boolean sync) {
		this.isSynchronized = sync;
	}

	@Override
	public synchronized long getEstimatedStartTime() {
		return estimatedStartTime;
	}

	public synchronized void setEstimatedStartTime(long estimatedStartTime) {
		this.estimatedStartTime = estimatedStartTime;
	}

	@Override
	public synchronized long getEstimatedFinishTime() {
		return estimatedStartTime + lastPtsNorm;
	}

	public synchronized long getTargetTime() {
		return targetTime;
	}

	public synchronized void setTargetTime(long targetPtsNorm) {
		this.targetTime = targetPtsNorm;
	}

	public synchronized void setLastPtsNorm(long ptsNorm) {
		this.lastPtsNorm = ptsNorm;
	}

	public synchronized long caclEstimatedStartTime(long ptsNorm, long rxTime) {
		long newEstStartTime = rxTime - ptsNorm;
		if (Math.abs(newEstStartTime - estimatedStartTime) > 1000)
			n = 0;

		if (n > 15)
			estimatedStartTime = (15 * estimatedStartTime + (rxTime - ptsNorm)) / 16;
		else
			estimatedStartTime = (n * estimatedStartTime + newEstStartTime)
					/ (n + 1);

		n++;
		return estimatedStartTime;
	}

	@Override
	public abstract boolean isStarted();

	@Override
	public abstract void start() throws MsControlException;

	@Override
	public abstract void stop();

	@Override
	public long getHeadTime() {
		long ptsMillis = calcPtsMillis(packetsQueue.peek());
		return ptsMillis + getEstimatedStartTime();
	}

	@Override
	public boolean hasMediaPacket() {
		return !packetsQueue.isEmpty();
	}

	@Override
	public void startRecord() {
		startRecord(-1);
	}

	@Override
	public void startRecord(long time) {
		setTargetTime(time);
		setRecording(true);
		synchronized (controll) {
			controll.notify();
		}
	}

	@Override
	public void stopRecord() {
		setRecording(false);
	}

	@Override
	public void flushTo(long time) {
		RxPacket p = packetsQueue.peek();
		while (p != null) {
			if ((calcPtsMillis(p) + getEstimatedStartTime()) > time)
				break;
			packetsQueue.remove(p);
			p = packetsQueue.peek();
		}
	}

	@Override
	public void flushAll() {
		packetsQueue.clear();
	}

	protected long calcPtsMillis(RxPacket p) {
		if (p == null)
			return -1;

		return 1000 * ((p.getPts() - p.getStartTime()) * p.getTimeBaseNum())
				/ p.getTimeBaseDen();
	}

	private static RecorderControllerComponent recorderControllerInstance = null;

	protected synchronized RecorderController getRecorderController() {
		if (!syncMediaStreams)
			return new RecorderControllerComponent(maxDelay);

		if (recorderControllerInstance == null) {
			recorderControllerInstance = new RecorderControllerComponent(
					maxDelay);
		}
		recorderControllerInstance.setMaxDelay(maxDelay);
		return recorderControllerInstance;
	}

	@Override
	public Object getInfo(AndroidInfo info) throws MsControlException {
		if (AndroidInfo.RECORDER_QUEUE.equals(info))
			return packetsQueue.size();
		throw new MsControlException("Info not found");
	}

}
