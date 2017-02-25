package com.company;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.*;

public class Main {

    public static void main(String[] args) {

        String root;
        //BlockingQueue implementations are thread-safe
        //A BlockingQueue does not intrinsically support any kind of "close" or "shutdown"
        final BlockingQueue<Message> producerToConsumer = new SynchronousQueue<>();
        //the output of all of the individual maps from text analysis threads
        //to be merged by MapMerger
        final BlockingQueue<Message> consumerToMerger = new SynchronousQueue<>();

        //can take commandline argument, or get path from user input
        if(args.length == 0){
            System.out.println("Enter a directory path, and have all text files analyzed and output into a histogram");
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter a directory path: ");
            String consoleInput = scanner.nextLine();

            //sanitize input to pathproducer
            while(!Files.exists(Paths.get(consoleInput)) || !Files.isDirectory(Paths.get(consoleInput)) || consoleInput.length() == 0){
                System.out.println("Invalid directory, or does not exist on this file system.");
                System.out.print("Enter a directory path: ");
                consoleInput = scanner.nextLine();
            }
                root = consoleInput;
            //root = "/Users/cameronphillips/Desktop/maana/";
        }else{
            root = args[0];
        }

        PathProducer directoryWalker = new PathProducer(producerToConsumer, root);
        PathConsumer textAnalyzer = new PathConsumer(producerToConsumer, consumerToMerger);
        MapMerger mapMerger = new MapMerger(consumerToMerger);


        new Thread(directoryWalker).start();
        new Thread(textAnalyzer).start();
        new Thread(mapMerger).start();
    }
}
