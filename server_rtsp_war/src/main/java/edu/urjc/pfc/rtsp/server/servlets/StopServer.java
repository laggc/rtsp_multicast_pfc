package edu.urjc.pfc.rtsp.server.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.urjc.pfc.rtsp.server.ServerRTSP;

@WebServlet("/StopServer")
public class StopServer extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
 
    public StopServer() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try {
			ServerRTSP.INSTANCE.destroyInstance();
		}catch(Exception e) {}
		
		try {
			ServerRTSP.INSTANCE.start();
		}catch(Exception e){}
		
		response.sendRedirect("admin");
	}

}
