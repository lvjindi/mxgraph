import com.mxgraph.canvas.mxGraphicsCanvas2D;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLDecoder;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import com.mxpdf.text.DocumentException;

import com.mxpdf.text.pdf.BaseFont;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.mxpdf.text.Document;
import com.mxpdf.text.Rectangle;
import com.mxpdf.text.pdf.PdfWriter;
import com.mxgraph.canvas.mxICanvas2D;
import com.mxgraph.reader.mxDomOutputParser;
import com.mxgraph.reader.mxSaxOutputHandler;
import com.mxgraph.util.mxUtils;
import com.mxgraph.util.mxXmlUtils;

public class ExportServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        int height = 0;
        int width = 0;
        String format = URLDecoder.decode(request.getParameter("format"), "UTF-8").replace("\n", "&#xa;");
        String filename = URLDecoder.decode(request.getParameter("filename"), "UTF-8").replace("\n", "&#xa;");
        String xml = URLDecoder.decode(request.getParameter("xml"), "UTF-8").replace("\n", "&#xa;");
        try {
            height = Integer.parseInt(URLDecoder.decode(request.getParameter("h"), "UTF-8").replace("\n", "&#xa;"));
            width = Integer.parseInt(URLDecoder.decode(request.getParameter("w"), "UTF-8").replace("\n", "&#xa;"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        long t0 = System.currentTimeMillis();
        BufferedImage image = mxUtils.createBufferedImage(width, height, Color.WHITE);

        // Creates handle and configures anti-aliasing
        Graphics2D g2 = image.createGraphics();
        mxUtils.setAntiAlias(g2, true, true);
        long t1 = System.currentTimeMillis();

        // Parses request into graphics canvas
        mxGraphicsCanvas2D gc2 = new mxGraphicsCanvas2D(g2);
        try {
            parseXmlSax(xml, gc2);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        long t2 = System.currentTimeMillis();
        if (format.equals("pdf")) {
            // For PDF export using iText from http://www.lowagie.com/iText/
            Document document = new Document(new Rectangle((float) width, (float) height));
            PdfWriter writer = null;
            try {
                writer = PdfWriter.getInstance(document,
                        new FileOutputStream(filename));
            } catch (DocumentException e) {
                e.printStackTrace();
            }
            document.open();

            gc2 = new mxGraphicsCanvas2D(writer.getDirectContent().createGraphics(width, height));
            try {
                parseXmlSax(xml, gc2);
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }

            gc2.getGraphics().dispose();
            document.close();
        } else if (format.equals("xml")) {
            File file = new File(filename);
            try {
                FileWriter fw = new FileWriter(file);
                if (!file.exists()) {
                    file.createNewFile();
                }
                fw.write(xml);
                fw.flush();
                fw.close();
                System.out.print("写入文件成功");

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            ImageIO.write(image, format, new File(filename));
            long t3 = System.currentTimeMillis();
        }
        System.out.println("Create img: " + (t1 - t0) + " ms, Parse XML: "
                + (t2 - t1) + " ms, Write File: ");

    }

    protected void parseXmlDom(String xml, mxICanvas2D canvas) {
        new mxDomOutputParser(canvas).read(mxXmlUtils.parseXml(xml)
                .getDocumentElement().getFirstChild());
    }

    protected void parseXmlSax(String xml, mxICanvas2D canvas)
            throws SAXException, ParserConfigurationException, IOException {
        // Creates SAX handler for drawing to graphics handle
        mxSaxOutputHandler handler = new mxSaxOutputHandler(canvas);

        // Creates SAX parser for handler
        XMLReader reader = SAXParserFactory.newInstance().newSAXParser()
                .getXMLReader();
        reader.setContentHandler(handler);

        // Renders XML data into image
        reader.parse(new InputSource(new StringReader(xml)));
    }
}
