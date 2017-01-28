package com.company;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;


/**
 * Created by cameronphillips on 1/22/17.
 */
public class PathConsumer implements Runnable{
    private final BlockingQueue<Path> queue;
    private final BlockingQueue<Map<String, Long>> outgoingMaps;

    PathConsumer(BlockingQueue<Path> q, BlockingQueue<Map<String, Long>> out){
        queue = q;
        outgoingMaps = out;
    }

    @Override
    public void run() {
        try {
            while(true){
                consume(queue.take());
            }
        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }

    //receives a path of a text file from the queue, extracts and counts all of the words within it
    void consume(Path path) throws InterruptedException{
//        System.out.println(Thread.currentThread().getName() + " Analyzing: " + path.toString());
        if(path != null){
            Map<String, Long> wordCount = new HashMap<>();
            //TODO MAKE TRY WITH RESOURCES BLOCK!
            try{
                //Regex s is a whitespace character, or a space, tab, carriage return, new line, vertical tab, or form feed
                wordCount = Files.lines(path)
                        .flatMap(line -> Arrays.stream(line.trim().split("\\s")))
                        .map(word -> word.replaceAll("[^a-zA-Z]", "").toLowerCase(Locale.ROOT).trim())
                        .filter(word -> word.length() > 0)
                        .map(word -> new SimpleEntry<>(word, 1))
                        .collect(groupingBy(SimpleEntry::getKey, counting()));
            } catch(IOException e){
                System.err.println(e);
            }

            //wordCount.forEach((k, v) -> System.out.println(String.format("%s ==>> %d", k, v)));
            outgoingMaps.put(wordCount);
        }else{
            outgoingMaps.put(null);
        }
    }
}
