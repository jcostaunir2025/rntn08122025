package com.example.rntn;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.Properties;

/**
 * TreeConverter: produces a Penn-style constituency parse and renders a labeled tree string
 * in the Stanford Sentiment Treebank format by propagating a sentence-level numeric label
 * to all internal nodes. This is a pragmatic approach when phrase-level labels are not available.
 */
public class TreeConverter {

    private final StanfordCoreNLP pipeline;

    public TreeConverter() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,parse");
        // reduce memory usage by disabling neural dependency parser if present
        this.pipeline = new StanfordCoreNLP(props);
    }

    /**
     * Returns the constituency tree (Penn Treebank style) for the first sentence.
     */
    public String getParseTree(String sentence) {
        Annotation ann = new Annotation(sentence);
        pipeline.annotate(ann);
        CoreMap sent = ann.get(CoreAnnotations.SentencesAnnotation.class).get(0);
        Tree tree = sent.get(TreeCoreAnnotations.TreeAnnotation.class);
        return tree.pennString();
    }

    /**
     * Render a labeled tree in SST-style by prepending numeric labels to each subtree.
     * This implementation uses a recursive renderer that prints "(<label> <subtree...>)".
     *
     * Note: we simply propagate rootLabel to all nodes when phrase-level labels are not provided.
     */
    public String getLabeledTreeWithPropagation(String sentence, int rootLabel) {
        Annotation ann = new Annotation(sentence);
        pipeline.annotate(ann);
        CoreMap sent = ann.get(CoreAnnotations.SentencesAnnotation.class).get(0);
        Tree tree = sent.get(TreeCoreAnnotations.TreeAnnotation.class);
        return renderWithLabel(tree, rootLabel);
    }

    private String renderWithLabel(Tree node, int label) {
        // If leaf node (word), print "(<label> word)"
        if (node.isLeaf()) {
            // For leaves, node.label().value() is the word.
            return "(" + label + " " + escape(node.label().value()) + ")";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(label).append(" ");
        // For non-terminals, we need to print node's label (e.g., NP, VP) as well as children.
        // SST format usually omits the syntactic category after the label and directly nests children,
        // so we will iterate children and render them.
        for (int i = 0; i < node.numChildren(); i++) {
            Tree child = node.getChild(i);
            sb.append(renderWithLabel(child, label));
            if (i < node.numChildren() - 1) sb.append(" ");
        }
        sb.append(")");
        return sb.toString();
    }

    private String escape(String token) {
        // Minimal escaping for parentheses used in tokens.
        return token.replace("(", "-LRB-").replace(")", "-RRB-");
    }
}
