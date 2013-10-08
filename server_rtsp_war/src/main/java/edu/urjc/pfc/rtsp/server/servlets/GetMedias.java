package edu.urjc.pfc.rtsp.server.servlets;


import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.urjc.pfc.rtsp.server.ServerRTSP;


@WebServlet("/GetMedias")
public class GetMedias extends HttpServlet {
	private static final long serialVersionUID = 1L;


	public GetMedias() {
		super();
	}


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/xml");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter page = response.getWriter();

		page.print(ServerRTSP.INSTANCE.getXMLmedias());
	}

}