package com.example.rntn.util;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.Properties;

/**
 * SentimentPredictor: loads a trained sentiment model and runs inference on sentences.
 * The property key to supply a custom sentiment model may depend on CoreNLP version.
 * This example shows setting the sentiment.model property to point to the serialized model.
 */
public class SentimentPredictor {

    private final StanfordCoreNLP pipeline;

    // Custom labels mapping (index 0..4)
    private static final String[] CUSTOM_LABELS = new String[]{
            "ANXIETY",
            "SUICIDAL",
            "ANGER",
            "SADNESS",
            "FRUSTRATION"
    };

    /**
     * modelPath: path to serialized model (.ser.gz) produced by SentimentTraining
     */
    public SentimentPredictor(String modelPath) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,parse,sentiment");
        // set custom sentiment model location (check CoreNLP docs for version-specific keys)
        props.setProperty("sentiment.model", modelPath);
        this.pipeline = new StanfordCoreNLP(props);
    }

    /**
     * Returns predicted class id (as integer) for the first sentence in the input.
     * This implementation maps textual labels (e.g., "Verynegative") or numeric labels
     * to an integer index consistent with CUSTOM_LABELS.
     */
    public int predictClass(String sentence) {
        Annotation ann = new Annotation(sentence);
        pipeline.annotate(ann);
        CoreMap sent = ann.get(CoreAnnotations.SentencesAnnotation.class).get(0);
        String cls = sent.get(SentimentCoreAnnotations.SentimentClass.class);
        if (cls == null) return -1;
        cls = cls.trim();
        // Try numeric first
        try {
            int id = Integer.parseInt(cls);
            if (id >= 0 && id < CUSTOM_LABELS.length) return id;
            return -1;
        } catch (NumberFormatException ex) {
            // Map common textual sentiment names to indices
            String k = cls.toLowerCase();
            switch (k) {
                case "verynegative":
                case "very negative":
                case "very-negative":
                case "very_negative":
                    return 0;
                case "negative":
                    return 1;
                case "neutral":
                    return 2;
                case "positive":
                    return 3;
                case "verypositive":
                case "very positive":
                case "very-positive":
                case "very_positive":
                    return 4;
                default:
                    // If cls already matches one of the custom labels, return its index
                    for (int i = 0; i < CUSTOM_LABELS.length; i++) {
                        if (CUSTOM_LABELS[i].equalsIgnoreCase(cls)) return i;
                    }
                    System.err.println("Unrecognized sentiment class: " + cls);
                    return -1;
            }
        }
    }

    /**
     * Returns the remapped label (custom string) for the first sentence.
     * If the predicted class is unknown, returns the raw sentiment string if available.
     */
    public String predictLabel(String sentence) {
        Annotation ann = new Annotation(sentence);
        pipeline.annotate(ann);
        CoreMap sent = ann.get(CoreAnnotations.SentencesAnnotation.class).get(0);
        String cls = sent.get(SentimentCoreAnnotations.SentimentClass.class);
        if (cls == null) return null;
        cls = cls.trim();
        // Try numeric
        try {
            int id = Integer.parseInt(cls);
            if (id >= 0 && id < CUSTOM_LABELS.length) return CUSTOM_LABELS[id];
        } catch (NumberFormatException ex) {
            // Map textual
            String k = cls.toLowerCase();
            switch (k) {
                case "verynegative":
                case "very negative":
                case "very-negative":
                case "very_negative":
                    return CUSTOM_LABELS[0];
                case "negative":
                    return CUSTOM_LABELS[1];
                case "neutral":
                    return CUSTOM_LABELS[2];
                case "positive":
                    return CUSTOM_LABELS[3];
                case "verypositive":
                case "very positive":
                case "very-positive":
                case "very_positive":
                    return CUSTOM_LABELS[4];
                default:
                    for (String s : CUSTOM_LABELS) if (s.equalsIgnoreCase(cls)) return s;
                    // Fallback: return raw
                    return cls;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: SentimentPredictor <model.ser.gz> <sentence>");
            System.exit(2);
        }
        String model = args[0];
        String sentence = args[1];
        SentimentPredictor p = new SentimentPredictor(model);
        int cls = p.predictClass(sentence);
        String label = p.predictLabel(sentence);
        // Output results for CLI usage
        System.out.println("Predicted class id: " + cls);
        System.out.println("Predicted label: " + label);
    }
}

