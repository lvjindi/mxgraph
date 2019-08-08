import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.net.URLDecoder;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sun.javafx.collections.MappingChange;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class VerifyServlet extends HttpServlet {
    private List<String> lists = new ArrayList<String>();

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        String xml = URLDecoder.decode(request.getParameter("xml"), "UTF-8").replace("\n", "&#xa;");
        String query = request.getParameter("property");
        System.out.println(query);
        try {
            parseXml(xml, query);
        } catch (Exception e) {
            e.printStackTrace();
        }

        BufferedReader br = null;
        try {
            String cmd = "E:\\program\\uppaal-4.1.19\\bin-Win32\\verifyta  E:\\program\\uppaal-4.1.19\\demo\\bridge.xml E:\\program\\uppaal-4.1.19\\demo\\query.xml";
            // 执行dos命令并获取输出结果
            Process proc = Runtime.getRuntime().exec(cmd);
            br = new BufferedReader(new InputStreamReader(proc.getInputStream(), "GBK"));
            String line;
            while ((line = br.readLine()) != null) {
                lists.add(line);
                System.out.println(line);
            }
            proc.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        String json = JSON.toJSONString(lists);
        response.setCharacterEncoding("utf-8");
        //JSONObject  jsonStr = JSONObject.parseObject(JSON.toJSONString(map));
        PrintWriter writer = response.getWriter();
        writer.print(json);


        System.out.println(json);

    }

    //将mxgraph输出的xml转化成uppal的xml形式
    public static void parseXml(String xml, String query) throws Exception {

        File file = new File("E:\\LabProject\\mxgraph\\temp.xml");
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        // 把数据写入到输出流
        out.write(xml);
        // 关闭输出流
        out.close();

        File fileQuery = new File("E:\\LabProject\\mxgraph\\query.txt");
        BufferedWriter out1 = new BufferedWriter(new FileWriter(fileQuery));
        // 把数据写入到输出流
        out1.write(query);
        // 关闭输出流
        out1.close();


        SAXReader reader = new SAXReader();
//        File file = new File("E:\\LabProject\\mxgraph\\forProperty1.xml");
        Document doc = reader.read(file);//read

        Document doc1 = DocumentHelper.createDocument();//write
        Element root1 = doc1.addElement("nta");

        List<Element> templateList = new ArrayList<>();
        List<Element> vertexList = new ArrayList<>();
        List<Element> edgeList = new ArrayList<>();
        List<Element> queryList = new ArrayList<>();

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

            if (cell.getName() == "queries") {
                queryList = cell.elements();

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

            //先写点
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

            //后写边
            while (edgeIterator.hasNext()) {
                Element edge = edgeIterator.next();
                Attribute parent = findCellAttribute(edge, "parent");
                if (parent.getValue().equals(id)) {
                    createEdge(template, edge);
                }
            }
        }

        Element queries = root1.addElement("queries");
        Iterator<Element> queryIterator = queryList.iterator();
        createQueries(queries, queryIterator);

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

    public static void createQueries(Element queries, Iterator<Element> queryIterator) {
        while (queryIterator.hasNext()) {
            Element queryEle = queryIterator.next();
            if (queryEle.getName() == "query") {
                Element query = queries.addElement("query");
                if (haschildren(queryEle)) {
                    List<Element> children = queryEle.elements();
                    Iterator<Element> child = children.iterator();
                    while (child.hasNext()) {
                        Element item = child.next();
                        if (item.getName() == "formula") {
                            Element formula = query.addElement("formula");
                            formula.addText(item.getText());
                        }
                        if (item.getName() == "comment") {
                            Element comment = query.addElement("comment");
                            comment.addText(item.getText());
                        }
                    }
                }
            }
        }
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
