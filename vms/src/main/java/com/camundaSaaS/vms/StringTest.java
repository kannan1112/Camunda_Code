package com.camundaSaaS.vms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StringTest {
	
	

	public static void main(String[] args) {
		
		String[] array = {"Apple","Banana","Apple","Peanut","Banana","Orange","Apple","Peanut"};
		
	    Map<String, Integer> result = new HashMap();
	    
	    for(String s : array){

	        if(result.containsKey(s)){
	            
	            result.put(s, result.get(s)+1);
	        }else{
	            
	            result.put(s, 1);
	        }
	    }
	    System.out.println(result);
	
}
}
