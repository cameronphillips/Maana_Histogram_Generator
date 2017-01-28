package com.company;

import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class Main {


    public static void main(String[] args) {

        String root;
        //used to keep track of unzipped directories that must be cleaned after analysis
        //BlockingQueue implementations are thread-safe. All queuing methods achieve their
        //effects atomically using internal locks or other forms of concurrency control.
        //A BlockingQueue does not intrinsically support any kind of "close" or "shutdown"
        BlockingQueue<Path> q = new SynchronousQueue<>();
        //the output of all of the individual maps from text analysis threads
        //to be merged by MapMerger
        BlockingQueue<Map<String, Long>> out = new SynchronousQueue<>();

        if(args.length == 0){
            root = "/Users/cameronphillips/Desktop/maana/";
        }else{
            root = args[0];
        }

        PathProducer directoryWalker = new PathProducer(q, root);
        PathConsumer textAnalyzer0 = new PathConsumer(q, out);
        MapMerger mapMerger = new MapMerger(out);

        new Thread(directoryWalker).start();
        new Thread(textAnalyzer0).start();
        new Thread(mapMerger).start();


    }





}
