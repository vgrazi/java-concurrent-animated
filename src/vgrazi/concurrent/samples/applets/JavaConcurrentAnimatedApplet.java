package vgrazi.concurrent.samples.applets;

import vgrazi.concurrent.samples.examples.ConcurrentExample;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class JavaConcurrentAnimatedApplet extends JApplet {

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
            ConcurrentExample examplePanel = (ConcurrentExample) constructor.newInstance(title, this, 0);
            this.add(examplePanel);
            examplePanel.launchExample();
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
}
