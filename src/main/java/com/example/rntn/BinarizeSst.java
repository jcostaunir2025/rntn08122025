package com.example.rntn;

import edu.stanford.nlp.trees.Tree;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility to binarize SST-format files so they are compatible with SentimentTraining.
 * Usage:
 *   java -cp target\rntn-project-1.0-SNAPSHOT-shaded.jar com.example.rntn.BinarizeSst input.sst output.sst
 */
public class BinarizeSst {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: BinarizeSst <input.sst> <output.sst>");
            System.exit(2);
        }
        Path in = Path.of(args[0]);
        Path out = Path.of(args[1]);
        try (BufferedReader br = new BufferedReader(new FileReader(in.toFile()));
             BufferedWriter bw = new BufferedWriter(new FileWriter(out.toFile()))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                Tree t = Tree.valueOf(line);
                Tree b = binarizeTree(t);
                bw.write(b.pennString());
                bw.newLine();
            }
        }
        System.out.println("Wrote binarized SST to: " + out.toAbsolutePath());
    }

    private static Tree binarizeTree(Tree t) {
        if (t.isLeaf()) return t;
        List<Tree> kids = new ArrayList<>();
        for (Tree c : t.children()) {
            kids.add(binarizeTree(c));
        }
        if (kids.size() <= 2) {
            return t.treeFactory().newTreeNode(t.label(), kids);
        }
        // left-branching combine
        while (kids.size() > 2) {
            Tree a = kids.remove(0);
            Tree b = kids.remove(0);
            List<Tree> pair = Arrays.asList(a, b);
            Tree merged = t.treeFactory().newTreeNode(t.label(), pair);
            kids.add(0, merged);
        }
        return t.treeFactory().newTreeNode(t.label(), kids);
    }
}

