//package open.richter;

import com.sun.net.httpserver.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

import com.pi4j.io.gpio.*;
import com.pi4j.platform.Platform;
import com.pi4j.platform.PlatformAlreadyAssignedException;
import com.pi4j.platform.PlatformManager;
import com.pi4j.util.CommandArgumentParser;
import com.pi4j.util.Console;

/**
 * How to use pi4j:
 *	install it, read manual from pi4j
 *	to run, you need the right kernel version:
 *		sudo rpi-update 52241088c1da59a359110d39c1875cda56496764
 *	build java file with
 *		sudo javac -classpath .:classes:/opt/pi4j/lib/'*' requestServer.java
 *	run java with
 *		sudo java  -classpath .:classes:/opt/pi4j/lib/'*' requestServer
 *
 **/

//sudo javac -classpath .:classes:/opt/pi4j/lib/'*' requestServer.java & sudo java  -classpath .:classes:/opt/pi4j/lib/'*' requestServer

public class requestServer {

    private static String response = "This is the response at ";
    private static int port = 8080;
    private static int pwmWidth = 100;

    private static GpioPinPwmOutput red = null;
    private static GpioPinPwmOutput green = null;
    private static GpioPinPwmOutput blue = null;

    final static Console console = new Console();

    private static int[] nextColors = new int[3];
    private static int[] currentColors = new int[3];
    private static int[] lastColors = new int[3];


    /**
     * Starts the server. Defines port, context and handler.
     **/
    public static void main(String[] args) throws IOException, InterruptedException {

        console.title("<-- The PiLight Project -->", "Kuhn-Richter Coproduction");

        // allow for user to exit program using CTRL-C
        console.promptForExit();

        
	preparePins();
        wakeUp();
        //gpio.shutdown();

        //configure web service and start it
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        HttpContext colorContext = server.createContext("/color");
        colorContext.setHandler(requestServer::handleRequest);
        server.start();
    }

    private static void preparePins(){
	console.println("-- preparePins --");
	// create gpio controller
        final GpioController gpio = GpioFactory.getInstance();
	Pin pRed = CommandArgumentParser.getPin(RaspiPin.class, RaspiPin.GPIO_00);
	Pin pGreen = CommandArgumentParser.getPin(RaspiPin.class, RaspiPin.GPIO_02);
	Pin pBlue = CommandArgumentParser.getPin(RaspiPin.class, RaspiPin.GPIO_03);
        red = gpio.provisionSoftPwmOutputPin(pRed);
        green = gpio.provisionSoftPwmOutputPin(pGreen);
        blue = gpio.provisionSoftPwmOutputPin(pBlue);

	red.setPwmRange(100);
	blue.setPwmRange(100);
	green.setPwmRange(100);

        //com.pi4j.wiringpi.Gpio.pwmSetMode(com.pi4j.wiringpi.Gpio.PWM_MODE_BAL);
        //com.pi4j.wiringpi.Gpio.pwmSetRange(pwmWidth);
        //com.pi4j.wiringpi.Gpio.pwmSetClock(500); //Was macht der kram?
    }

    private static void wakeUp() throws InterruptedException{
	console.println("-- I am waking up --");
	//für feste pwmwidth = 1000 entworfen.
	for (int i = 0; i <= 100; i++){
	 	red.setPwm((pwmWidth/100)*i);
        	//console.println("PWM rate is: " + red.getPwm());
		Thread.sleep(20); //20 is nice
	}
	
	red.setPwm(0);
	Thread.sleep(200);
	red.setPwm(100);
	Thread.sleep(300);
	red.setPwm(0);
    }

    /**
     * context /color
     **/
    private static void handleRequest(HttpExchange exchange) throws IOException {
	console.println("-- handleRequest --");
        URI requestURI = exchange.getRequestURI();
        response +=  requestURI + "\n";
        //printRequestInfo(exchange);
        changeColor(exchange);
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        response = "\nThis is the response at ";
        os.close();
    }

