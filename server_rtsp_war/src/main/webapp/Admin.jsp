<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import='edu.urjc.pfc.rtsp.server.*' %>
<%@page import='java.util.ArrayList' %>
<%@page import='java.io.File' %>




<!DOCTYPE html>

<!--[if IE 8]>    
	<html class="no-js lt-ie9" lang="en"> 
<![endif]-->
<!--[if gt IE 8]>
	<!--> <html class="no-js" lang="en"> 
<!--<![endif]-->


<head>
     <jsp:include page="head.html" />
</head>
<body>

	<jsp:include page="header.html" />

	<!-- Body -->
    <section>
        <div class="row">
            <!-- Formulario -->
            <div class="six columns">
                <p class="tituloCaja">EMITIR NUEVO MEDIA</p>
                
                <form name="formulario" id="formulario" action="AddMedia" method="post" enctype="multipart/form-data">
                    <fieldset id="recuadroIzq">
                    <div class="row">
                        <div class="twelve columns">
                        
                            <!-- Titulo -->
                            <div>
                                <label for="inputTitulo">Titulo</label>
                                <input name="inputTitulo" id="inputTitulo" type="text">
                                <small  id="small_aux_titulo" class="small-normal">&nbsp;</small>
                            </div><!-- Titulo -->
                            
                            <!-- Formato -->
                            <div>
                                <label for="inputFormato">Formato</label>
                                <select name="inputFormato" id="inputFormato"  onchange="changeFormato()" style="margin-bottom:15px;">
                                    <%
										for(Formats formato: Formats.values()){
											out.println("<option value=\""+formato.toString()+"\">"+formato.toString()+"</option>");
										}
									%>
                                </select>
                            </div><!-- Formato -->
                            
                        </div>
                    </div>
                    
                    <!-- Path -->
                    <div id=divPath>
	                    <div class="row">
	                        <div class="twelve columns">
	                            <label for="inputPath">Path</label>
	                        </div>
	                    </div>
	                    
	                    <div class="row collapse">
	                        <div class="eight columns">
	                            <!-- Este DIV es para poner/quitar la clase error y perder la clase superior -->
	                            <div>
	                                <input name="inputPath" id="inputPath" type="text" readonly/>
	                                <small  id="small_aux_path" class="small-normal">&nbsp;</small>
	                            </div>
	                        </div>
	                        <div class="four columns">
	                            <a class="button expand postfix"  id="botonPath" onclick="getFilePath()">Examinar</a>
	                        </div>
	                    </div> 
                    </div><!-- Path -->
                    
                    
            
                    <!-- Imagen -->
                    <div class="row">
                        <div class="twelve columns">
                            <label for="inputImagen" id="labelInputImagen">Imagen</label>
                        </div>
                    </div>
                    
                    <div class="row collapse">
                        <div class="eight columns">
                            <!-- Este DIV es para poner/quitar la clase error y perder la clase superior -->
                            <div>
                                <input 	name="inputImagen" id="inputImagen" type="text" readonly/>
                                <small  id="small_aux_imagen" class="small-normal">&nbsp;</small>
                            </div>
                        </div>
                        <div class="four columns">
                            <a class="button expand postfix" id="botonImagen" onclick="getFileImagen()">Examinar</a>
                        </div>
                    </div><!-- Imagen -->
                                
    
                    
                    <div class="row">
                        <div class="six columns">
                            
                            <!-- IP Multicast -->
                            <div>
                                <label for="inputIPMC" id="labelIPMC">IP Multicast</label>
                                <input name="inputIPMC" id="inputIPMC" type="text" placeholder="224.0.0.1">
                                <small id="small_aux_ip" class="small-normal">&nbsp;</small>
                            </div><!-- IP Multicast -->
                        </div>
                    
                        <div class="three columns" id="divPuertoVideo">
                            <!-- Puerto Video -->
                            <div>
                                <label for="inputPuertoVideo">P. Video</label>
                                <input name="inputPuertoVideo" id="inputPuertoVideo" type="text" placeholder="5000">
                            </div><!-- Puerto Video -->
                        </div>
                        
                        
                        <div class="three columns" id="divPuertoAudio">
                            <!-- Puerto Audio -->
                            <div>
                                <label for="inputPuertoAudio">P. Audio</label>
                                <input name="inputPuertoAudio" id="inputPuertoAudio" type="text" placeholder="5002">
                            </div><!-- Puerto Audio -->
                        </div>
                    </div>
                    
                    <div class="row">
                        <div class="four columns">  
                        </div>
                        
                        <div class="four columns"> 
                            <p>
                                <input class="success button" type="submit" value="Iniciar Streaming" />
                            </p>
                        </div>
                        
                        <div class="four columns">  
                        </div>
                    </div>
                    
                    
                    <!-- Div que contiene el input type=file oculto para el Path -->
                    <div class="ocultar">
                        <input name="inputPathAux" id="inputPathAux" type="file" accept=".3gp,video/*" onchange="subPath(this)"/>
                    </div>
                    
                    <!-- Div que contiene el input type=file para la imagen -->
                    <div class="ocultar">																			
                        <input name="inputImagenAux" id="inputImagenAux" type="file" accept="image/*" onchange="subImagen(this)"/>
                    </div>
                    
                    
                    </fieldset>
                </form>
            </div> <!-- Formulario -->
        
            <!-- Orbit -->
            <div class="six columns">
                <p class="tituloCaja">MEDIAS EN EMISION</p>
                
                <div id="featured" >
                
                
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
		           		out.println("<a class=\"alert button\" href=\"DeleteMedia?titulo="+ m.getTitle() +"\" style=\"margin:0 10px 0 10px;\">Parar Streaming</a>");
		           		out.println("</div>");
		           		out.println("<div class=\"three columns\">");
		           		out.println("</div>");
		           		out.println("</div><!-- Boton -->");
		           		out.println("</div><!-- content -->");  		
		           	}	
					%>
                </div><!-- featured -->
            </div> <!-- Orbit -->
        </div> 
    </section><!-- Body -->
    
    <div style="clear:both;"></div>
    
    <section class="ocultar">
        <div class="row">
            <div class="nine columns"></div>
            <div class="three columns">
                <a class="alert button" href="StopServer" style="float:right;">Parar Servidor</a>
            </div>
        </div>
    </section>
  
    
    <jsp:include page="footer.html" />
    
   
 
</body>
</html>
