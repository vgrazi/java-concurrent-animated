package vgrazi.util;

import java.io.*;

/**
 * Created by Victor Grazi.
 * Date: Dec 14, 2008 - 12:00:53 PM
 */
public class IOUtils {
  public static String readHtmlText(String fileName) throws IOException {
    BufferedReader reader = null;
    try {
      final InputStream stream = IOUtils.class.getResourceAsStream(fileName);
      reader = new BufferedReader(new InputStreamReader(stream));
      StringBuffer sb = new StringBuffer();
      String line;
      while((line = reader.readLine()) != null) {
        line = line.replace("<!--.*-->", "");
        sb.append(line);
        sb.append("\r\n");
      }
      return sb.toString();
    } finally {
      if(reader != null) {
        reader.close();
      }
    }
  }
}
