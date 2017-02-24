package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.*;
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
    private final CharsetDecoder decoder;

    PathConsumer(BlockingQueue<Message> q, BlockingQueue<Message> out){
        newQueue = q;
        newOutgoing = out;
        //specify decoder for line reader
        //due to difficulty of parsing multi-encoded files
        decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
    }


    @Override
    public void run() {
        try {
            while(true){
                consume(newQueue.take());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //receives a path of a text file from the queue, extracts and counts all of the words within it
    void consume(Message message) throws InterruptedException{
        if(!message.isPoison()){
            Map<String, Long> wordCount = new HashMap<>();
            //need to use a buffered reader rather than Files.lines() to override charset decoder
            try(Reader reader = Channels.newReader(FileChannel.open(message.getPath()), decoder, -1);
                BufferedReader bufferedReader = new BufferedReader(reader)) {
                //regex s matches a space, a tab, a line break, or a form feed
                wordCount = bufferedReader.lines().flatMap(line -> Arrays.stream(line.trim().split("\\s")))
                        .map(word -> word.replaceAll("[^a-zA-Z]", "").toLowerCase(Locale.ROOT).trim())
                        .filter(word -> word.length() > 0)
                        .map(word -> new SimpleEntry<>(word, 1))
                        .collect(groupingBy(SimpleEntry::getKey, counting()));

            } catch(MalformedInputException e){
                //if this exception is thrown then *potentially* retry with different encoding
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
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
