package com.example.rntn;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * CsvToSstConverter:
 * Converts a CSV with two columns: sentence,label into an SST-style tree file where each line
 * is a labeled parenthesized tree. If phrase-level annotations are not present, the sentence-level
 * label is propagated to all nodes.
 *
 * CSV format: header optional. Columns: sentence,label
 *
 * Example:
 * "I feel hopeless and anxious.",0
 *
 * Output line:
 * (0 (0 I) (0 (0 feel) (0 (0 hopeless) (0 (0 and) (0 anxious)))) (. .))
 *
 */
public class CsvToSstConverter {

    private final TreeConverter converter;

    public CsvToSstConverter() {
        this.converter = new TreeConverter();
    }

    public void convert(File csvFile, File outSstFile, boolean hasHeader) throws IOException {
        try (Reader in = new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outSstFile), StandardCharsets.UTF_8))) {

            CSVFormat format = hasHeader ? CSVFormat.DEFAULT.withFirstRecordAsHeader() : CSVFormat.DEFAULT;
            CSVParser parser = new CSVParser(in, format);

            for (CSVRecord record : parser) {
                String sentence = record.get(0);
                String labelStr = record.get(1);
                int label;
                try {
                    label = Integer.parseInt(labelStr.trim());
                } catch (NumberFormatException ex) {
                    // default to 0 if label invalid
                    label = 0;
                }
                String labeledTree = converter.getLabeledTreeWithPropagation(sentence, label);
                writer.write(labeledTree);
                writer.newLine();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: CsvToSstConverter <input.csv> <output.sst> [header:true|false]");
            System.exit(2);
        }
        File in = new File(args[0]);
        File out = new File(args[1]);
        boolean header = true;
        if (args.length >= 3) header = Boolean.parseBoolean(args[2]);
        CsvToSstConverter conv = new CsvToSstConverter();
        conv.convert(in, out, header);
        System.out.println("Converted " + in + " -> " + out);
    }
}
