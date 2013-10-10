package edu.urjc.pfc.rtsp.server.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.urjc.pfc.rtsp.server.ServerRTSP;

/**
 * 
 * @author laggc
 *
 */
@WebServlet("/DeleteMedia")
public class DeleteMedia extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
   
    public DeleteMedia() {
        super();
    }

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String titulo = request.getParameter("titulo");
		
	
		
		if(titulo!=null) {
			try {
				ServerRTSP.INSTANCE.deleteMedia(titulo);	
			} catch(Exception e){}
		}
		
		
		response.sendRedirect("admin");
	}

}
