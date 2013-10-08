package edu.urjc.pfc.rtsp.client.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.urjc.pfc.rtsp.client.ClientRTSP;
import edu.urjc.pfc.rtsp.client.Info;


public class GuiClient extends JFrame implements ActionListener, Observer {

	private static Logger logger = LoggerFactory.getLogger(GuiClient.class);

	private static final long serialVersionUID = 1L;

	private JTextField txt_url;
	private JButton bttn_playMedia;


	public GuiClient() {
		super("CLIENT RTSP");

		this.setLocationRelativeTo(null);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		txt_url = new JTextField("rtsp://localhost:5454/hola");
		bttn_playMedia = new JButton("PLAY MEDIA");


		this.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 3;
		constraints.gridheight = 1;
		this.add(txt_url, constraints);

		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		this.add(bttn_playMedia, constraints);



		this.pack();

		bttn_playMedia.addActionListener(this);


		//Añado la GUI como observador del ClientRTSP
		ClientRTSP.getInstance().addObserver(this);

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == bttn_playMedia) {

			ClientRTSP.getInstance().playMedia(txt_url.getText().trim());
		}	
	}

	@Override
	public void update(Observable o, Object arg) {
		logger.info("Ya se puede reproducir!!");

		try {
			Info p = (Info)arg;


			if(p.getSdp() != null) {
				System.out.println(p.getSdp());
			} else
				System.out.println(p.getMsg());

		}catch(Exception e) {

		}
	}

	/**
	 * Intentará buscar un archivo de configuración para log4j.
	 * Si no lo encuentra cargará su configuración básica.
	 */
	private static void Config_log4j() {

		try
		{
			Properties props = new Properties();
			props.load(new FileInputStream("log4j.properties"));
			PropertyConfigurator.configure(props);
		}
		catch(Exception e){}

		BasicConfigurator.configure();	
		logger.info("No se ha encontrado el fichero log4j.properties.");
		logger.info("Cargada configuracion basica de log4j.");
	}



	public static void main(String[] args) {
		
		Config_log4j();

		logger.info("Iniciando GUI");

		new GuiClient();
	}
}
