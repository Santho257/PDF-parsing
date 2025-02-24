package com.santho;

import com.santho.pdfparser.PdfParser;

import java.io.File;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
//        PdfParser.createTextPDF();
        File file = new File("/Users/santhosh-22951/Documents/Zoho-Java-Learnings/HyperLinkParsing/src/main/resources/Sample.pdf");
        System.out.println(PdfParser.highlightedTexts(file).toString().replace("\n", "").replace("], ", "]\n"));
        System.out.println(PdfParser.highlightedTexts(file, new float[]{1, 1, 0.6f}).toString());
//        System.out.println(PdfParser.links(file));
    }
}