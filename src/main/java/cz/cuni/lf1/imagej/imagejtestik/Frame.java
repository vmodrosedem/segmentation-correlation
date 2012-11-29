/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.lf1.imagej.imagejtestik;

import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GUI;
import ij.plugin.frame.PlugInFrame;
import ij.process.AutoThresholder;
import ij.process.ImageProcessor;
import java.awt.Button;
import java.awt.Choice;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static java.awt.GridBagConstraints.*;
import java.awt.TextField;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 *
 * @author Matlab
 */
public class Frame extends PlugInFrame implements ImageListener, ActionListener, ItemListener {

  final double DEFAULT_SIGMA1 = 5.0;
  final double DEFAULT_SIGMA2 = 5.0;
  final String DEFAULT_METHOD = "Otsu";
  Choice image1Choice;
  Choice image2Choice;
  TextField sigma1TextField;
  TextField sigma2TextField;
  Choice thresholdMethodChoice;
  TextField thresholdTextField1;
  TextField thresholdTextField2;
  Button runButton;
  ImagePlus resultImage;

  public Frame() {
    super("Plugin frame");
    this.setLayout(new GridBagLayout());


    //---input selection---
    Insets insets = new Insets(5, 5, 1, 1);
    GridBagConstraints pos = new GridBagConstraints(0, 0, REMAINDER, 1, 0, 0, LINE_START, NONE, insets, 0, 0);
    image1Choice = new Choice();
    image2Choice = new Choice();
    this.add(new Label("Choose first channel image sequence:"), pos);
    pos = new GridBagConstraints(0, 1, REMAINDER, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(image1Choice, pos);
    pos = new GridBagConstraints(0, 2, REMAINDER, 1, 0, 0, LINE_START, NONE, insets, 0, 0);
    this.add(new Label("Choose second channel image sequence:"), pos);
    pos = new GridBagConstraints(0, 3, REMAINDER, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(image2Choice, pos);
    int count = WindowManager.getImageCount();
    for (int i = 1; i <= count; i++) {
      ImagePlus ip = WindowManager.getImage(WindowManager.getNthImageID(i));
      image1Choice.insert(ip.getID() + " " + ip.getTitle(), i);
      image2Choice.insert(ip.getID() + " " + ip.getTitle(), i);
    }
    ImagePlus.addImageListener(this);

    //---segmentation parameters---
    pos = new GridBagConstraints(0, 4, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(new Label("1st channel sigma:"), pos);
    sigma1TextField = new TextField(DEFAULT_SIGMA1 + "");
    pos = new GridBagConstraints(1, 4, REMAINDER, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(sigma1TextField, pos);
    pos = new GridBagConstraints(0, 5, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(new Label("2nd channel sigma:"), pos);
    sigma2TextField = new TextField(DEFAULT_SIGMA2 + "");
    pos = new GridBagConstraints(1, 5, REMAINDER, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(sigma2TextField, pos);

    pos = new GridBagConstraints(0, 6, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(new Label("Threshold selection method:"), pos);
    thresholdMethodChoice = new Choice();
    pos = new GridBagConstraints(1, 6, REMAINDER, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(thresholdMethodChoice, pos);
    String[] methods = AutoThresholder.getMethods();
    for (int i = 0; i < methods.length; i++) {
      thresholdMethodChoice.add(methods[i]);
    }
    thresholdMethodChoice.select(DEFAULT_METHOD);
    thresholdMethodChoice.addItemListener(this);
    pos = new GridBagConstraints(0, 7, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(new Label("1st channel threshold value:"), pos);
    thresholdTextField1 = new TextField("");
    pos = new GridBagConstraints(1, 7, REMAINDER, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(thresholdTextField1, pos);
    pos = new GridBagConstraints(0, 8, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(new Label("2nd channel threshold value:"), pos);
    thresholdTextField2 = new TextField("");
    pos = new GridBagConstraints(1, 8, REMAINDER, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(thresholdTextField2, pos);



    //---button---
    runButton = new Button("Run");
    runButton.addActionListener(this);
    pos = new GridBagConstraints(0, RELATIVE, 1, 1, 0, 0, LINE_END, NONE, insets, 0, 0);
    this.add(runButton, pos);



    pack();
    GUI.center(this);
    this.setVisible(true);
  }

  private ImagePlus getFirstImage() {

    String selected = image1Choice.getSelectedItem();
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
    String selected = image2Choice.getSelectedItem();
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
    image1Choice.add(imp.getID() + " " + imp.getTitle());
    image2Choice.add(imp.getID() + " " + imp.getTitle());
  }

  public void imageClosed(ImagePlus imp) {
    image1Choice.remove(imp.getID() + " " + imp.getTitle());
    image2Choice.remove(imp.getID() + " " + imp.getTitle());
  }

  public void imageUpdated(ImagePlus imp) {
  }

  //threshold method changed
  public void itemStateChanged(ItemEvent e) {
    if (getFirstImage() != null) {
      thresholdTextField1.setText("" + Process.getOptimumStackThreshold(getFirstImage(), thresholdMethodChoice.getSelectedItem()));
    }
    if (getSecondImage() != null) {
      thresholdTextField2.setText("" + Process.getOptimumStackThreshold(getSecondImage(), thresholdMethodChoice.getSelectedItem()));
    }
  }

  public UIParams getUIParams() {
    UIParams p = new UIParams();
    try {
      if ("".equals(sigma1TextField.getText())) {
        p.sigma1 = DEFAULT_SIGMA1;
        sigma1TextField.setText(DEFAULT_SIGMA1 + "");
      } else {
        p.sigma1 = Float.parseFloat(sigma1TextField.getText());
      }
      if ("".equals(sigma2TextField.getText())) {
        p.sigma2 = DEFAULT_SIGMA2;
        sigma2TextField.setText(DEFAULT_SIGMA2 + "");
      } else {
        p.sigma2 = Float.parseFloat(sigma2TextField.getText());
      }
      if ("".equals(thresholdTextField1.getText())) {
        if (getFirstImage() != null) {
          int thr = Process.getOptimumStackThreshold(getFirstImage(), thresholdMethodChoice.getSelectedItem());
          thresholdTextField1.setText("" + thr);
          p.threshold1 = thr;
        }
      } else {
        p.threshold1 = Integer.parseInt(thresholdTextField1.getText());
      }
      if ("".equals(thresholdTextField2.getText())) {
        if (getSecondImage() != null) {
          int thr = Process.getOptimumStackThreshold(getSecondImage(), thresholdMethodChoice.getSelectedItem());
          thresholdTextField2.setText("" + thr);
          p.threshold2 = thr;
        }
      } else {
        p.threshold2 = Integer.parseInt(thresholdTextField2.getText());
      }
      return p;
    } catch (Throwable t) {
      throw new RuntimeException("Invalid parameter values", t);
    }
  }

  public void actionPerformed(ActionEvent e) {
    try{
    if ("Run".equals(e.getActionCommand())) {
      UIParams params = getUIParams();
      if (getFirstImage() == null && getSecondImage() == null) {
        return;
      }

      ImagePlus imageToShow = null;
      if (getFirstImage() != null) {
        imageToShow = Process.convertStackToRGB(getFirstImage());
        ImagePlus mask1 = Process.segmentStack(getFirstImage(), params.sigma1, params.threshold1);
        Process.drawOutlineStack(imageToShow, mask1, 0x00ff00);
      }

      if (getSecondImage() != null) {
        if (imageToShow == null) {
          imageToShow = Process.convertStackToRGB(getSecondImage());
        }
        ImagePlus mask2 = Process.segmentStack(getSecondImage(), params.sigma2, params.threshold2);
        Process.drawOutlineStack(imageToShow, mask2, 0xff0000);
      }

      getResultImage().setImage(imageToShow);
      getResultImage().show();
      getResultImage().updateAndDraw();
    }
    }catch (Throwable t){
      t.printStackTrace();
      IJ.showMessage(t.toString());
    }
  }

  public class UIParams {

    double sigma1;
    double sigma2;
    int threshold1;
    int threshold2;
  }
}
//        ImagePlus scatter = new ImagePlus("scatter", Process.scatterPlot(
//                firstImage.getStack().getProcessor(firstImage.getCurrentSlice()),
//                secondImage.getStack().getProcessor(secondImage.getCurrentSlice())));
//        scatter.show();
//        //maximum projection
//        ZProjector zProjector = new ZProjector();
//        zProjector.setImage(firstImage);
//        zProjector.setMethod(ZProjector.MAX_METHOD);
//        zProjector.setStopSlice(firstImage.getStackSize());
//        zProjector.doProjection();
//        ByteProcessor contourImage = (ByteProcessor) zProjector.getProjection().getProcessor().convertToByte(true);