;(function ($, window, undefined) {
	'use strict';

	var $doc = $(document),
	Modernizr = window.Modernizr;

	$(document).ready(function() {
		$.fn.foundationAlerts           ? $doc.foundationAlerts() : null;
		$.fn.foundationButtons          ? $doc.foundationButtons() : null;
		$.fn.foundationAccordion        ? $doc.foundationAccordion() : null;
		$.fn.foundationNavigation       ? $doc.foundationNavigation() : null;
		$.fn.foundationTopBar           ? $doc.foundationTopBar() : null;
		$.fn.foundationCustomForms      ? $doc.foundationCustomForms() : null;
		$.fn.foundationMediaQueryViewer ? $doc.foundationMediaQueryViewer() : null;
		$.fn.foundationTabs             ? $doc.foundationTabs({callback : $.foundation.customForms.appendCustomMarkup}) : null;
		$.fn.foundationTooltips         ? $doc.foundationTooltips() : null;
		$.fn.foundationMagellan         ? $doc.foundationMagellan() : null;
		$.fn.foundationClearing         ? $doc.foundationClearing() : null;

		$.fn.placeholder                ? $('input, textarea').placeholder() : null;
	});

	// UNCOMMENT THE LINE YOU WANT BELOW IF YOU WANT IE8 SUPPORT AND ARE USING .block-grids
	// $('.block-grid.two-up>li:nth-child(2n+1)').css({clear: 'both'});
	// $('.block-grid.three-up>li:nth-child(3n+1)').css({clear: 'both'});
	// $('.block-grid.four-up>li:nth-child(4n+1)').css({clear: 'both'});
	// $('.block-grid.five-up>li:nth-child(5n+1)').css({clear: 'both'});

	// Hide address bar on mobile devices (except if #hash present, so we don't mess up deep linking).
	if (Modernizr.touch && !window.location.hash) {
		$(window).load(function () {
			setTimeout(function () {
				window.scrollTo(0, 1);
			}, 0);
		});
	}

})(jQuery, this);


/*Mis funciones */

/*Funciones para los input con estilo */

function getFilePath(){
	document.getElementById("inputPathAux").click();
}

function subPath(obj){
	var file = obj.value;
	var fileName = file.split("\\");
	document.formulario.inputPath.value = fileName[fileName.length-1];
}

function getFileImagen(){
	document.getElementById("inputImagenAux").click();
}

function subImagen(obj){
	var file = obj.value;
	var fileName = file.split("\\");
	document.formulario.inputImagen.value = fileName[fileName.length-1];
}

/*Funcion para ocultar el input del puerto de Audio */

function changeFormato(){

	if((document.getElementById("inputFormato").value=="H264_Video_Audio") || 
			(document.getElementById("inputFormato").value=="H264_Encoding_Video_Audio")){

		document.getElementById("divPuertoAudio").className="three columns";
		document.getElementById("divPuertoVideo").className="three columns";

	}
	if((document.getElementById("inputFormato").value=="H264_Video") || 
			(document.getElementById("inputFormato").value=="H264_Encoding_Video")|| 
			(document.getElementById("inputFormato").value=="H264_Encoding_Video_WebCam")){

		document.getElementById("divPuertoAudio").className="ocultar";
		document.getElementById("divPuertoVideo").className="six columns";

	}

	if((document.getElementById("inputFormato").value=="H264_Encoding_Video_WebCam")){
		document.getElementById("divPath").className="ocultar_simple";
	}else {
		document.getElementById("divPath").className="";
	}


}


