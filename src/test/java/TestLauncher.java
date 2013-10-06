import junit.framework.TestSuite;
import org.junit.Test;
import org.xml.sax.SAXException;
import vgrazi.concurrent.samples.launcher.ConcurrentExampleLauncher;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class TestLauncher extends TestSuite{

    @Test
    public void testLaunch() throws ParserConfigurationException, SAXException, IOException, InterruptedException {
        new ConcurrentExampleLauncher();
        Thread.sleep(100000);
    }
}
