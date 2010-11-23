var imageRequestTimer=null;
var browser;
var frame;
var remote;
var webSocket;
var receiveIndexes=[];
var receiveData={};
var mouseX=0;
var mouseY=0;
var mouseWheel=0;
var startScale=1;
function sendMessage(data){
	webSocket.send(data.join(","));
}
function requestImage(){
	sendMessage(["image"]);
}
function setImageRequestTimer(milliseconds){
	if(imageRequestTimer!=null){
		clearTimeout(imageRequestTimer);
	}
	imageRequestTimer=setTimeout(requestImage,milliseconds);
}
function handler(eve){
	eve.preventDefault();
}
function frameHandler(eve){
	switch(eve.type){
	case "touchstart":
		if(eve.touches.length!=1){return;}
		mouseX=eve.touches[0].pageX;
		mouseY=eve.touches[0].pageY;
		break;
	case "touchmove":
		if(eve.touches.length!=1){return;}
		sendMessage([
			"mousemoveby",
			(eve.touches[0].pageX-mouseX).toString(10),
			(eve.touches[0].pageY-mouseY).toString(10)
		]);
		mouseX=eve.touches[0].pageX;
		mouseY=eve.touches[0].pageY;
		break;
	case "touchend":
		break;
	case "gesturestart":
		break;
	case "gesturechange":
		remote.style.webkitTransform="scale("+startScale*eve.scale+")";
		break;
	case "gestureend":
		startScale*=eve.scale;
		break;
	}
}
function mouseMainHandler(eve){
	mouseButtonHandler(eve,"main");
}
function mouseContextmenuHandler(eve){
	mouseButtonHandler(eve,"contextmenu");
}
function mouseButtonHandler(eve,button){
	switch(eve.type){
	case "touchstart":
		if(eve.touches.length!=1){return;}
		sendMessage(["mousedown",button]);
		break;
	case "touchmove":
		break;
	case "touchend":
		if(eve.touches.length!=0){return;}
		sendMessage(["mouseup",button]);
		break;
	}
}
function mouseWheelHandler(eve){
	switch(eve.type){
	case "touchstart":
		if(eve.touches.length!=1){return;}
		mouseWheel=eve.touches[0].pageY;
		break;
	case "touchmove":
		if(eve.touches.length!=1){return;}
		sendMessage([
			"mousewheel",
			(eve.touches[0].pageY-mouseWheel).toString(10)
		]);
		mouseWheel=eve.touches[0].pageY;
		break;
	case "touchend":
		break;
	}
}
/*
function onMouseMoveBrowser(eve){
	var x=eve.clientX/browser.clientWidth*(remote.offsetWidth-browser.clientWidth);
	var y=eve.clientY/browser.clientHeight*(remote.offsetHeight-browser.clientHeight);
	scrollTo(x,y);
	eve.stopPropagation();
	eve.preventDefault();
	sendMessage(["mousemoveto",eve.pageX,eve.pageY]);
}
function onContextMenuBrowser(eve){
	eve.stopPropagation();
	eve.preventDefault();
}
function onMouseDownImage(eve){
	var button="";
	switch(eve.button){
	case 0:button="main";break;
	case 1:button="wheel";break;
	case 2:button="contextmenu";break;
	}
	eve.stopPropagation();
	eve.preventDefault();
	sendMessage(["mousedown",button]);
}
function onMouseUpImage(eve){
	var button="";
	switch(eve.button){
	case 0:button="main";break;
	case 1:button="wheel";break;
	case 2:button="contextmenu";break;
	}
	eve.stopPropagation();
	eve.preventDefault();
	sendMessage(["mouseup",button]);
}
function onMouseWheelImage(eve){
	eve.stopPropagation();
	eve.preventDefault();
	sendMessage(["mousewheel",eve.wheelDelta]);
}
*/
function onOpenWebSocket(){
	["touchstart","touchmove","touchend","gesturestart","gesturechange","gestureend"].forEach(
			function(eventName){
				document.addEventListener(eventName,handler,false);
				document.getElementById("frame").addEventListener(eventName,frameHandler,false);
				document.getElementById("mouse_main").addEventListener(eventName,mouseMainHandler,false);
				document.getElementById("mouse_contextmenu").addEventListener(eventName,mouseContextmenuHandler,false);
				document.getElementById("mouse_wheel").addEventListener(eventName,mouseWheelHandler,false);
			}
		);
	requestImage();
}
function onCloseWebSocket(){
}
function onMessageWebSocket(message){
	var data=message.data;
	var position=data.indexOf("_");
	var messageType;
	var messageData;
	if(position==-1){
		messageType=data;
		messageData="";
	}else{
		messageType=data.slice(0,position);
		messageData=data.slice(position+1);
	}
	switch(messageType){
	case "image":
		onMessageWebSocketImage(messageData);
		break;
	case "mousepoint":
		onMessageWebSocketMousePoint(messageData);
		break;
	}
}
function onMessageWebSocketImage(data){
	setImageRequestTimer(3000);
	var splitData=data.split("_");
	if(splitData.length!=4){
		return;
	}
	var capturedDate=splitData[0];
	var sequenceNumber=parseInt(splitData[1],16);
	var sequenceCount=parseInt(splitData[2],16);
	if(receiveIndexes.indexOf(capturedDate)==-1){
		receiveIndexes.push(capturedDate);
		receiveIndexes.sort(function(a,b){return parseInt(a,16)-parseInt(b,16);});
		receiveData[capturedDate]={"data":[],"receiveCount":0,"sequenceCount":sequenceCount};
	}
	var receive=receiveData[capturedDate];
	receive.receiveCount++;
	receive.data[sequenceNumber]=splitData[3];
	if(receive.receiveCount!=receive.sequenceCount){
		return;
	}
	var currentPosition=receiveIndexes.indexOf(capturedDate);
	var deleteIndexes=receiveIndexes.slice(0,currentPosition+1);
	receiveIndexes=receiveIndexes.slice(currentPosition+2);
	for(var i=0;i<deleteIndexes.length;i++){
		delete receiveData[deleteIndexes[i]];
	}
	var base64="";
	for(var i=1;i<receive.data.length;i++){
		base64+=receive.data[i];
	}
	remote.setAttribute("src","data:image/jpg;base64,"+base64);
	setImageRequestTimer(100);
}
function onMessageWebSocketMousePoint(data){
	var splitData=data.split("_");
	if(splitData.length!=2){
		return;
	}
	var x=parseInt(splitData[0],10);
	var y=parseInt(splitData[1],10);
}
function onUnloadWindow(){
	webSocket.close();
}
function initial(eve){
	browser=document.documentElement;
	frame=document.getElementById("frame");
	remote=document.getElementById("remote");
	var protocol=(location.protocol=="https:")?"wss":"ws";
	var host=location.host;
	webSocket=new WebSocket(protocol+"://"+host+"/ws/");
	webSocket.addEventListener("open",onOpenWebSocket,false);
	webSocket.addEventListener("close",onCloseWebSocket,false);
	webSocket.addEventListener("message",onMessageWebSocket,false);
	window.addEventListener("unload",onUnloadWindow,false);
}
window.addEventListener("load",initial,false);
