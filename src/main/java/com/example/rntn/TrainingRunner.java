package com.example.rntn;

import java.util.ArrayList;
import java.util.List;

/**
 * TrainingRunner: thin wrapper to call Stanford's SentimentTraining main.
 *
 * Note: The class edu.stanford.nlp.sentiment.SentimentTraining must be available on the classpath
 * (provided by the stanford-corenlp jar). Training may require large memory: run with -Xmx6g or more.
 */
public class TrainingRunner {

    public static void main(String[] args) throws Exception {
        // Example usage:
        // java -cp "*" com.example.rntn.TrainingRunner /path/to/train.sst /path/to/dev.sst /path/to/out-model.ser.gz
        if (args.length < 3) {
            System.err.println("Usage: TrainingRunner <train.sst> <dev.sst> <out-model>");
            System.exit(2);
        }
        String train = args[0];
        String dev = args[1];
        String out = args[2];

        List<String> argList = new ArrayList<>();
        argList.add("-trainPath");
        argList.add(train);
        argList.add("-devPath");
        argList.add(dev);
        argList.add("-model");
        argList.add(out);
        argList.add("-train");
        argList.add("-numHid");
        argList.add("25");
        argList.add("-numClasses");
        argList.add("5");

        String[] runArgs = argList.toArray(new String[0]);

        // Delegate to Stanford's SentimentTraining main entry point
        edu.stanford.nlp.sentiment.SentimentTraining.main(runArgs);
    }
}
