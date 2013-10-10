package edu.urjc.pfc.rtsp.server.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author laggc
 *
 */
public class Log4jInit extends HttpServlet {

	private static Logger logger = LoggerFactory.getLogger(Log4jInit.class);

	private static final long serialVersionUID = 1L;
 
	public void init() {

		try
		{
			String prefix =  getServletContext().getRealPath("/");
			String file = getInitParameter("log4j-init-file");
			PropertyConfigurator.configure(prefix+file);
		}
		catch(Exception e){
			BasicConfigurator.configure();	
			logger.info("No se ha encontrado el fichero log4j.properties.");
			logger.info("Cargada configuracion basica de log4j.");
		}
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) {}
}