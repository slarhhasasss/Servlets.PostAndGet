import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


// Вообще говоря, нужно создавать файл web.xml в webapps -> WEB-INF, но в "идее" можно обойтись
// аннотацией @WebServlet("/NameUrl") (И вообще, не только в идее). Например, данный сервлет будет доступен по ссылке
// http://localhost:8080/test/send
@WebServlet("/send")
public class MainServlet extends HttpServlet {


    // get запросы могут принимать значения только из тела http запроса, поэтому считывать inputStream нет смылса,
    // там все равно ничего не будет
   @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       //Берем тело запроса
       String queryStr = req.getQueryString();

       //Получаем параметры строки в виде мапы
       Map<String, String[]> hm = req.getParameterMap();
       //если нам пришло какое-то имя, то отвечаем на него!
       if (hm.containsKey("name")) {
           OutputStream os = resp.getOutputStream();
           //если есть имя, то оно одно!
           byte[] ans = ("Hello, " + hm.get("name")[0]).getBytes();
           os.flush();
           os.write(ans);
           os.close();
       } else {
          //если имени нет, то просто отправляем то, что пришло!
           StringBuilder sb = new StringBuilder("\nIt is useless for us, take it away: \n");

           //Делаем адекватную строку, чтобы отправить ее назад
           for (String curKey : hm.keySet()) {
               sb.append(curKey).append(" = ");
               for (String value : hm.get(curKey)) {
                   sb.append(value).append(", ");
               }
               //обрезаем строку - удаляем два последних символа.
               sb.delete(sb.length() - 2, sb.length() - 1);
               sb.append("\n");
           }
           //Удаляет символ на текущем месте (на последнем в данном случае)
           sb.deleteCharAt(sb.length() - 1);

           //Отправляем обратно!
           OutputStream os = resp.getOutputStream();
           byte[] ans = sb.toString().getBytes();
           os.flush();
           os.write(ans);
           os.close();
       }



    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

       //считываем данные из входящего потока. В методе POST Уже нельзя просто
        // обратиться к req и взять отуда ParameterMap.
        InputStream is = req.getInputStream();
        byte[] byteArrFromUser = is.readAllBytes();
        String inputStreamStr = new String(byteArrFromUser);
        is.close();

        //Отправляем ответ пользователю
        OutputStream os = resp.getOutputStream();
        os.write(("You made Post request! Data was send to server: " + inputStreamStr + "\n").getBytes());
        os.flush();
        //os.write(sbNames.toString().getBytes());
        //os.flush();
        os.close();
    }
}
