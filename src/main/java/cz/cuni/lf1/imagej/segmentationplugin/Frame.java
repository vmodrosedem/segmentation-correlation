/*
 * .*nov09_01_z\d+_ch00.*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.lf1.imagej.segmentationplugin;

import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GUI;
import ij.plugin.frame.PlugInFrame;
import ij.process.AutoThresholder;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Choice;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.TextField;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import static java.awt.GridBagConstraints.*;

/**
 * User interface for segmentation plugin
 * @author Matlab
 */
public class Frame extends PlugInFrame implements ImageListener, ActionListener, ItemListener {

  final double DEFAULT_SIGMA1 = 8.0;
  final double DEFAULT_SIGMA2 = 3.0;
  final String DEFAULT_METHOD = "Otsu";
  Choice image1Choice;
  Choice image2Choice;
  TextField sigma1TextField;
  TextField sigma2TextField;
  Choice thresholdMethodChoice;
  TextField thresholdTextField1;
  TextField thresholdTextField2;
  Checkbox fillHolesCheckbox1;
  Checkbox fillHolesCheckbox2;
  TextField areaThresholdTextField1;
  TextField areaThresholdTextField2;
  Checkbox maximumProjectionCheckbox1;
  CheckboxGroup group;
  Checkbox preview1;
  Checkbox preview2;
  Button runButton;
  ImagePlus resultImage;
  ImagePlus scatterPlot;

