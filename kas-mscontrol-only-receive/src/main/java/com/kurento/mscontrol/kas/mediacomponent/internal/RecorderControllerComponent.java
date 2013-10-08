package com.kurento.mscontrol.kas.mediacomponent.internal;

import java.util.concurrent.CopyOnWriteArraySet;

import android.util.Log;

public class RecorderControllerComponent implements
		RecorderController {

	private static final String LOG_TAG = "RecorderController";

	private CopyOnWriteArraySet<Recorder> recorders = new CopyOnWriteArraySet<Recorder>();
	private Controller controller;

	private int maxDelay;

	public int getMaxDelay() {
		return maxDelay;
	}

	public void setMaxDelay(int maxDelay) {
		this.maxDelay = maxDelay;
	}
	public RecorderControllerComponent(int maxDelay) {
		this.maxDelay = maxDelay;
	}
	@Override
	public synchronized void addRecorder(Recorder r) {
		recorders.add(r);
		if (controller == null) {
			controller = new Controller();
			controller.start();
		}
	}

	@Override
	public synchronized void deleteRecorder(Recorder r) {
		recorders.remove(r);
		if ((controller != null) && recorders.isEmpty()) {
			controller.interrupt();
			controller = null;
		}
	}

	public static final int INC = 40;
	public static final int INTERVAL = 40;
	public static final int MAX_WAIT = 200;

	private class Controller extends Thread {
		@Override
		public void run() {
			try {
				long t, lastT;
				long globalStartTime, globalHeadTime, globalFinishTime;
				long relativeTargetTime, absoluteTargetTime;
				long latency;

				lastT = System.currentTimeMillis();
				relativeTargetTime = 0;
				absoluteTargetTime = lastT;

				for (;;) {
					t = System.currentTimeMillis();

					globalHeadTime = Long.MAX_VALUE;
					globalFinishTime = Long.MAX_VALUE;
					globalStartTime = Long.MAX_VALUE;
					long nToRecord = 0;
					for (Recorder r : recorders) {
						long finishTime = r.getEstimatedFinishTime();
						long startTime = r.getEstimatedStartTime();
						if (startTime >=0)
							globalStartTime = Math.min(globalStartTime, startTime);

						if (r.hasMediaPacket()) {
							globalHeadTime = Math.min(globalHeadTime, r.getHeadTime());
							globalFinishTime = Math.min(globalFinishTime, finishTime);
							r.setSynchronize(true);
							nToRecord++;
						} else if ((absoluteTargetTime - finishTime) > MAX_WAIT) {
							r.setSynchronize(false);
						}
					}

					if (nToRecord == 0) {
						lastT = t;
						sleep(INC);
						continue;
					}

					relativeTargetTime += t - lastT;
					absoluteTargetTime = globalStartTime + relativeTargetTime;
					absoluteTargetTime = Math.min(absoluteTargetTime, globalHeadTime);
					latency = globalFinishTime - absoluteTargetTime;
					// relativeTargetTime = absoluteTargetTime - globalStartT;

					if (latency > maxDelay) {
						long flushTo = globalFinishTime - 1;
						Log.w(LOG_TAG, "Latency: " + latency + ". Flush to " + flushTo);
						for (Recorder r : recorders)
							r.flushTo(flushTo);
						relativeTargetTime = flushTo - globalStartTime;

						lastT = t;
						sleep(INC);
						continue;
					}

					for (Recorder r : recorders) {
						if (r.isSynchronize())
							r.startRecord(absoluteTargetTime + INTERVAL);
					}

					lastT = t;
					sleep(INC);
				}
			} catch (InterruptedException e) {
				Log.d(LOG_TAG, "Controller stopped");
			}
		}
	}

}
