package com.mobigator;

import com.spire.pdf.PdfDocument;
import com.spire.pdf.automaticfields.PdfCompositeField;
import com.spire.pdf.automaticfields.PdfPageCountField;
import com.spire.pdf.automaticfields.PdfPageNumberField;
import com.spire.pdf.graphics.*;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.awt.*;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PdfTool {
    public static void main(String[] args) throws Exception {
        System.out.println("Stephen");
        if (args.length > 3){
            throw new Exception("Parameter length is longer than expected. " + args.length);
        }

        String pdfDummy = "";
//        pdfDummy = requestPdfFromJasper(args[0], args[2]);
//        savePdfWithPageNumber(mergePdf(args[1], pdfDummy), args[2]);

        pdfDummy = requestPdfFromJasper("{\"reportParams\":[{\"JRXML_BASEPATH\": \".\",\"reportName\":\"/MR4130/MR4130\",\"SUBREPORT_DIR\": \".\",\"FORM_ID\": 1}]}", "C:\\dev\\Pdf_tool");
        savePdfWithPageNumber(mergePdf("C:\\dev\\Pdf_tool\\report.pdf", pdfDummy), "C:\\dev\\Pdf_tool");

//        savePdfWithPageNumber("C:\\dev\\PdfTool\\report.pdf", "C:\\dev\\PdfTool");

        try{
            File file = new File(pdfDummy);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e){
            throw new Exception(e);
        }

    }

    private static String requestPdfFromJasper(String jsonData, String savedTargetLocation) {
        try {
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

    private static void savePdfWithPageNumber(String pdf, String savedLocation) {

        String date = new SimpleDateFormat("YYYY-MM-DD").format(new Date());
        String time = new SimpleDateFormat("HHmmss").format(new Date());

        String name = "\\report_" + date + "_T" + time + ".pdf";

        PdfDocument doc = new PdfDocument();
        doc.loadFromFile(pdf);

        PdfTrueTypeFont font = new PdfTrueTypeFont(new Font("Times New Roman", Font.PLAIN, 12));

        Dimension2D pageSize = doc.getPages().get(0).getSize();

        float y = (float) pageSize.getHeight() - 40;

        for (int i = 0; i < doc.getPages().getCount(); i++) {

            PdfPageNumberField number = new PdfPageNumberField();

            PdfPageCountField count = new PdfPageCountField();

            PdfCompositeField compositeField = new PdfCompositeField(font, PdfBrushes.getBlack(), "Page {0} of {1}", number, count);

            compositeField.setStringFormat(new PdfStringFormat(PdfTextAlignment.Right, PdfVerticalAlignment.Top));

            Dimension2D textSize = font.measureString(compositeField.getText());

            compositeField.setBounds(new Rectangle2D.Float((float) pageSize.getWidth() - (float) textSize.getWidth() - 30, y, (float) textSize.getWidth(), (float) textSize.getHeight()));

            compositeField.draw(doc.getPages().get(i).getCanvas());
        }
        doc.saveToFile(savedLocation + name);
    }

    private static String mergePdf(String coverPage, String savedPDFLocation) throws IOException {
        PDFMergerUtility mergerUtility = new PDFMergerUtility();
        mergerUtility.addSource(coverPage);
        mergerUtility.addSource(savedPDFLocation);
        mergerUtility.setDestinationFileName(savedPDFLocation);
        mergerUtility.mergeDocuments();

        return mergerUtility.getDestinationFileName();
    }
}