  public Frame() {
    super("Segmentation options");
    this.setLayout(new GridBagLayout());


    //data input
    Insets insets = new Insets(5, 5, 1, 1);
    GridBagConstraints pos = new GridBagConstraints(0, 0, 2, 1, 0, 0, CENTER, NONE, insets, 0, 0);
    this.add(new Label("Channel 1"), pos);
    pos = new GridBagConstraints(2, 0, 2, 1, 0, 0, CENTER, NONE, insets, 0, 0);
    this.add(new Label("Channel 2"), pos);
    image1Choice = new Choice();
    image2Choice = new Choice();
    pos = new GridBagConstraints(0, 1, 1, 1, 0, 0, LINE_START, NONE, insets, 0, 0);
    this.add(new Label("Data:"), pos);
    pos = new GridBagConstraints(1, 1, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(image1Choice, pos);
    pos = new GridBagConstraints(2, 1, 1, 1, 0, 0, LINE_START, NONE, insets, 0, 0);
    this.add(new Label("Data:"), pos);
    pos = new GridBagConstraints(3, 1, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(image2Choice, pos);
    int count = WindowManager.getImageCount();
    for (int i = 1; i <= count; i++) {
      ImagePlus ip = WindowManager.getImage(WindowManager.getNthImageID(i));
      image1Choice.insert(ip.getID() + " " + ip.getTitle(), i);
      image2Choice.insert(ip.getID() + " " + ip.getTitle(), i);
    }
    ImagePlus.addImageListener(this);

    //sigma
    pos = new GridBagConstraints(0, 3, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(new Label("Blur sigma:"), pos);
    sigma1TextField = new TextField(DEFAULT_SIGMA1 + "");
    pos = new GridBagConstraints(1, 3, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(sigma1TextField, pos);
    pos = new GridBagConstraints(2, 3, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(new Label("Blur sigma:"), pos);
    sigma2TextField = new TextField(DEFAULT_SIGMA2 + "");
    pos = new GridBagConstraints(3, 3, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(sigma2TextField, pos);
    //threshold
    pos = new GridBagConstraints(0, 4, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(new Label("Threshold value:"), pos);
    thresholdTextField1 = new TextField("");
    pos = new GridBagConstraints(1, 4, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(thresholdTextField1, pos);
    pos = new GridBagConstraints(2, 4, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(new Label("Threshold value:"), pos);
    thresholdTextField2 = new TextField("");
    pos = new GridBagConstraints(3, 4, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(thresholdTextField2, pos);
    //fill holes
    fillHolesCheckbox1 = new Checkbox("Fill holes", true);
    pos = new GridBagConstraints(0, 5, 2, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(fillHolesCheckbox1, pos);
    fillHolesCheckbox2 = new Checkbox("Fill holes", false);
    pos = new GridBagConstraints(2, 5, 2, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(fillHolesCheckbox2, pos);
    //area threshold
    pos = new GridBagConstraints(0, 6, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(new Label("Min area [px]:"), pos);
    areaThresholdTextField1 = new TextField("");
    pos = new GridBagConstraints(1, 6, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(areaThresholdTextField1, pos);
    pos = new GridBagConstraints(2, 6, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(new Label("Min area [px]:"), pos);
    areaThresholdTextField2 = new TextField("150");
    pos = new GridBagConstraints(3, 6, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(areaThresholdTextField2, pos);
    //maximum projection
    maximumProjectionCheckbox1 = new Checkbox("Use maximum projection", true);
    pos = new GridBagConstraints(0, 7, 2, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(maximumProjectionCheckbox1, pos);
    //preview image
    group = new CheckboxGroup();
    preview1 = new Checkbox("Use ch1 for preview", group, false);
    preview2 = new Checkbox("Use ch2 for preview", group, true);
    pos = new GridBagConstraints(0, 8, 2, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(preview1, pos);
    pos = new GridBagConstraints(2, 8, 2, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(preview2, pos);
    //threshold selection method
    pos = new GridBagConstraints(0, 9, 2, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(new Label("Threshold selection method:"), pos);
    thresholdMethodChoice = new Choice();
    pos = new GridBagConstraints(2, 9, REMAINDER, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(thresholdMethodChoice, pos);
    String[] methods = AutoThresholder.getMethods();
    for (int i = 0; i < methods.length; i++) {
      thresholdMethodChoice.add(methods[i]);
    }
    thresholdMethodChoice.select(DEFAULT_METHOD);
    thresholdMethodChoice.addItemListener(this);
    //---button---
    runButton = new Button("Run");
    runButton.addActionListener(this);
    pos = new GridBagConstraints(3, 10, 1, 1, 0, 0, LINE_END, NONE, insets, 0, 0);
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

  private ImagePlus getScatterImage() {
    if (scatterPlot == null) {
      scatterPlot = new ImagePlus();
      scatterPlot.setTitle("Scatter plot");
    }
    return scatterPlot;
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

      p.fillHoles1 = fillHolesCheckbox1.getState();
      p.fillHoles2 = fillHolesCheckbox2.getState();

      if ("".equals(areaThresholdTextField1.getText())) {
        p.minArea1 = 0;
      } else {
        p.minArea1 = Integer.parseInt(areaThresholdTextField1.getText());
      }
      if ("".equals(areaThresholdTextField2.getText())) {
        p.minArea2 = 0;
      } else {
        p.minArea2 = Integer.parseInt(areaThresholdTextField2.getText());
      }
      p.useMaximumProjection1 = maximumProjectionCheckbox1.getState();
      p.preview1 = preview1.getState();
      return p;
    } catch (Throwable t) {
      throw new RuntimeException("Invalid parameter values", t);
    }
  }

  public void actionPerformed(ActionEvent e) {
    try {
      if ("Run".equals(e.getActionCommand())) {
        if (getFirstImage() == null && getSecondImage() == null) {
          return;
        }

        UIParams params = getUIParams();
        ImagePlus mask1 = null;
        ImagePlus mask2 = null;
        //preview image        
        ImagePlus imageToShow = null;
        if (params.preview1) {
          imageToShow = Process.convertStackToRGB(getFirstImage());
        } else {
          imageToShow = Process.convertStackToRGB(getSecondImage());
        }

        //first image
        if (params.useMaximumProjection1) {
          mask1 = Process.segmentStack(Process.maximumIntensityProjection(getFirstImage()), params.sigma1, params.threshold1, params.fillHoles1);
        } else {
          mask1 = Process.segmentStack(getFirstImage(), params.sigma1, params.threshold1, params.fillHoles1);
        }
        if (params.minArea1 > 0) {
          Process.discardSmallRegions(mask1, params.minArea1);
        }
        Process.drawOutlineStack(imageToShow, mask1, 0x00ff00); //green
        //Process.drawOutlineStack(imageToShow, mask1, 0x0000ff); //blue
        //mask1.show();

        //second image
        mask2 = Process.segmentStack(getSecondImage(), params.sigma2, params.threshold2, params.fillHoles2);
        if (params.minArea2 > 0) {
          Process.discardSmallRegions(mask2, params.minArea2);
        }
        Process.drawOutlineStack(imageToShow, mask2, 0xff0000); //red

        //scatter
        if (mask1 != null && mask2 != null) {
          Process.andMaskStack(mask2, mask1);
          getScatterImage().setImage(Process.scatterPlot(getFirstImage(), getSecondImage(), mask2));
          getScatterImage().show();
          getScatterImage().updateAndDraw();
        }
        //show preview
        getResultImage().setImage(imageToShow);
        getResultImage().show();
        getResultImage().updateAndDraw();
      }
    } catch (Throwable t) {
      t.printStackTrace();
      IJ.showMessage(t.toString());
    }
  }

  public class UIParams {

    double sigma1;
    double sigma2;
    int threshold1;
    int threshold2;
    boolean fillHoles1;
    boolean fillHoles2;
    int minArea1;
    int minArea2;
    boolean useMaximumProjection1;
    boolean preview1;
  }
}