var imageRequestTimer=null;
var browser;
var remote;
var webSocket;
var receiveIndexes=[];
var receiveData={};
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
/*
function onKeyDownImage(eve){
	console.log(eve.keyCode);
//	sendMessage(["keydown"]);
}
function onKeyUpImage(eve){
	console.log(eve.keyCode);
//	sendMessage(["keyup"]);
}
function onKeyPressImage(eve){
	console.log(eve.charCode);
//	sendMessage(["keypress"]);
}
*/
function onOpenWebSocket(){
	browser.addEventListener("mousemove",onMouseMoveBrowser,false);
	remote.addEventListener("mousedown",onMouseDownImage,false);
	remote.addEventListener("mouseup",onMouseUpImage,false);
	remote.addEventListener("mousewheel",onMouseWheelImage,false);
//	window.addEventListener("keydown",onKeyDownImage,false);
//	window.addEventListener("keyup",onKeyUpImage,false);
//	window.addEventListener("keypress",onKeyPressImage,false);
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
function onUnloadWindow(){
	webSocket.close();
}
function initial(eve){
	browser=document.documentElement;
	remote=document.getElementById("remote");
	var protocol=(location.protocol=="https:")?"wss":"ws";
	var host=location.host;
	webSocket=new WebSocket(protocol+"://"+host+"/ws/");
	webSocket.addEventListener("open",onOpenWebSocket,false);
	webSocket.addEventListener("close",onCloseWebSocket,false);
	webSocket.addEventListener("message",onMessageWebSocket,false);
	window.addEventListener("unload",onUnloadWindow,false);
	browser.addEventListener("contextmenu",onContextMenuBrowser,false);
	browser.addEventListener("contextmenu",onContextMenuBrowser,true);
}
window.addEventListener("load",initial,false);
