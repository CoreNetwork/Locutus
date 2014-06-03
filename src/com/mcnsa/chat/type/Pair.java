package com.mcnsa.chat.type;

public class Pair {
	private Object left;
	private Object right;
	
	public Pair(Object l, Object r){
		left = l;
		right = r;
	}
	
	public Object getLeft(){
		return left;
	}
	
	public Object getRight(){
		return right;
	}
	
	public void setLeft(Object l){
		left = l;
	}
	
	public void setRight(Object r){
		right = r;
	}
}
