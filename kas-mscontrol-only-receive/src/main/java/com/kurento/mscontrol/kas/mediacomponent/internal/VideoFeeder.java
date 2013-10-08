package com.kurento.mscontrol.kas.mediacomponent.internal;

import com.kurento.kas.media.rx.VideoFrame;

public interface VideoFeeder {

	public void freeVideoFrameRx(VideoFrame videoFrame);

}
