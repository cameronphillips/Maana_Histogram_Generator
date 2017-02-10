package com.company;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class Main {

    public static void main(String[] args) {

        String root;
        //BlockingQueue implementations are thread-safe
        //A BlockingQueue does not intrinsically support any kind of "close" or "shutdown"
        final BlockingQueue<Message> producerToConsumer = new SynchronousQueue<>();
        //the output of all of the individual maps from text analysis threads
        //to be merged by MapMerger
        final BlockingQueue<Message> consumerToMerger = new SynchronousQueue<>();

        //hard coded path for debugging and testing, otherwise takes a commandline argument
        if(args.length == 0){
            root = "/Users/cameronphillips/Desktop/maana/";
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
