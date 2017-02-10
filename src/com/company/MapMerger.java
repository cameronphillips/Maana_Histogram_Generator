package com.company;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by cameronphillips on 1/27/17.
 */
public class MapMerger implements Runnable{

    private Map<String, Long> histogram;
    private final BlockingQueue<Message> incomingMaps;
    public MapMerger(BlockingQueue<Message> inq){
        histogram = new HashMap<>();
        incomingMaps = inq;
    }


    @Override
    public void run(){
        try {
            while(true){
                consume(incomingMaps.take());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //takes maps from queue and merges them with running histogram map of all maps
    private void consume (Message toBeMerged){
        if(!toBeMerged.isPoison()){


            histogram = Stream.concat(toBeMerged.getText().entrySet().stream(), histogram.entrySet().stream())
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
            shutdown();
        }

    }

     public Map<String, Long> getfinalMergedMap() {
        return histogram;
     }

     void shutdown(){
        //0 indicates normal shutdown
        System.exit(0);
     }

     void printHistogram(){
         //find the longest word in the histogram to properly pad output
         int longestWord = histogram.entrySet().stream()
                 .map(entry -> entry.getKey().length())
                 .reduce(0, (a, b) -> Integer.max(a, b));

         histogram.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach((entry) -> System.out.println(String.format("%"+longestWord+"s ==>> %d", entry.getKey(), entry.getValue())));
     }

}
