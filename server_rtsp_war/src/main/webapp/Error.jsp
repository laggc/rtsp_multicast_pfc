<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

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
    <section id="cuerpoError">
        <div class="row">
            
            <div class="twelve columns">
                <p class="tituloCaja">HA OCURRIDO UN ERROR:</p>
                
                <p id="parrafoError">
                <% 
                	String error = request.getParameter("error");

                	if(error == null || error.equals("")) {
                		error="Error desconocido.";
                	}
                	out.println(error);
                %>
                </p>  
            </div> 
        </div> 
    </section><!-- Body -->
    
    <div style="clear:both;"></div>
    
    <section>
        <div class="row">
            <div class="six columns"></div>
            <div class="three columns">
                <a class="button" href="admin" style="float:right;">Administracion</a>
            </div>
            <div class="three columns">
                <a class="alert button" href="StopServer" style="float:left;">Parar Servidor</a>
            </div>
        </div>
    </section>
    
    <jsp:include page="footer.html" />
    
   
 
</body>
</html>
