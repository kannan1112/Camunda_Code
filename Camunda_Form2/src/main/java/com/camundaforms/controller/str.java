package com.camundaforms.controller;

public class str {
public static void main(String[] args) {
	
	String sre= "1234";
	String str="{\"query\":{\"match\":{\"_id\":\"" + sre + "\"}}}";
	System.err.println(str);
}
}
