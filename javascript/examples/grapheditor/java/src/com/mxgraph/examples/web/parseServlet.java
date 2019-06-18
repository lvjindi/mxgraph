import javax.servlet.http.HttpServlet;
import javax.swing.plaf.synth.SynthTextAreaUI;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class parseServlet extends HttpServlet {
    public static void main(String[] args) throws Exception {

        SAXReader reader = new SAXReader();
        File file = new File("E:\\LabProject\\mxgraph\\Drawing5.xml");
        Document doc = reader.read(file);
        Document doc1 = DocumentHelper.createDocument();
        Element root1 = doc1.addElement("nta");
        Element declaration = root1.addElement("declaration");
        declaration.addText("typedef int[1,6] id_t;\n" +
                "int id;");
        Element template = root1.addElement("template");
        Element name = template.addElement("name");
        name.addText(file.getName());
        List<Element> vertexList = new ArrayList<>();
        List<Element> edgeList = new ArrayList<>();

        Element root = doc.getRootElement();
        Element list = root.element("root");

        if (haschildren(list)) {
            List<Element> cellList = list.elements();
            Iterator<Element> iterator = cellList.iterator();
            while (iterator.hasNext()) {
                Element cell = iterator.next();
                System.out.print("节点名：" + cell.getName() + '\n');
                if (findCellAttribute(cell, "vertex") != null) {
                    vertexList.add(cell);
                } else if (findCellAttribute(cell, "edge") != null) {
                    edgeList.add(cell);
                } else {
                    if (cell.getName() != "mxCell") {
                        Element point = template.addElement(cell.getName());
                        point.addText(cell.getText());
                    }
                }
            }

            String initialId = new String("");
            for (Element item : vertexList) {
                createVertex(template, item);
                Attribute attrId = findCellAttribute(item, "id");
                Attribute attrInitial = findCellAttribute(item, "initial");
                if (attrInitial != null) {
                    initialId = attrId.getValue();
                }
            }
            Element initial = template.addElement("init");
            initial.addAttribute("ref", initialId);
            for (Element item : edgeList) {
                createEdge(template, item);
            }

        }
        XMLWriter writer = new XMLWriter(new FileOutputStream("parse.xml"),
                OutputFormat.createPrettyPrint());
        writer.write(doc1);
        System.out.println("写出完毕");
        writer.close();

    }


    public static void createLabel(Element ele, Attribute attr) {
        Element guard = ele.addElement("label");
        if (attr.getName() == "update") {

            guard.addAttribute("kind", "assignment");
        } else {
            guard.addAttribute("kind", attr.getName());
        }
        guard.addText(attr.getValue());
    }

    public static boolean haschildren(Element ele) {
        List<Element> children = ele.elements();
        if (children.size() > 0) {
            return true;
        } else {
            return false;
        }
    }


    public static Attribute findCellAttribute(Element ele, String attrName) {
        Attribute temp = null;
        List<Attribute> attributeList = ele.attributes();
        for (Attribute attr : attributeList) {
            if (attr.getName() == attrName) {
                temp = attr;
                break;
            }
        }
        return temp;
    }

    public static void createVertex(Element template, Element ele) {
        Element vertex = template.addElement("location");
        if (haschildren(ele)) {
            List<Element> children = ele.elements();
            Iterator<Element> iterator = children.iterator();
            while (iterator.hasNext()) {
                Element child = iterator.next();
                Attribute attrX = findCellAttribute(child, "x");
                Attribute attrY = findCellAttribute(child, "y");
                Attribute attrId = findCellAttribute(ele, "id");
                Attribute attrName = findCellAttribute(ele, "name");
                Attribute attrInvariant = findCellAttribute(ele, "invariant");

                if (attrX != null && attrY != null && attrId != null) {
                    vertex.addAttribute(attrX.getName(), attrX.getValue());
                    vertex.addAttribute(attrY.getName(), attrY.getValue());
                    vertex.addAttribute(attrId.getName(), attrId.getValue());
                    if (attrName != null) {
                        Element name = vertex.addElement("name");
                        name.addText(attrName.getValue());
                    }
                    if (attrInvariant != null) {
                        createLabel(vertex, attrInvariant);
                    }
//
                }

            }
        }

    }

    public static void createEdge(Element template, Element ele) {
        Element vertex = template.addElement("transition");
        if (haschildren(ele)) {
            List<Element> children = ele.elements();
            Iterator<Element> iterator = children.iterator();
            while (iterator.hasNext()) {
                Element child = iterator.next();
                Attribute attrSource = findCellAttribute(ele, "source");
                Attribute attrTarget = findCellAttribute(ele, "target");
                Attribute attrGuard = findCellAttribute(ele, "guard");
                Attribute attrUpdate = findCellAttribute(ele, "update");
                if (attrSource != null && attrTarget != null) {
                    Element source = vertex.addElement(attrSource.getName());
                    Element target = vertex.addElement(attrTarget.getName());
                    source.addAttribute("ref", attrSource.getValue());
                    target.addAttribute("ref", attrTarget.getValue());
                    if (attrGuard != null) {
                        createLabel(vertex, attrGuard);
                    }
                    if (attrUpdate != null) {
                        createLabel(vertex, attrUpdate);
                    }
                }
            }
        }

    }

}
