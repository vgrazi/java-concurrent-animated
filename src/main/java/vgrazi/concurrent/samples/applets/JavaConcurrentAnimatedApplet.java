package vgrazi.concurrent.samples.applets;

import vgrazi.concurrent.samples.examples.ConcurrentExample;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class JavaConcurrentAnimatedApplet extends JApplet {
    ConcurrentExample examplePanel;

    @Override
    public void init() {
        super.init();
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                printBounds();
            }

            public void componentMoved(ComponentEvent componentEvent) {
                printBounds();
            }
        });
        String example = getParameter("example");
        String title = getParameter("title");
        try {
            Class aClass = Class.forName(example);
            Constructor constructor = aClass.getConstructor(String.class, Container.class, int.class);
//          System.out.println("JavaConcurrentAnimatedApplet.init Creating new example instance");
            examplePanel = (ConcurrentExample) constructor.newInstance(title, this, 0);
//          System.out.println("JavaConcurrentAnimatedApplet.init adding example");
            this.add(examplePanel);
//          System.out.println("JavaConcurrentAnimatedApplet.init launch the example");
            examplePanel.launchExample();
//          System.out.println("JavaConcurrentAnimatedApplet.init set the canvas to true");
            examplePanel.setAnimationCanvasVisible(true);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    private void printBounds() {
        Rectangle bounds = getBounds();
        System.out.printf("new bounds: %d, %d, %d, %d%n", (int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight());
    }

    @Override
    public void stop() {
        super.stop();
        examplePanel.pauseAnimationClock();
        shutdown();
    }

    @Override
    public void destroy() {
        super.destroy();
        shutdown();
    }

    private void shutdown() {
        if(examplePanel != null) {
          try {
            examplePanel.reset();
            examplePanel.pauseAnimationClock();
          } catch (Exception e) {
            e.printStackTrace();
          }
          examplePanel = null;
        }
    }
}
