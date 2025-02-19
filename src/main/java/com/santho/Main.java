package com.santho;

import com.santho.pdfparser.PdfParser;

import java.io.File;

public class Main {
    public static void main(String[] args) {
//        PdfParser.createTextPDF();
        File file = new File("/Users/santhosh-22951/Documents/Zoho-Java-Learnings/HyperLinkParsing/src/main/resources/learning.pdf");
//        System.out.println(PdfParser.links(file));
        System.out.println(PdfParser.textContents(file));
    }
}