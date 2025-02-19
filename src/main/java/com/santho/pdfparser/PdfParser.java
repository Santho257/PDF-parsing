package com.santho.pdfparser;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.TIMES_ROMAN;

public class PdfParser {
    public static void createTextPDF() {
        File file = new File("src/main/resources/Sample.pdf");
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            PDPage readPage = document.getPage(0);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, readPage)) {
                contentStream.beginText();
                contentStream.setLeading(14.5f);
                contentStream.newLineAtOffset(25, 450);
                contentStream.setFont(new PDType1Font(TIMES_ROMAN), 12);
                contentStream.showText("Hello Everyone");
                contentStream.newLine();
                contentStream.showText("This is a sample file");
                contentStream.endText();
            }
            document.save(file);
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
            throw new IllegalArgumentException("Error Writing PDF file");
        }
    }

    public static String textContents(File pdfFile) {
        if (isNotPdf(pdfFile)) throw new IllegalArgumentException("Not a PDF file");
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper textStripper = new PDFTextStripper();
            return textStripper.getText(document);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error Parsing text contents");
        }
    }

    public static List<String> links(File pdfFile) {
        if (isNotPdf(pdfFile)) throw new IllegalArgumentException("Not a PDF file");
        List<String> urls = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDPageTree allPages =  document.getPages();
            for(PDPage page : allPages){
                List<PDAnnotation> annotations = page.getAnnotations();
                for(PDAnnotation annotation : annotations){
                    PDAnnotationLink link = (PDAnnotationLink) annotation;
                    if(link.getAction() instanceof PDActionURI){
                        urls.add(((PDActionURI) link.getAction()).getURI());
                    }
                }
            }
            return urls;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new IllegalArgumentException("Error fetching links");
        }
    }

    private static boolean isNotPdf(File file) {
        String filename = file.getName();
        return !"pdf".equalsIgnoreCase(filename.substring(filename.lastIndexOf(".") + 1));
    }
}
