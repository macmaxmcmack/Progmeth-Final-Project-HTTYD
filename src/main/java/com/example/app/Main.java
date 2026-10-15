package com.example.app;

public class Main {

    public static String greet(String name) {
        return "Hello, " + name + "!";
    }

    public static void main(String[] args) {
        System.out.println(greet("team"));
        System.out.println("Running on Java " + Runtime.version().feature());
    }
}
