package com.santho.pdfparser;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.*;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.*;

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

    public static Map<Integer, List<String>> highlightedTexts(File pdfFile, float[] color){
        if (isNotPdf(pdfFile)) throw new IllegalArgumentException("Not a PDF file");
        Map<Integer, List<String>> highlightedTexts= new HashMap<>();
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDPageTree allPages = document.getPages();
            int pageNo = 1;
            for (PDPage page : allPages) {
                List<PDAnnotation> annotations = page.getAnnotations();
                PDFTextStripperByArea areaStripper = new PDFTextStripperByArea();
                for (PDAnnotation annotation : annotations) {
                    if (annotation.getSubtype().equalsIgnoreCase("highlight")) {
                        PDAnnotationHighlight highlight = (PDAnnotationHighlight) annotation;
                        if(compare(annotation.getColor().getComponents(), color)){
                            Rectangle2D[] activeArea = getActiveRectangle(highlight.getQuadPoints(), page.getCropBox().getHeight());
                            for (Rectangle2D rect : activeArea)
                                areaStripper.addRegion("hightlight-" + rect.hashCode(), rect);
                        }
                    }
                }
                areaStripper.extractRegions(page);
                areaStripper.setSortByPosition(true);
                List<String> highlighted = areaStripper.getRegions();
                List<String> marked = new ArrayList<>();
                for (String region : highlighted) {
                    marked.add(areaStripper.getTextForRegion(region));
                }
                if(!marked.isEmpty())
                    highlightedTexts.put(pageNo, marked);
                pageNo++;
            }
            return highlightedTexts;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new IllegalArgumentException("Error fetching links");
        }
    }

    private static boolean compare(float[] components, float[] color) {
        if(components.length != color.length){
            return false;
        }
        for(int i = 0; i < components.length; i++){
            if(Math.abs(components[i] - color[i]) >= 0.001){
                return false;
            }
        }
        return true;
    }

    public static Map<Integer, List<String>> highlightedTexts(File pdfFile) {
        if (isNotPdf(pdfFile)) throw new IllegalArgumentException("Not a PDF file");
        Map<Integer, List<String>> highlightedTexts= new HashMap<>();
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDPageTree allPages = document.getPages();
            int pageNo = 1;
            for (PDPage page : allPages) {
                List<PDAnnotation> annotations = page.getAnnotations();
                PDFTextStripperByArea areaStripper = new PDFTextStripperByArea();
                for (PDAnnotation annotation : annotations) {
                    if (annotation.getSubtype().equalsIgnoreCase("highlight")) {
                        System.out.println(annotation.getColor());
                        PDAnnotationHighlight highlight = (PDAnnotationHighlight) annotation;
                        Rectangle2D[] activeArea = getActiveRectangle(highlight.getQuadPoints(), page.getCropBox().getHeight());
                        for (Rectangle2D rect : activeArea)
                            areaStripper.addRegion("hightlight-" + rect.hashCode(), rect);
                    }
                }
                areaStripper.extractRegions(page);
                areaStripper.setSortByPosition(true);
                List<String> highlighted = areaStripper.getRegions();
                List<String> marked = new ArrayList<>();
                for (String region : highlighted) {
                    marked.add(areaStripper.getTextForRegion(region));
                }
                if(!marked.isEmpty())
                    highlightedTexts.put(pageNo, marked);
                pageNo++;
            }
            return highlightedTexts;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new IllegalArgumentException("Error fetching links");
        }
    }

    public static List<String> links(File pdfFile) {
        if (isNotPdf(pdfFile)) throw new IllegalArgumentException("Not a PDF file");
        List<String> urls = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDPageTree allPages = document.getPages();
            for (PDPage page : allPages) {
                List<PDAnnotation> annotations = page.getAnnotations();
                for (PDAnnotation annotation : annotations) {
                    if (annotation instanceof PDAnnotationLink) {
                        PDAnnotationLink link = (PDAnnotationLink) annotation;
                        if (link.getAction() instanceof PDActionURI)
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

    private static Rectangle2D[] getActiveRectangle(float[] quadPoints, float pageHeight) {
        if (quadPoints == null || quadPoints.length < 8) {
            return new Rectangle2D[0];
        }
        Rectangle2D[] activeRectangles = new Rectangle2D[quadPoints.length / 8];
        for (int i = 0, j = 0; i < quadPoints.length; i += 8, j++) {
            float x1 = i + quadPoints[0];
            float y1 = i + quadPoints[1];
            float x2 = i + quadPoints[2];
            float y3 = i + quadPoints[5];
            float width = x2 - x1;
            float height = y1 - y3;
            activeRectangles[j] = new Rectangle2D.Float(x1, pageHeight - y1, width, height);
        }
        return activeRectangles;
    }
}
