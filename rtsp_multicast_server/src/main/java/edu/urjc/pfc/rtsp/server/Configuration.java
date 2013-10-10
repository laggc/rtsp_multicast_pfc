package edu.urjc.pfc.rtsp.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author laggc
 *
 */
public enum Configuration {
	
	INSTANCE;

	private  Logger logger = LoggerFactory.getLogger(Configuration.class);

	private String nameFile = "configuration.properties";

	

	private String nombreInterfazRed = "eth1";
	private int puerto_servidor = 5454;


	public String getNombreInterfazRed() {
		return nombreInterfazRed;
	}

	public int getPuertoServidor() {
		return puerto_servidor;
	}

	private void setPuertoServidor(String _puertoServidor) {
		
		try {
			if(_puertoServidor != null) {
				puerto_servidor = Integer.parseInt(_puertoServidor.trim());
			}
		}catch(Exception e) {}
		
	}

	private void setNombreInterfazRed(String _nombreInterfazRed) {
		if(_nombreInterfazRed != null) {
			nombreInterfazRed = _nombreInterfazRed;
		}
	}


	private Configuration() {
		try{
			logger.info("Obtenido valores del fichero:\t" + nameFile);
			
			InputStream file = new FileInputStream(new File(nameFile)) ;
			Properties props = new Properties();
			props.load(file);

			setNombreInterfazRed(props.getProperty("NOMBRE_INTERFAZ_RED"));
			setPuertoServidor(props.getProperty("PUERTO_SERVIDOR"));

		} 
		catch(Exception e){
			logger.error("Error leyendo el fichero: " + nameFile);
			logger.info("Valores por defecto:");
			logger.info("\tNOMBRE_INTERFAZ_RED:\t" + nombreInterfazRed);
			logger.info("\tPUERTO_SERVIDOR:\t" + puerto_servidor);
		}	 
	}
}
