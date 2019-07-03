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
        parseXml();

    }

    //将mxgraph输出的xml转化成uppal的xml形式
    public static void parseXml() throws Exception {
        SAXReader reader = new SAXReader();
        File file = new File("E:\\LabProject\\mxgraph\\Drawing1.xml");
        Document doc = reader.read(file);//read

        Document doc1 = DocumentHelper.createDocument();//write
        Element root1 = doc1.addElement("nta");

        List<Element> templateList = new ArrayList<>();
        List<Element> vertexList = new ArrayList<>();
        List<Element> edgeList = new ArrayList<>();

        Element root = doc.getRootElement();
        Element list = root.element("root");
        List<Element> cellList = list.elements();
        Iterator<Element> iterator = cellList.iterator();

        while (iterator.hasNext()) {
            Element cell = iterator.next();
            Attribute style = findCellAttribute(cell, "style");

            if (cell.getName() == "declaration") {
                Element declaration = root1.addElement("declaration");
                declaration.addText(cell.getText());
            }

            if (style != null && style.getValue().indexOf("swimlane") != -1) {
                templateList.add(cell);
            }

            if (findCellAttribute(cell, "vertex") != null) {
                vertexList.add(cell);
            } else if (findCellAttribute(cell, "edge") != null) {
                edgeList.add(cell);
            }

        }

        for (Element item : templateList) {
            String initialId = "";
            Attribute attrId = findCellAttribute(item, "id");
            Element template = createTemplate(root1, item);

            String id = attrId.getValue();
            Iterator<Element> vertexIterator = vertexList.iterator();
            Iterator<Element> edgeIterator = edgeList.iterator();

            while (vertexIterator.hasNext()) {
                Element vertex = vertexIterator.next();
                Attribute parent = findCellAttribute(vertex, "parent");
                Attribute attrInitial = findCellAttribute(vertex, "initial");
                if (parent.getValue().equals(id)) {
                    if (attrInitial != null) {
                        Attribute vertexId = findCellAttribute(vertex, "id");
                        initialId = vertexId.getValue();
                    }
                    createVertex(template, vertex);
                }
            }

            Element initial = template.addElement("init");
            initial.addAttribute("ref", initialId);

            while (edgeIterator.hasNext()) {
                Element edge = edgeIterator.next();
                Attribute parent = findCellAttribute(edge, "parent");
                if (parent.getValue().equals(id)) {
                    createEdge(template, edge);
                }
            }
        }

        XMLWriter writer = new XMLWriter(new FileOutputStream("parse.xml"),
                OutputFormat.createPrettyPrint());
        writer.write(doc1);
        writer.close();
    }

    public static Element createTemplate(Element root1, Element item) {
        Attribute attrName = findCellAttribute(item, "value");
        Attribute attrDeclaration = findCellAttribute(item, "declaration");
        Attribute attrParameter = findCellAttribute(item, "parameter");
        Element template = root1.addElement("template");
        Element name = template.addElement("name");
        name.addText(attrName.getValue());
        if (attrParameter != null) {
            Element parameter = template.addElement("parameter");
            parameter.addText(attrParameter.getValue());
        }
        if (attrDeclaration != null) {
            Element declaration = template.addElement("declaration");
            declaration.addText(attrDeclaration.getValue());
        }
        return template;
    }

    public static void createLabel(Element ele, Attribute attr) {
        Element guard = ele.addElement("label");
        if (attr.getName() == "update") {
            guard.addAttribute("kind", "assignment");
        } else if (attr.getName() == "sync") {
            guard.addAttribute("kind", "synchronisation");
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
                Attribute attrSyn = findCellAttribute(ele, "sync");
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
                    if (attrSyn != null) {
                        createLabel(vertex, attrSyn);
                    }
                }
            }
        }

    }

}
