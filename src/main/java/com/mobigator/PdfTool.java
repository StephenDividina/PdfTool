package com.mobigator;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PdfTool {
    public static void main(String[] args) throws Exception {
        if (args.length > 3){
            throw new Exception("Parameter length is longer than expected. " + args.length);
        }

        String pdfDummy = "";
        pdfDummy = createPdfFromJasper(args[0], args[2]);
        savePdfWithPageNumber(mergePdf(args[1], pdfDummy), args[2]);

//        pdfDummy = createPdfFromJasper("{\"reportParams\":[{\"JRXML_BASEPATH\": \".\",\"reportName\":\"/MR4130/MR4130\",\"SUBREPORT_DIR\": \".\",\"FORM_ID\": 1}]}", "C:\\dev\\Pdf_tool");
//        savePdfWithPageNumber(mergePdf("C:\\dev\\Pdf_tool\\report.pdf", pdfDummy), "C:\\dev\\Pdf_tool");

//        savePdfWithPageNumber("C:\\dev\\Pdf_tool\\report.pdf", "C:\\dev\\Pdf_tool");

        try{
            File file = new File(pdfDummy);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e){
            throw new Exception(e);
        }

    }

    public static String createPdfFromJasper(String jsonData, String savedTargetLocation) {
        try {
            File file = new File(savedTargetLocation);
            if (!file.exists()){
                file.mkdirs();
            }

            HttpURLConnection connection = jasperRequest(jsonData);

            InputStream in = connection.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            int i;
            while ((i = in.read()) != -1) {
                bos.write(i);
            }

            byte[] bytes = bos.toByteArray();
            FileOutputStream outputStream = new FileOutputStream(savedTargetLocation + "\\dummy.pdf");
            outputStream.write(bytes);
            outputStream.close();

            connection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return savedTargetLocation + "\\dummy.pdf";
    }

    public static HttpURLConnection jasperRequest(String jsonData) throws IOException {
        URL url = new URL("http://localhost:83/Jasper/jasperReports/batch");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Cache-Control", "no-cache");
        connection.setRequestProperty("Postman-Token", "<calculated when request is sent>");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Length", "<calculated when request is sent>");
        connection.setRequestProperty("Host", "<calculated when request is sent>");
        connection.setRequestProperty("User-Agent", "PostmanRuntime/7.26.8");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Accept", "text/html");

        OutputStream os = connection.getOutputStream();
        os.write(jsonData.getBytes());
        os.flush();

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
        }

        return connection;
    }

    public static String mergePdf(String coverPage, String savedPDFLocation) throws IOException {
        PDFMergerUtility mergerUtility = new PDFMergerUtility();
        mergerUtility.addSource(coverPage);
        mergerUtility.addSource(savedPDFLocation);
        mergerUtility.setDestinationFileName(savedPDFLocation);
        mergerUtility.mergeDocuments();

        return mergerUtility.getDestinationFileName();
    }

    public static void savePdfWithPageNumber(String pdf, String savedLocation) throws IOException {

        String date = new SimpleDateFormat("YYYY-MM-DD").format(new Date());
        String time = new SimpleDateFormat("HHmmss").format(new Date());

        String name = "\\report_" + date + "_T" + time + ".pdf";

        PDDocument doc = null;
        try
        {
            doc = PDDocument.load(new File(pdf));

            int allPages = doc.getNumberOfPages();
            PDFont font = PDType1Font.TIMES_ROMAN;
            float fontSize = 15f;

            for( int i=0; i<allPages; i++ )
            {
                PDPage page = doc.getPage(i);
                PDRectangle pageSize = page.getMediaBox();
                float stringWidth = font.getStringWidth(String.format("Page %d of %d", i+1, allPages))*fontSize/1000f;
                // calculate to center of the page
                int rotation = page.getRotation();
                boolean rotate = rotation == 90 || rotation == 270;
                float pageWidth = rotate ? pageSize.getHeight() : pageSize.getWidth();
                float pageHeight = rotate ? pageSize.getWidth() : pageSize.getHeight();
                double centeredXPosition = rotate ? pageHeight/2f : (pageWidth - stringWidth)/2f;
                double centeredYPosition = rotate ? (pageWidth - stringWidth)/2f : pageHeight/2f;
                // append the content to the existing stream
                PDPageContentStream contentStream = new PDPageContentStream(doc, page, true, true,true);
                contentStream.beginText();
                // set font and font size
                contentStream.setFont( font, fontSize );
                // set text color to red
                contentStream.setNonStrokingColor(0, 0, 0);
                if (rotate)
                {
                    // rotate the text according to the page rotation
                    contentStream.setTextRotation(Math.PI/2, centeredXPosition, centeredYPosition);
                }
//                else
//                {
//                    contentStream.setTextTranslation(centeredXPosition, centeredYPosition);
//                }
//                contentStream.newLineAtOffset(230, -390);
                contentStream.newLineAtOffset(pageWidth - (stringWidth + 20),  30);
                contentStream.drawString( String.format("Page %d of %d", i+1, allPages));
                contentStream.endText();
                contentStream.close();
            }

            doc.save(savedLocation + name);
        }
        finally
        {
            if( doc != null )
            {
                doc.close();
            }
        }
    }

}

//{\"reportParams\":[{\"JRXML_BASEPATH\":\".\",\"reportName\":\"/MR4130/MR4130\",\"SUBREPORT_DIR\":\".\",\"FORM_ID\":1}]}