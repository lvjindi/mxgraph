import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;

public class EchoServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        // request.setCharacterEncoding("UTF-8");
        //response.setCharacterEncoding("UTF-8");
        try {
            String xml = URLDecoder.decode(request.getParameter("xml"), "UTF-8").replace("\n", "&#xa;");
            String filename = URLDecoder.decode(request.getParameter("filename"), "UTF-8").replace("\n", "&#xa;");
            File file = new File(System.getProperty("user.dir") + "\\" + filename);
            try {
                FileWriter fw = new FileWriter(file);
                if (file.exists()) {

                    fw.write(xml);
                    fw.flush();
                    fw.close();
                    System.out.print("写入文件成功");
                } else {
                    file.createNewFile();
                    fw.write(xml);
                    fw.flush();
                    fw.close();
                    System.out.print("写入文件成功");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.print(System.getProperty("user.dir"));
            System.out.print("save");
            System.out.print(filename);
            System.out.print(xml);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
