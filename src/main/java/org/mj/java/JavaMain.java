package org.mj.java;

import org.mj.scala.ScalaMain;

public class JavaMain {

	public static void call() {
		System.out.println("This is a call to JavaMain");
	}

	public static void main(String[] args) {
        System.out.println("Inside JavaMain Start");
        ScalaMain.call();
        System.out.println("Inside JavaMain Start");
    }
}