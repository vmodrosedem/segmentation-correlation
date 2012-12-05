/*
 * .*nov09_01_z\d+_ch00.*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.lf1.imagej.segmentationplugin;

import static java.awt.GridBagConstraints.*;
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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * User interface for segmentation plugin
 *
 * @author Matlab
 */
public class Frame extends PlugInFrame implements ImageListener, ActionListener, ItemListener, KeyListener {

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
  Checkbox borderCheckbox1;
  Checkbox borderCheckbox2;
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
    sigma1TextField.addKeyListener(this);
    pos = new GridBagConstraints(1, 3, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(sigma1TextField, pos);
    pos = new GridBagConstraints(2, 3, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(new Label("Blur sigma:"), pos);
    sigma2TextField = new TextField(DEFAULT_SIGMA2 + "");
    sigma2TextField.addKeyListener(this);
    pos = new GridBagConstraints(3, 3, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(sigma2TextField, pos);
    //threshold
    pos = new GridBagConstraints(0, 4, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(new Label("Threshold value:"), pos);
    thresholdTextField1 = new TextField("");
    thresholdTextField1.addKeyListener(this);
    pos = new GridBagConstraints(1, 4, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(thresholdTextField1, pos);
    pos = new GridBagConstraints(2, 4, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(new Label("Threshold value:"), pos);
    thresholdTextField2 = new TextField("");
    thresholdTextField2.addKeyListener(this);
    pos = new GridBagConstraints(3, 4, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(thresholdTextField2, pos);
    //fill holes
    fillHolesCheckbox1 = new Checkbox("Fill holes", true);
    pos = new GridBagConstraints(0, 5, 2, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(fillHolesCheckbox1, pos);
    fillHolesCheckbox2 = new Checkbox("Fill holes", false);
    pos = new GridBagConstraints(2, 5, 2, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(fillHolesCheckbox2, pos);
    //border touching
    borderCheckbox1 = new Checkbox("Discard border-touching regions", true);
    pos = new GridBagConstraints(0, 6, 2, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(borderCheckbox1, pos);
    borderCheckbox2 = new Checkbox("Discard border-touching regions", false);
    pos = new GridBagConstraints(2, 6, 2, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(borderCheckbox2, pos);
    //area threshold
    pos = new GridBagConstraints(0, 7, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(new Label("Min area [px]:"), pos);
    areaThresholdTextField1 = new TextField("");
    areaThresholdTextField1.addKeyListener(this);
    pos = new GridBagConstraints(1, 7, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(areaThresholdTextField1, pos);
    pos = new GridBagConstraints(2, 7, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(new Label("Min area [px]:"), pos);
    areaThresholdTextField2 = new TextField("150");
    areaThresholdTextField2.addKeyListener(this);
    pos = new GridBagConstraints(3, 7, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(areaThresholdTextField2, pos);
    //maximum projection
    maximumProjectionCheckbox1 = new Checkbox("Use maximum projection", true);
    pos = new GridBagConstraints(0, 8, 2, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(maximumProjectionCheckbox1, pos);
    //preview image
    group = new CheckboxGroup();
    preview1 = new Checkbox("Use ch1 for preview", group, false);
    preview2 = new Checkbox("Use ch2 for preview", group, true);
    pos = new GridBagConstraints(0, 9, 2, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(preview1, pos);
    pos = new GridBagConstraints(2, 9, 2, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(preview2, pos);
    //threshold selection method
    pos = new GridBagConstraints(0, 10, 2, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(new Label("Threshold selection method:"), pos);
    thresholdMethodChoice = new Choice();
    pos = new GridBagConstraints(2, 10, REMAINDER, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
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
    pos = new GridBagConstraints(3, 11, 1, 1, 0, 0, LINE_END, NONE, insets, 0, 0);
    this.add(runButton, pos);

    addKeyListener(this);

    pack();
    GUI.center(this);
    this.setVisible(true);
  }

  private ImagePlus getFirstImage() {

    String selected = image1Choice.getSelectedItem();
    if (selected != null) {
      int id = Integer.parseInt(selected.substring(0, selected.indexOf(' ')));
      ImagePlus ret = WindowManager.getImage(id);
      if (ret != null) {
        return ret;
      }
    }
    return null;
  }

  private ImagePlus getSecondImage() {
    String selected = image2Choice.getSelectedItem();
    if (selected != null) {
      int id = Integer.parseInt(selected.substring(0, selected.indexOf(' ')));
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

  public void keyTyped(KeyEvent e) {
  }

  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
      runButton.dispatchEvent(new ActionEvent(runButton, ActionEvent.ACTION_PERFORMED, runButton.getActionCommand()));
    }
  }

  public void keyReleased(KeyEvent e) {
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
      p.border1 = borderCheckbox1.getState();
      p.border2 = borderCheckbox2.getState();
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
        if ((getFirstImage().getType() != ImagePlus.GRAY8 && getFirstImage().getType() != ImagePlus.GRAY16)
                || (getSecondImage().getType() != ImagePlus.GRAY8 && getSecondImage().getType() != ImagePlus.GRAY16)) {
          IJ.error("Requires 8bit or 16bit grayscale images.");
          return;
        }
        if (getFirstImage().getWidth() != getSecondImage().getWidth()
                || getFirstImage().getHeight() != getSecondImage().getHeight()
                || getFirstImage().getStackSize() != getSecondImage().getStackSize()) {
          IJ.error("Both images must have the same dimensions.");
          return;
        }

        UIParams params = getUIParams();
        ImagePlus mask1;
        ImagePlus mask2;
        //preview image        
        ImagePlus imageToShow;
        if (params.preview1) {
          imageToShow = Process.convertStackToRGB(getFirstImage());
        } else {
          imageToShow = Process.convertStackToRGB(getSecondImage());
        }

        //create segmentation masks
        if (params.useMaximumProjection1) {
          mask1 = Process.segmentStack(Process.maximumIntensityProjection(getFirstImage()), params.sigma1, params.threshold1, params.fillHoles1);
        } else {
          mask1 = Process.segmentStack(getFirstImage(), params.sigma1, params.threshold1, params.fillHoles1);
        }
        Process.filterRegions(mask1, params.minArea1, params.border1);
        mask2 = Process.segmentStack(getSecondImage(), params.sigma2, params.threshold2, params.fillHoles2);
        Process.filterRegions(mask2, params.minArea2, params.border2);
        
        //scattergram
        Process.andMaskStack(mask2, mask1);
        getScatterImage().setImage(Process.scatterPlot(getFirstImage(), getSecondImage(), mask2));
        getScatterImage().show();
        getScatterImage().updateAndDraw();
        IJ.showMessage("Pearsons correlation coefficient: " + Process.computeCorrelation(getFirstImage(), getSecondImage(), mask2));
        mask2.show();
        //show preview
        Process.drawOutlineStack(imageToShow, mask1, 0x00ff00); //green
        Process.drawOutlineStack(imageToShow, mask2, 0xff0000); //red
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
    boolean border1;
    boolean border2;
    boolean useMaximumProjection1;
    boolean preview1;
  }
}