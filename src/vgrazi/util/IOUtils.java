package vgrazi.util;

import vgrazi.concurrent.samples.ConcurrentExampleConstants;
import vgrazi.concurrent.samples.launcher.ConcurrentExampleLauncher;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by Victor Grazi.
 * Date: Dec 14, 2008 - 12:00:53 PM
 */
public class IOUtils {
  public static String readHtmlText(String fileName) throws IOException {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(fileName));
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
