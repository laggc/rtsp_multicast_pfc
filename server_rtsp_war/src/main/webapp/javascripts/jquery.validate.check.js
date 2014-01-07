$(document).ready(function(){

	$.validator.addMethod("ip_multicast",function(value,element){
		return this.optional(element) || /\b(224)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\b/i.test(value);
	},"No se ha introducido una IP multicast correcta");

	$.validator.addMethod("input_audio",function(value,element){

		if((document.getElementById("inputFormato").value=="H264_Video_Audio") || 
				(document.getElementById("inputFormato").value=="H264_Encoding_Video_Audio")|| 
				(document.getElementById("inputFormato").value=="H263_Video_Audio")){
			return /\b[0-9]+\b/i.test(value);
		}

		if((document.getElementById("inputFormato").value=="H264_Video") || 
				(document.getElementById("inputFormato").value=="H263_Video") || 
				(document.getElementById("inputFormato").value=="H264_Encoding_Video")|| 
				(document.getElementById("inputFormato").value=="H263_Encoding_Video_WebCam")||
				(document.getElementById("inputFormato").value=="H263_Encoding_Video_Screen")){
			return true;
		};


	},"Requerido");

	$.validator.addMethod("input_Path",function(value,element){

		if((document.getElementById("inputFormato").value=="H263_Encoding_Video_WebCam")||
				(document.getElementById("inputFormato").value=="H263_Encoding_Video_Screen")){
			return true;
		}else{
			if( value == null || value ==""){
				return false;
			}else {
				return true;
			}
		};

	},"Introduzca la ruta de un fichero.");

	$("#formulario").validate({
		errorElement:"small",
		rules: {
			inputTitulo: {
				required:true
			},
			inputPath: {
				input_Path:true
			},
			inputImagen: {
				required:true
			},
			inputIPMC:{
				required:true,
				ip_multicast:true

			},
			inputPuertoVideo: {
				required:true,
				digits:true
			},
			inputPuertoAudio: {
				input_audio:true
			}

		},
		messages: {
			inputTitulo: {
				required: "Introduzca un valor para el título",
				minlength: "longitud",
				digits: "numeros",
			},
			inputPath: {
				input_Path:"Introduzca la ruta de un fichero."
			},
			inputImagen: {
				required: "Introduzca la ruta de la imagen."
			},
			inputIPMC:{
				required:"Introduzca una IP",
				ip_multicast:"La IP no multicast."

			},
			inputPuertoVideo: {
				required:"Requerido",
				digits:"Incorrecto"
			},
			inputPuertoAudio: {

			}
		},
		errorPlacement: function(error, element) {
			error.insertAfter(element);	
		},
		highlight: function(element, errorClass) {
			$(element).parent()[0].className = "error";	

			if(element.id=="inputTitulo") {
				document.getElementById("small_aux_titulo").className="ocultar";
			}

			if(element.id=="inputPath") {
				document.getElementById("small_aux_path").className="ocultar";
			}

			if(element.id=="inputImagen") {
				document.getElementById("small_aux_imagen").className="ocultar";
			}

			if(element.id=="inputIPMC") {
				document.getElementById("small_aux_ip").className="ocultar";
			}

		},
		unhighlight: function(element, errorClass) {
			$(element).parent()[0].className = "";

			if(element.id=="inputTitulo") {
				document.getElementById("small_aux_titulo").className="small-normal";
			}

			if(element.id=="inputPath") {
				document.getElementById("small_aux_path").className="small-normal";
			}

			if(element.id=="inputImagen") {
				document.getElementById("small_aux_imagen").className="small-normal";
			}

			if(element.id=="inputIPMC") {
				document.getElementById("small_aux_ip").className="small-normal";
			}
		}			
	});
});