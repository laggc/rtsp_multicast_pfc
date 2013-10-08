package edu.urjc.pfc.rtsp.client;


/**
 * Una instancia de esta clase será devuelva cuando el protocolo RTSP haya terminado,
 * indiciando si todo ha ido correctamente, y con el SDP en caso de que así sea.
 * En caso contrario contendrá el mensaje de error.
 * @author laggc
 *
 */
public class Info {
	
	private boolean play;
	private String msg;
	private String sdp;
	
	public boolean isPlay() {
		return play;
	}


	public void setPlay(boolean play) {
		this.play = play;
	}


	public String getMsg() {
		return msg;
	}


	public void setMsg(String msg) {
		this.msg = msg;
	}


	public String getSdp() {
		return sdp;
	}


	public void setSdp(String sdp) {
		this.sdp = sdp;
	}
	
	
	public Info(boolean _play, String _msg, String _sdp) {
		setPlay(_play);
		setMsg(_msg);
		setSdp(_sdp);
		
	}
}
