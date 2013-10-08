<%@page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import='edu.urjc.pfc.rtsp.server.*'%>
<%@page import='java.util.ArrayList'%>
<%@page import='java.io.File'%>




<!DOCTYPE html>

<!--[if IE 8]>    
	<html class="no-js lt-ie9" lang="en"> 
<![endif]-->
<!--[if gt IE 8]>
	<!-->
<html class="no-js" lang="en">
<!--<![endif]-->


<head>
<jsp:include page="head.html" />
</head>
<body>

	<jsp:include page="header.html" />

	<!-- Body -->
	<section>
		<div class="row">

			<div class="three columns"></div>

			<!-- Orbit -->
			<div class="six columns">
				<p class="tituloCaja">MEDIAS EN EMISION</p>

				<div id="featured">


					<%
        	
		        	//Cargamos los medias que estén en ejecución
		        	
		       		ArrayList<Media> medias = ServerRTSP.INSTANCE.getMedias();
		          	             		
		           	for(int i=0; i<medias.size(); i++ ) {
		           		Media m = medias.get(i);
		           	
		           		out.println("<div class=\"content\">");
		           		out.println("<div class=\"row\">");
		           		out.println("<div class=\"twelve columns\" style=\"text-align:center;\">");
		           		out.println("<div class=\"imagenMedia\">");
		           		out.println("<img src=\"uploads/"+ m.getPathImage() +"\" alt=\"\"  />");
		           		out.println("</div>");
		           		out.println("</div>");
		           		
		           		
		           		out.println("<div class=\"twelve columns\" style=\"text-align:center;\">");
		           		
		           		out.println("<div class=\"infoMedia\">");
		           		out.println("<p class=\"parrafoContenido\"><span class=\"titular\">Titulo:  </span><span class=\"contenido\">"+m.getTitle()+"</span></p>");
		           		out.println(" <p class=\"parrafoContenido\"><span class=\"titular\">URL:  </span><span class=\"contenido\">rtsp://"+ServerRTSP.INSTANCE.getHost()+":"+ServerRTSP.INSTANCE.getPort()+"/"+m.getTitle()+"</span></p>");
		           		out.println("</div>");
		           		out.println("<div class=\"clear\"></div>");
		           		out.println("</div>");
		           		out.println("</div> ");
		           		
		           		
		           		
		           		out.println("<!-- Boton --> ");
		           		out.println("<div class=\"row\">");
		           		out.println("<div class=\"three columns\">");
		           		out.println("</div>");
		           		out.println("<div class=\"six columns\" style=\"margin:33px 0 24px 0; text-align:center;\">");
		           		out.println("<a class=\"button\" href=\"rtsp://"+ServerRTSP.INSTANCE.getHost()+":"+ServerRTSP.INSTANCE.getPort()+"/"+m.getTitle()+"\" style=\"margin:0 10px 0 10px;\">PLAY!</a>");
		           		out.println("</div>");
		           		out.println("<div class=\"three columns\">");
		           		out.println("</div>");
		           		out.println("</div><!-- Boton -->");
		           		out.println("</div><!-- content -->");  		
		           	}	
					%>
				</div>
				<!-- featured -->
			</div>
			<!-- Orbit -->
			
			<div class="three columns"></div>
		</div>
	</section>
	<!-- Body -->

	<div style="clear: both;"></div>




	<jsp:include page="footer.html" />



</body>
</html>
