package com.main;

public class MainRunner {
	
	public static void main(String[] args) { 
		
		Thread thread=new PointerDataReceiver(9048);  
		thread.start();   
		  
	}  

} 
   