package com.company;

import java.io.IOException;
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
    private final BlockingQueue<Message> newQueue;
    private final BlockingQueue<Message> newOutgoing;

    PathConsumer(BlockingQueue<Message> q, BlockingQueue<Message> out){
        newQueue = q;
        newOutgoing = out;
    }

    @Override
    public void run() {
        try {
            while(true){
                consume(newQueue.take());
            }
        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }

    //receives a path of a text file from the queue, extracts and counts all of the words within it
    void consume(Message message) throws InterruptedException{
        if(!message.isPoison()){
            Map<String, Long> wordCount = new HashMap<>();
            try{
                //Regex s is a whitespace character, or a space, tab, carriage return, new line, vertical tab, or form feed
                wordCount = Files.lines(message.getPath())
                        .flatMap(line -> Arrays.stream(line.trim().split("\\s")))
                        .map(word -> word.replaceAll("[^a-zA-Z]", "").toLowerCase(Locale.ROOT).trim())
                        .filter(word -> word.length() > 0)
                        .map(word -> new SimpleEntry<>(word, 1))
                        .collect(groupingBy(SimpleEntry::getKey, counting()));
            } catch(IOException e){
                System.err.println(e);
            }

            message.setText(wordCount);
            newOutgoing.put(message);
        }else{
            //if the message is poison there is no more processing to be done,
            //send it along to the next consumer (map merger in this case)
            newOutgoing.put(message);
        }
    }
}
