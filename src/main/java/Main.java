import netscape.javascript.JSObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class Main {

    //localhost url
    public static String BASE_URL = "http://localhost:8080/test/send";

    public static void main(String[] argc) throws IOException {

        Scanner in = new Scanner(System.in);
        System.out.print("Введите команду: ");
        String command = in.next();
        while (!command.equals("exit")) {
            switch (command) {
                case "doGet":
                    doGetRequest();
                    break;
                case "doPost":
                    doPostRequest();
                    break;
                default:
                    System.out.println("No such command!");
            }
            System.out.println("-------------------------------------------.!.---------------------------------------");
            System.out.print("Введите команду: ");
            command = in.next();
        }

    }

    //в таких запросах мы хотим отправить тайную информацию. Поэтому параметры запроса не будут
    //видны в http запросе. Проверим это на сервере
    private static void doPostRequest() throws IOException {
        URL url = new URL(BASE_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.connect();

        //data for sending to server:
        HashMap<String, String[]> data = new HashMap<>();
        data.put("name", new String[]{"Dima"});
        data.put("age", new String[]{"18"});
        data.put("friends", new String[]{"Alina", "Valera"});


        //можно попытаться как-то поумному передать в виле json файла, но в java нет встроенных
        //классов для работы с json, а импортировать мне лень, поэтому опять в виде строки
        String strData = getStringForQuery(data);
        System.out.println(strData);

        //Отправляем байтовый массив на сервер
        OutputStream os = connection.getOutputStream();
        os.flush();
        os.write(strData.getBytes());
        os.close();


        InputStream is = connection.getInputStream();
        byte[] bArrFromServer = is.readAllBytes();
        String strFromServer = new String(bArrFromServer);
        is.close();

        int respCode = connection.getResponseCode();


        connection.disconnect();

        System.out.println("Data was sent! Response code: "
                + respCode);
        System.out.println("Text from server: " + strFromServer + "\n");
    }


    //В гет запросах сервлеты считывают только данные из тела запроса, то есть из url.
    //если попытаемся отравить что-то через оутпут стрим, то в итоге не дойдет ничего.
    private static void doGetRequest() throws IOException {
        //вставляем наши данные в мапу, тсобы было удобнее преобразовывать данные для командной строки
        //можно вставлять любые строковые данные и любые строковые ключи, из них потом запилится строка запроса
        HashMap<String, String[]> hm = new HashMap<>();
        hm.put("name", new String[]{"dima kol", "Alina"});
        hm.put("text", new String[]{"hello world!"});

        //make request body for insert into http request
        String strForQuery = getStringForQuery(hm);

        //выводим наш получившийся запрос для проверочки
        System.out.println("queryString = " + strForQuery);            //?name=dima+kol&name=Alina&text=hello+world!

        try {
            URL url = new URL(BASE_URL + strForQuery);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.connect();

            //сразу считывам, что нам отправил сервер. можно через более изящные методы, кнч,
            //например InputStreamReader
            InputStream is = connection.getInputStream();
            byte[] bArrFromServer = is.readAllBytes();
            String strFromServer = new String(bArrFromServer); //Hello, dima kol
            is.close();

            //узнаем респонскод
            int respCode = connection.getResponseCode();     //200

            connection.disconnect();

            //если респонкод == 200, то выписываем все на экран, что нам сервер ответил
            if (respCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Data was sent! Response code: "
                        + respCode);
                System.out.println("Text from server: " + strFromServer);
            } else {
                System.out.println("Error! Response code: " + respCode);
            }
        } catch (Throwable th) {
            System.out.println("Error! " + th.getMessage());
        }

    }

    //Эта функция превращает словарь в http запрос и возвращает переделанную для запроса строку,
    // чтобы его прибавить к url и отпрвить на сервер. Эту строку нужно просто добавить к url.
    private static String getStringForQuery(HashMap<String, String[]> hashMap) {
        StringBuilder sb = new StringBuilder("?");
        Set<String> keySet = hashMap.keySet();
        for (String key : keySet) {
            int j = 0;
            while (j < hashMap.get(key).length) {
                String curVal = hashMap.get(key)[j];
                int i = 0;
                while (i < curVal.length()) {
                    if (curVal.charAt(i) == ' ') {
                        curVal = curVal.replace(' ', '+');
                    }
                    i++;
                }
                sb.append(key).append("=").append(curVal).append("&");
                j++;
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

}
