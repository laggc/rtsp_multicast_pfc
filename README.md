rtsp_multicast_pfc
==================

Service RTSP Multicast


This system contains the following projects:

* **app_kmc_player:** client Android using the project rtsp_multicast_client.
* **kas-mscontrol-only-receive:** Kurento MSControl implementation for Android. This version has been modified to only receive.
* **mobicents_rtsp:** asynchronous client-server API for RTSP protocol using Netty. This code has been modified for this project, the original code can be found [here](http://mobicents-media-server.blogspot.com.es/2009/09/mobicents-rtsp-stack-100bta1-released.html) 
* **rtsp_multicast_client:** asynchronous client for multicast RTSP protocol using Netty.
* **rtsp_multicast_server:** Asynchronous server for multicast RTSP protocol using Netty.
* **server_rtsp_war:** web application that implements a multicast RTSP server using the project rtsp_multicast_server
* **simple_player_gui:** client example using the project rtsp_multicast_client.


The requirements are:

* GStreamer 0.10
* Java 6


Available downloads:

* [APK KMC Player](/laggc/rtsp_multicast_pfc/blob/master/Downloads/app_kmc_player-0.0.1-SNAPSHOT.apk)

* [WAR KMC Server](/laggc/rtsp_multicast_pfc/blob/master/Downloads/server_rtsp_war.war)



