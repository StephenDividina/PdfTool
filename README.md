# PdfTool
pdf and jasper merger

command for creating jar file:
mvn clean install

location of jar file:
test

how to run jar file:
ex.
java -jar <name of jar file> <first args: jasper data> <second args: name of pdf to merge or cover page> <third args: location where to save> <fourth args: url of jasper>

sample
java -jar PdfTool-1.0-SNAPSHOT-jar-with-dependencies.jar {\"reportParams\":[{\"JRXML_BASEPATH\":\".\",\"reportName\":\"/MR4130/MR4130\",\"SUBREPORT_DIR\":\".\",\"FORM_ID\":1}]} C:\dev\Pdf_tool\report.pdf C:\dev localhost:8080
