package org._2333.cyls;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class WebUtil {

    /**
     * @param httpUrl :请求接口
     * @return 返回结果
     */
    public static String request(String httpUrl, Map<String, String> param,String mode) {
        BufferedReader reader;
        String result = null;
        StringBuilder sbf = new StringBuilder();

        try {
            URL url = new URL(httpUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(mode);
            if(param!=null){
                for(String key:param.keySet()){
                    connection.addRequestProperty(key,param.get(key));
                }
            }
            connection.connect();
            InputStream is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strRead;
            while ((strRead = reader.readLine()) != null) {
                sbf.append(strRead);
                sbf.append("\r\n");
            }
            reader.close();
            result = sbf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
