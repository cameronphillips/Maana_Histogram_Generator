package com.company;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by cameronphillips on 1/27/17.
 */
public class MapMerger implements Runnable{

    private Map<String, Long> merged;
    private final BlockingQueue<Map<String, Long>> incomingMaps;
    public MapMerger(BlockingQueue<Map<String, Long>> inq){
        merged = new HashMap<>();
        incomingMaps = inq;
    }

    @Override
    public void run(){
        try {
            while(true){
                consume(incomingMaps.take());
            }
        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }

    //takes maps from queue and merges them with running merged map of all maps
    void consume (Map<String, Long> toBeMerged){
        if(toBeMerged != null){
            merged = Stream.concat(toBeMerged.entrySet().stream(), merged.entrySet().stream())
                    .collect(Collectors.toMap(
                            //the key
                            entry -> entry.getKey(),
                            //the value
                            entry -> entry.getValue(),
                            //the method reference called when duplicate keys are found
                            //takes the sum of the values from both keys
                            Long::sum
                    ));
        }else{
            printHistogram();
        }

    }

     public Map<String, Long> getfinalMergedMap() {
        return merged;
     }

     void printHistogram(){
         merged.forEach((k, v) -> System.out.println(String.format("%s ==>> %d", k, v)));
     }

}
