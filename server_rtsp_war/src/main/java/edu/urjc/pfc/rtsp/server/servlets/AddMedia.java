package edu.urjc.pfc.rtsp.server.servlets;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import edu.urjc.pfc.rtsp.server.Formats;
import edu.urjc.pfc.rtsp.server.Media;
import edu.urjc.pfc.rtsp.server.ServerRTSP;

/**
 * 
 * @author laggc
 *
 */
@WebServlet("/AddMedia")
public class AddMedia extends HttpServlet {
	private static final long serialVersionUID = 1L;


	public AddMedia() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String titulo = null;
		Formats formato= null;
		String path= null;
		String pathImagen= null;
		String IPMC = null;
		int puertoVideo= -1;
		int puertoAudio= -1;


		response.setContentType("text/html");
		try
		{
			String destination = "/uploads";
			ServletContext servletContext = getServletContext();
			String destinationRealPath = servletContext.getRealPath( destination );

			DiskFileItemFactory factory = new DiskFileItemFactory();
			factory.setSizeThreshold( 1024 );

			factory.setRepository( new File( destinationRealPath ) );

			ServletFileUpload uploader = new ServletFileUpload( factory );

			List<FileItem> items = uploader.parseRequest( request );

			Iterator<FileItem> iterator = items.iterator();

			while( iterator.hasNext() )
			{
				FileItem item = (FileItem) iterator.next();

				if(item.isFormField()){

					if(item.getFieldName().equals("inputTitulo")){
						titulo=quitaEspacios(item.getString().trim());
					}

					if(item.getFieldName().equals("inputFormato")){
						if(item.getString().equals("H264_Video_Audio")){
							formato=Formats.H264_Video_Audio;
						}
						if(item.getString().equals("H264_Video")){
							formato=Formats.H264_Video;
						}
						if(item.getString().equals("H264_Encoding_Video_Audio")){
							formato=Formats.H264_Encoding_Video_Audio;
						}
						if(item.getString().equals("H264_Encoding_Video")){
							formato=Formats.H264_Encoding_Video;
						}
						if(item.getString().equals("H264_Encoding_Video_WebCam")){
							formato=Formats.H264_Encoding_Video_WebCam;
						}
					}

					if(item.getFieldName().equals("inputPuertoVideo")){
						puertoVideo=Integer.parseInt(item.getString());
					}

					if(item.getFieldName().equals("inputIPMC")){
						IPMC=item.getString();
					}


					if(item.getFieldName().equals("inputPuertoAudio") && (item.getString() != null) 
							&& (!item.getString().equals(""))){
						puertoAudio=Integer.parseInt(item.getString());
					}

				}
				else {

					if(item.getFieldName().equals("inputPathAux")){
						path=destinationRealPath + "/" + item.getName();

						if( (item.getName() != null) && (!item.getName().equals("")) ) {
							File file = new File( destinationRealPath, item.getName() );
							item.write( file );
						}
					}

					if(item.getFieldName().equals("inputImagenAux")){
						pathImagen=item.getName();

						File file = new File( destinationRealPath, item.getName() );
						item.write( file );
					}


				}
			}

			if( (titulo==null) || (formato==null) || (IPMC==null) || (path==null)) {
				lanzarError(response,"Error con los parametros al añadir un nuevo media.");
				return;
			}


			Media m = new Media(titulo,formato,path,pathImagen,IPMC,puertoVideo,puertoAudio);

			ServerRTSP.INSTANCE.addMedia(m);

			response.sendRedirect("admin");

		}
		catch (Exception e) {
			lanzarError(response,e.getMessage());
			e.printStackTrace();
			return;
		}
	}

	private void lanzarError(HttpServletResponse response, String msgError) throws IOException {
		response.sendRedirect("error?error=" + msgError);
	}

	private String quitaEspacios(String texto) {
		java.util.StringTokenizer tokens = new java.util.StringTokenizer(texto);
		texto = "";
		while(tokens.hasMoreTokens()){
			texto += tokens.nextToken();
		}
		texto = texto.toString();
		texto = texto.trim();
		return texto;
	}
}