    private static void printRequestInfo(HttpExchange exchange) {
        System.out.println("-- headers --");
        Headers requestHeaders = exchange.getRequestHeaders();
        requestHeaders.entrySet().forEach(System.out::println);

        System.out.println("-- principle --");
        HttpPrincipal principal = exchange.getPrincipal();
        System.out.println(principal);

        System.out.println("-- HTTP method --");
        String requestMethod = exchange.getRequestMethod();
        System.out.println(requestMethod);

        System.out.println("-- query --");
        URI requestURI = exchange.getRequestURI();
        String query = requestURI.getQuery();
        System.out.println(requestURI);
        System.out.println(query);
    }

    private static void extractColorsFromExchange(HttpExchange exchange){
	console.println("-- extractColorsFromExchange --");
        System.out.println("-- extractColorsFromExchange --");
        String query = exchange.getRequestURI().getQuery();

        String[] seperatedQuery = query.split("&");
        for (String s : seperatedQuery){
	    String[] arg = s.split("=");	    
	    if(arg[0] != null && arg[1] != null && isNum(arg[1])){
	    	if(arg[0].equals("r")){
			nextColors[0] = Integer.parseInt(arg[1]);
	    	} else if(arg[0].equals("g")){ 
			nextColors[1] = Integer.parseInt(arg[1]);
	    	} else if(arg[0].equals("b")){ 
			nextColors[2] = Integer.parseInt(arg[1]);
		}
	    } else {
		console.println("Argument " + s + " is not a color.");
	    }
        }
    }

    private static boolean isNum(String strNum) {
	console.println("-- isNum --");
    	try {
    		Integer.parseInt(strNum);
    	} catch (NumberFormatException e) {
        	return false;
    	}
    	return true;
    }

    private static void changeColor(HttpExchange exchange){
	console.println("-- changeColor --");
        System.out.println("-- changeColor --");
        extractColorsFromExchange(exchange);
        response += "Next Color will be: \n";
        for (int i : nextColors){
            response += i + "   ";
	    //console.println(i);
        }
	try{
		//setColor();
		fadeColor();
	} catch(Exception e) {}
    }

    private static void setColor() throws InterruptedException, IOException{
	console.println("-- setColor --");
	lastColors = currentColors;
	currentColors = nextColors;

	red.setPwm(currentColors[0]);
	//console.println("red");
	//Thread.sleep(1000);

	green.setPwm(currentColors[1]);
	//console.println("green");
	
	//Thread.sleep(1000);
	blue.setPwm(currentColors[2]);
	//console.println("blue");

	colorToConsole();
    }

    private static void colorToConsole(){
		console.println("["+currentColors[0]+","+currentColors[1]+","+currentColors[2]+"]");	
    }
    private static void nextColorToConsole(){
		console.println("["+nextColors[0]+","+nextColors[1]+","+nextColors[2]+"]");	
    }

	//could become new context
    private static void fadeColor() throws InterruptedException, IOException{
	console.println("-- fadeColor --");
	lastColors = currentColors;

	while (!distanceOfArray(nextColors, currentColors)){
		if(currentColors[0] > nextColors[0]){
			red.setPwm(currentColors[0]-1);
			currentColors[0] = currentColors[0]-1;
		} else if(currentColors[0] < nextColors[0]){
			red.setPwm(currentColors[0]+1);
			currentColors[0] = currentColors[0]+1;
		}

		if(currentColors[1] > nextColors[1]){
			blue.setPwm(currentColors[1]-1);
			currentColors[1] = currentColors[1]-1;
		} else if(currentColors[1] < nextColors[1]){
			blue.setPwm(currentColors[1]+1);
			currentColors[1] = currentColors[1]+1;
		}

		if(currentColors[2] > nextColors[2]){
			green.setPwm(currentColors[2]-1);
			currentColors[2] = currentColors[2]-1;
		} else if(currentColors[2] < nextColors[2]){
			green.setPwm(currentColors[2]+1);
			currentColors[2] = currentColors[2]+1;
		}
		colorToConsole();
		nextColorToConsole();
		Thread.sleep(30);
	}
	currentColors = nextColors;
    }

    private static boolean distanceOfArray(int[] a, int[] b){
	if(a.length != b.length){ return false; }
    	for(int i = 0; i <= a.length; i++){
		//if(b[i] == null){ return false; }
		//if(a[i] == null){ return false; }
		if(b[i] != a[i]){ 
			return false; 
		}
	}
	console.println("distance is zero");
	return true;
   }
}
