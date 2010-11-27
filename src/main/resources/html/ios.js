var parameters;
var mode;
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
var baseScale;
var currentScale=1;
var gestureScale=1;
var serverMouseX=0;
var serverMouseY=0;
var remoteWidth;
var remoteHeight;
var frameWidth;
var frameHeight;
function getParameters(){
	var data={};
	var params=location.search.replace(/^\?/,"").split("&");
	for(var i=0;i<params.length;i++){
		var keyvalue=params[i].split("=");
		if(keyvalue.length!=2){continue;}
		var key=decodeURIComponent(keyvalue[0]);
		var value=decodeURIComponent(keyvalue[1]);
		if(!data.hasOwnProperty(key)){
			data[key]=[];
		}
		data[key].push(value);
	}
	return data;
}
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
		var x=eve.touches[0].pageX-mouseX;
		var y=eve.touches[0].pageY-mouseY;
		var distance=Math.pow(x*x+y*y,1/2);
		if(distance>100){
			x+=(distance-100)*(x>0?1:-1)*2;
			y+=(distance-100)*(y>0?1:-1)*2;
		}
		sendMessage(["mousemoveby",Math.round(x).toString(10),Math.round(y).toString(10)]);
		mouseX=eve.touches[0].pageX;
		mouseY=eve.touches[0].pageY;
		break;
	case "touchend":
		break;
	case "gesturestart":
		break;
	case "gesturechange":
		if(currentScale*eve.scale>=1){
			gestureScale=eve.scale;
		}
		if(mode=="remote"){
			transformRemoteImage();
		}
		break;
	case "gestureend":
		currentScale=currentScale*eve.scale<1?1:currentScale*eve.scale;
		gestureScale=1;
		if(mode=="remote"){
			transformRemoteImage();
		}
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
			(mouseWheel-eve.touches[0].pageY).toString(10)
		]);
		mouseWheel=eve.touches[0].pageY;
		break;
	case "touchend":
		break;
	}
}
function onLoadRemoteImage(){
	var remoteStyle=document.defaultView.getComputedStyle(remote,"");
	remoteWidth=parseInt(remoteStyle.width,10);
	remoteHeight=parseInt(remoteStyle.height,10);
}
function onResizeFrame(){
	var frameStyle=document.defaultView.getComputedStyle(frame,"");
	frameWidth=parseInt(frameStyle.width,10);
	frameHeight=parseInt(frameStyle.height,10);
}
function calculateBaseScale(){
	var widthScale=frameWidth/remoteWidth;
	var heightScale=frameHeight/remoteHeight;
	baseScale=(widthScale<heightScale)?widthScale:heightScale;
}
function transformRemoteImage(){
	var scale=baseScale*currentScale*gestureScale;
	remote.style.webkitTransform="scale("+(scale)+")";
	var baseLeft=(frameWidth-remoteWidth)/2;
	if(frameWidth<remoteWidth*scale){
		baseLeft+=(frameWidth-remoteWidth*scale)*(serverMouseX/remoteWidth-0.5);
	}
	var baseTop=(frameHeight-remoteHeight)/2;
	if(frameHeight<remoteHeight*scale){
		baseTop+=(frameHeight-remoteHeight*scale)*(serverMouseY/remoteHeight-0.5);
	}
	remote.style.marginLeft=baseLeft+"px";
	remote.style.marginTop=baseTop+"px";
}
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
	if(mode=="remote"){
		requestImage();
	}
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
	serverMouseX=parseInt(splitData[0],10);
	serverMouseY=parseInt(splitData[1],10);
	if(mode=="remote"){
		transformRemoteImage();
	}
}
function onUnloadWindow(){
	webSocket.close();
}
function initial(eve){
	parameters=getParameters();
	mode=parameters["mode"]?parameters["mode"][0]:"remote";
	window.scrollTo(0,0);
	browser=document.documentElement;
	frame=document.getElementById("frame");
	remote=document.getElementById("remote");
	onLoadRemoteImage();
	onResizeFrame();
	calculateBaseScale();
	transformRemoteImage();
	remote.style.visibility="visible";
	remote.addEventListener("load",function(){
		onLoadRemoteImage();
		calculateBaseScale();
		transformRemoteImage();
	},false);
	window.addEventListener("orientationchange",function(){
		window.scrollTo(0,0);
		setTimeout(window.scrollTo,3000,0,0);
		onResizeFrame();
		calculateBaseScale();
		transformRemoteImage();
	},false);
	var protocol=(location.protocol=="https:")?"wss":"ws";
	var host=location.host;
	webSocket=new WebSocket(protocol+"://"+host+"/ws/");
	webSocket.addEventListener("open",onOpenWebSocket,false);
	webSocket.addEventListener("close",onCloseWebSocket,false);
	webSocket.addEventListener("message",onMessageWebSocket,false);
	window.addEventListener("unload",onUnloadWindow,false);
	setTimeout(window.scrollTo,3000,0,0);
}
window.addEventListener("load",initial,false);
