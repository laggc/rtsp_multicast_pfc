package edu.urjc.pfc.rtsp.server;

/**
 * Tipos de medios que emitirá el servidor
 * @author laggc
 *
 */
public enum Formats {
	
	H264_Video_Audio,			//Emitirá video+audio de un archivo H264
	H264_Video,					//Emitirá video de un archivo H264
	H264_Encoding_Video_Audio,	//Emitirá video+audio de un archivo de video y lo codificará en H264
	H264_Encoding_Video,		//Emitirá video de un archivo de video y lo codificará en H264
	H263_Video_Audio,			//Emitirá video+audio de un archivo H264
	H263_Video,					//Emitirá video de un archivo H264
	H263_Encoding_Video_WebCam,	//Emitirá el video de la cam, codificandolo en H263
	H263_Encoding_Video_Screen	//Emitirá el video de la pantalla
}
