package com.company;

public class Main extends Thread {
    static SnowFlake sample = new SnowFlake();

    @Override
    public void run() {
        System.out.println(sample.newIdSequence());
        System.out.println(sample.newId());
        System.out.println(sample.newIdSequence());
        System.out.println(sample.newId());
        System.out.println(sample.newIdSequence());
        System.out.println(sample.newId());
        System.out.println(sample.newIdSequence());
        System.out.println(sample.newId());
        System.out.println(sample.newIdSequence());
        System.out.println(sample.newId());
        System.out.println(sample.newIdSequence());
        System.out.println(sample.newId());
        System.out.println(sample.newIdSequence());
        System.out.println(sample.newId());
    }
    public static void main(String[] args) {
        Main t1 = new Main();
        Main t2 = new Main();
        Main t3 = new Main();
        t1.start();
        t2.start();
        t3.start();
    }
}
