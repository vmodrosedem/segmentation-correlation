/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.lf1.imagej.imagejtestik;

import ij.ImageListener;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GUI;
import ij.plugin.ZProjector;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.frame.PlugInFrame;
import ij.process.AutoThresholder;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;

/**
 *
 * @author Matlab
 */
public class Frame extends PlugInFrame implements ImageListener, ActionListener {

  Panel imageChoicePanel;
  Choice combobox;
  Choice combobox2;
  Panel buttonPanel;
  Button runButton;
  ImagePlus resultImage;

  public Frame() {
    super("Plugin frame");

    this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    this.add(Box.createRigidArea(new Dimension(10, 10)));

    imageChoicePanel = new Panel();
    imageChoicePanel.setLayout(new BoxLayout(imageChoicePanel, BoxLayout.PAGE_AXIS));
    combobox = new Choice();
    combobox2 = new Choice();
    imageChoicePanel.add(new Label("Choose first channel image sequence:"));
    imageChoicePanel.add(combobox);
    imageChoicePanel.add(new Label("Choose second channel image sequence:"));
    imageChoicePanel.add(combobox2);
    imageChoicePanel.add(Box.createGlue());
    this.add(imageChoicePanel);

    this.add(Box.createRigidArea(new Dimension(10, 10)));

    buttonPanel = new Panel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
    runButton = new Button("Run");
    runButton.addActionListener(this);
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(runButton);
    this.add(buttonPanel);

    this.add(Box.createRigidArea(new Dimension(10, 10)));



    int count = WindowManager.getImageCount();
    for (int i = 1; i <= count; i++) {
      ImagePlus ip = WindowManager.getImage(WindowManager.getNthImageID(i));
      combobox.insert(ip.getID()+ " " + ip.getTitle(), i);
      combobox2.insert(ip.getID()+ " " + ip.getTitle(), i);
    }
    ImagePlus.addImageListener(this);
    pack();
    GUI.center(this);
    this.setVisible(true);
  }

  private ImagePlus getFirstImage() {

    String selected = combobox.getSelectedItem();
    int id = Integer.parseInt(selected.substring(0, selected.indexOf(' ')));
    if (selected != null) {
      ImagePlus ret = WindowManager.getImage(id);
      if (ret != null) {
        return ret;
      }
    }
    return null;
  }

  private ImagePlus getSecondImage() {
    String selected = combobox2.getSelectedItem();
    int id = Integer.parseInt(selected.substring(0, selected.indexOf(' ')));
    if (selected != null) {
      ImagePlus ret = WindowManager.getImage(id);
      if (ret != null) {
        return ret;
      }
    }
    return null;
  }

  private ImagePlus getResultImage() {
    if (resultImage == null) {
      resultImage = new ImagePlus();
      resultImage.setTitle("Result");
    }
    return resultImage;
  }

  public void imageOpened(ImagePlus imp) {
    combobox.add(imp.getID() + " " + imp.getTitle());
    combobox2.add(imp.getID() + " " + imp.getTitle());
  }

  public void imageClosed(ImagePlus imp) {
    combobox.remove(imp.getID() + " " + imp.getTitle());
    combobox2.remove(imp.getID() + " " + imp.getTitle());
  }

  public void imageUpdated(ImagePlus imp) {
  }

  public void actionPerformed(ActionEvent e) {
    if ("Run".equals(e.getActionCommand())) {
      ImagePlus firstImage = getFirstImage();
      ImagePlus secondImage = getSecondImage();
      if (firstImage != null && secondImage != null) {
        //max projection
        ZProjector zProjector = new ZProjector();
        zProjector.setImage(firstImage);
        zProjector.setMethod(ZProjector.MAX_METHOD);
        zProjector.setStopSlice(firstImage.getStackSize());
        zProjector.doProjection();
        ImagePlus contourImage = zProjector.getProjection();

        //convert to 8-bit
        contourImage.setProcessor(contourImage.getProcessor().convertToByte(true));

        //blur
        GaussianBlur gaussPlugin = new GaussianBlur();
        double sigma = 10;
        gaussPlugin.blurGaussian(contourImage.getProcessor(), sigma, sigma, 1e-5);


        //threshold
        AutoThresholder thresholder = new AutoThresholder();
        int thresholdValue = thresholder.getThreshold(AutoThresholder.Method.Otsu, contourImage.getProcessor().getHistogram());
        contourImage.getProcessor().threshold(thresholdValue);
        //contourImage.show();

        //outline
        ((ByteProcessor) contourImage.getProcessor()).outline();

        //---second image---
        ByteProcessor ip2 = (ByteProcessor) secondImage.getStack().getProcessor(secondImage.getCurrentSlice()).convertToByte(true);
        gaussPlugin.blurGaussian(ip2, 5, 5, 1e-5);
        ip2.threshold(thresholder.getThreshold(AutoThresholder.Method.Otsu, ip2.getHistogram()));
        ip2.outline();
        
        
        ImageProcessor imageToShow = secondImage.getProcessor().convertToRGB();
        int[] pixels = (int[]) imageToShow.getPixels();
        byte[] contourPixels = (byte[]) contourImage.getProcessor().getPixels();
        byte[] contourPixels2 = (byte[]) ip2.getPixels();
        for (int i = 0; i < pixels.length; i++) {
          if (contourPixels[i] == 0) {
            pixels[i] |= 0x00ff00;//green
          }
          if(contourPixels2[i] == 0){
            pixels[i] |= 0xff0000;//red
          }
        }

        getResultImage().setImage(new ImagePlus("Result", imageToShow));
        getResultImage().show();
        getResultImage().updateAndDraw();


//        ImageProcessor firstImageProcessor = firstImage.getStack().getProcessor(firstImage.getCurrentSlice());
//        ImageProcessor resProcessor = firstImageProcessor.duplicate();
//        ImagePlus resultImage = new ImagePlus("res", resProcessor);
//        resultImage.show();
      }


    }
  }
}
