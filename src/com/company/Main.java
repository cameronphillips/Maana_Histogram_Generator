package com.company;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class Main {


    public static void main(String[] args) {

        String root;
        //BlockingQueue implementations are thread-safe
        //A BlockingQueue does not intrinsically support any kind of "close" or "shutdown"
        final BlockingQueue<Message> newQ = new SynchronousQueue<>();
        //the output of all of the individual maps from text analysis threads
        //to be merged by MapMerger
        final BlockingQueue<Message> newOut = new SynchronousQueue<>();

        if(args.length == 0){
            root = "/Users/cameronphillips/Desktop/maana/";
        }else{
            root = args[0];
        }

        PathProducer directoryWalker = new PathProducer(newQ, root);
        PathConsumer textAnalyzer0 = new PathConsumer(newQ, newOut);
        MapMerger mapMerger = new MapMerger(newOut);

        new Thread(directoryWalker).start();
        new Thread(textAnalyzer0).start();
        new Thread(mapMerger).start();
    }
}
