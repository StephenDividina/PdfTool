package com.mobigator;


import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

import static org.junit.jupiter.api.Assertions.*;

public class TestPdfTool {

    @Test
    public void testJasperRequestMethod() throws IOException {
        HttpURLConnection connection = PdfTool.jasperRequest("{\"reportParams\":[{\"JRXML_BASEPATH\":\".\",\"reportName\":\"/MR4130/MR4130\",\"SUBREPORT_DIR\":\".\",\"FORM_ID\":1}]}");
        assertEquals(200, connection.getResponseCode());
        assertNotNull(connection.getInputStream());
    }

    @Test
    public void testSavePDF() throws IOException {
        String savedDirectory = "C:\\devs";
        String fileName = PdfTool.createPdfFromJasper("{\"reportParams\":[{\"JRXML_BASEPATH\":\".\",\"reportName\":\"/MR4130/MR4130\",\"SUBREPORT_DIR\":\".\",\"FORM_ID\":1}]}", savedDirectory);
        File file = new File(fileName);
        assertEquals(fileName, file.getAbsolutePath());
        assertTrue(file.exists());
        FileUtils.deleteDirectory(new File(savedDirectory));
        assertTrue(!file.exists());
    }
}
