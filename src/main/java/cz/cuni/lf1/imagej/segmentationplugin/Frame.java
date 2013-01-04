package cz.cuni.lf1.imagej.segmentationplugin;

import static java.awt.GridBagConstraints.*;
import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GUI;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.plugin.LutLoader;
import ij.plugin.frame.PlugInFrame;
import ij.process.AutoThresholder;
import ij.process.ByteProcessor;
import ij.text.TextPanel;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.TextField;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.Box;
import javax.swing.BoxLayout;

/**
 * User interface for segmentation plugin
 *
 * .*nov09_01_z\d+_ch00.*
 *
 * @author Josef Borkovec
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
  Checkbox preview3;
  Button runButton;
  Button saveButton;
  ImagePlus resultImage;
  ImagePlus scatterPlot;
  TextPanel textOutput;
  UIParams oldParams = null;
  ImagePlus[] cellMasks;
  ImagePlus mask1;
  ImagePlus maskMax;
  ImagePlus mask2;
  ImagePlus blured1max;
  ImagePlus blured1;
  ImagePlus blured2;

  public Frame() {
    super("Segmentation options");
    this.setLayout(new GridBagLayout());

    //data input
    Insets insets = new Insets(5, 5, 1, 1);
    GridBagConstraints pos = new GridBagConstraints(0, 0, 3, 1, 0, 0, CENTER, NONE, insets, 0, 0);
    this.add(new Label("Channel 1"), pos);
    pos = new GridBagConstraints(3, 0, 3, 1, 0, 0, CENTER, NONE, insets, 0, 0);
    this.add(new Label("Channel 2"), pos);
    image1Choice = new Choice();
    image2Choice = new Choice();
    image1Choice.addItemListener(this);
    image2Choice.addItemListener(this);
    pos = new GridBagConstraints(0, 1, 1, 1, 0, 0, LINE_START, NONE, insets, 0, 0);
    this.add(new Label("Data:"), pos);
    pos = new GridBagConstraints(1, 1, 2, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(image1Choice, pos);
    pos = new GridBagConstraints(3, 1, 1, 1, 0, 0, LINE_START, NONE, insets, 0, 0);
    this.add(new Label("Data:"), pos);
    pos = new GridBagConstraints(4, 1, 2, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(image2Choice, pos);
    pack();
    Dimension size = image1Choice.getPreferredSize();
    size.width = 100;
    image1Choice.setPreferredSize(size);
    image2Choice.setPreferredSize(size);
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
    pos = new GridBagConstraints(2, 3, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(sigma1TextField, pos);
    pos = new GridBagConstraints(3, 3, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(new Label("Blur sigma:"), pos);
    sigma2TextField = new TextField(DEFAULT_SIGMA2 + "");
    sigma2TextField.addKeyListener(this);
    pos = new GridBagConstraints(5, 3, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(sigma2TextField, pos);

    //threshold
    pos = new GridBagConstraints(0, 4, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(new Label("Threshold value:"), pos);
    thresholdTextField1 = new TextField("");
    thresholdTextField1.addKeyListener(this);
    pos = new GridBagConstraints(2, 4, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(thresholdTextField1, pos);
    pos = new GridBagConstraints(3, 4, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(new Label("Threshold value:"), pos);
    thresholdTextField2 = new TextField("");
    thresholdTextField2.addKeyListener(this);
    pos = new GridBagConstraints(5, 4, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(thresholdTextField2, pos);

    //fill holes
    fillHolesCheckbox1 = new Checkbox("Fill holes", true);
    pos = new GridBagConstraints(0, 5, 2, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(fillHolesCheckbox1, pos);
    fillHolesCheckbox2 = new Checkbox("Fill holes", false);
    pos = new GridBagConstraints(3, 5, 2, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(fillHolesCheckbox2, pos);

    //border touching
    borderCheckbox1 = new Checkbox("Discard border-touching regions", true);
    pos = new GridBagConstraints(0, 6, 2, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(borderCheckbox1, pos);
    borderCheckbox2 = new Checkbox("Discard border-touching regions", false);
    pos = new GridBagConstraints(3, 6, 2, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(borderCheckbox2, pos);

    //area threshold
    pos = new GridBagConstraints(0, 7, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(new Label("Min area [px]:"), pos);
    areaThresholdTextField1 = new TextField("10000");
    areaThresholdTextField1.addKeyListener(this);
    pos = new GridBagConstraints(2, 7, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(areaThresholdTextField1, pos);
    pos = new GridBagConstraints(3, 7, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(new Label("Min area [px]:"), pos);
    areaThresholdTextField2 = new TextField("300");
    areaThresholdTextField2.addKeyListener(this);
    pos = new GridBagConstraints(5, 7, 1, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(areaThresholdTextField2, pos);

    //maximum projection
    maximumProjectionCheckbox1 = new Checkbox("Use maximum projection", true);
    pos = new GridBagConstraints(0, 8, 2, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(maximumProjectionCheckbox1, pos);

    //preview image
    group = new CheckboxGroup();
    Panel chbPanel = new Panel();
    chbPanel.setLayout(new BoxLayout(chbPanel, BoxLayout.LINE_AXIS));
    preview1 = new Checkbox("Use ch1 for preview", group, false);
    preview2 = new Checkbox("Use ch2 for preview", group, true);
    preview3 = new Checkbox("Overlay", group, false);
    chbPanel.add(preview1);
    chbPanel.add(Box.createHorizontalGlue());
    chbPanel.add(preview2);
    chbPanel.add(Box.createHorizontalGlue());
    chbPanel.add(preview3);
    pos = new GridBagConstraints(0, 9, 6, 1, 0, 0, LINE_START, HORIZONTAL, insets, 0, 0);
    this.add(chbPanel, pos);

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
    thresholdMethodChoice.add("K-Means");
    thresholdMethodChoice.select(DEFAULT_METHOD);
    thresholdMethodChoice.addItemListener(this);

    //button
    runButton = new Button("Run");
    runButton.addActionListener(this);
    pos = new GridBagConstraints(5, 11, 1, 1, 0, 0, LINE_END, NONE, insets, 0, 0);
    this.add(runButton, pos);

    //save button
    saveButton = new Button("Save");
    saveButton.addActionListener(this);
    pos = new GridBagConstraints(0, 11, 1, 1, 0, 0, LINE_START, NONE, insets, 0, 0);
    this.add(saveButton, pos);

    //correlation coefficient table
    textOutput = new TextPanel("correlation coefficient");
    textOutput.setColumnHeadings("Cell #\tPCC\tSRC\trandomized PCC");
    Dimension dim = textOutput.getMinimumSize();
    dim.height = 150;
    textOutput.setPreferredSize(dim);
    pos = new GridBagConstraints(0, 12, 6, 4, 0, 1, CENTER, BOTH, insets, 0, 0);
    this.add(textOutput, pos);

    addKeyListener(this);
    pack();
    GUI.center(this);
    this.setVisible(true);
  }

  /**
   * Returns Image selected as first channel, null if no channel is selected
   */
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

  /**
   * Returns image selected as second channel, null if no channel is selected
   */
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

  /**
   * Returns image used as preview, creates an empty one if there isnt any.
   */
  private ImagePlus getResultImage() {
    if (resultImage == null) {
      resultImage = new ImagePlus();
      resultImage.setTitle("Result");
    }
    return resultImage;
  }

  /**
   * Returns image used as scattergram, creates an empty one if there isnt any.
   */
  private ImagePlus getScatterImage() {
    if (scatterPlot == null) {
      scatterPlot = new ImagePlus();
      scatterPlot.setTitle("Scattergram");
    }
    return scatterPlot;
  }

  /**
   * Run button action. Performs all the work.
   */
  public void actionPerformed(ActionEvent e) {
//    java.util.Hashtable t = Menus.getCommands();
//    IJ.showMessage(t.get("Image Sequence...").toString());
    try {
      //error checking
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

        //preview image        
        ImagePlus imageToShow = null;
        switch (params.preview) {
          case 1:
            imageToShow = Process.convertStackToRGB(getFirstImage());
            break;
          case 2:
            imageToShow = Process.convertStackToRGB(getSecondImage());
            break;
          case 3:
            imageToShow = Process.convertToRGBOverlay(getFirstImage(), getSecondImage());
            break;
          default:
            throw new RuntimeException("Unknown preview image");
        }
        //create segmentation masks
        if (params.ch1changed(oldParams)) {
          IJ.showStatus("segmentation channel 1");
          if (params.sigma1changed(oldParams)) {
            ImagePlus maxProjection = Process.maximumIntensityProjection(getFirstImage());
            blured1max = Process.toFloat(maxProjection);
            IJ.run(blured1max, "Gaussian Blur...", "sigma=" + params.sigma1 + " stack");
          }

          maskMax = Process.segmentStack(blured1max, params.sigma1, params.threshold1, params.fillHoles1);
          Process.filterRegions(maskMax, params.minArea1, params.border1);
          if (!params.useMaximumProjection1) {
            if (params.sigma1changed(oldParams) || params.projectionChanged(oldParams)) {
              blured1 = Process.toFloat(getFirstImage());
              IJ.run(blured1, "Gaussian Blur...", "sigma=" + params.sigma1 + " stack");
            }
            mask1 = Process.segmentStack(blured1, params.sigma1, params.threshold1, params.fillHoles1);
            Process.filterRegions(mask1, params.minArea1, params.border1);
          } else {
            mask1 = maskMax;
          }

          //split cells
          IJ.showStatus("splitting cell regions");
          cellMasks = Process.splitCellRegions(maskMax);
        }
        if (params.ch2changed(oldParams)) {
          IJ.showStatus("segmentation channel 2");
          if (params.sigma2changed(oldParams)) {
            blured2 = Process.toFloat(getSecondImage());
            IJ.run(blured2, "Gaussian Blur...", "sigma=" + params.sigma2 + " stack");
          }
          mask2 = Process.segmentStack(blured2, params.sigma2, params.threshold2, params.fillHoles2);
          Process.filterRegions(mask2, params.minArea2, params.border2);
        }
        textOutput.clear();
        //scattergram stack and correlation calculation
        ImageStack scatterStack = new ImageStack(256, 256);
        //single cells
        for (int i = 0; i < cellMasks.length; i++) {
          IJ.showStatus("processing cell " + (i + 1));
          ImagePlus maskForProcessing;
          if (params.useMaximumProjection1) {
            maskForProcessing = mask2.duplicate();
            Process.andMaskStack(maskForProcessing, cellMasks[i]);
          } else {
            maskForProcessing = mask2.duplicate();
            Process.andMaskStack(maskForProcessing, mask1);
            Process.andMaskStack(maskForProcessing, cellMasks[i]);
          }

          scatterStack.addSlice("Cell " + (i + 1), Process.scattergram(getFirstImage(), getSecondImage(), maskForProcessing).getProcessor());
          double pcc = Process.computeCorrelation(getFirstImage(), getSecondImage(), maskForProcessing);
          double randomPcc = Process.computeRandomCorrelation(getFirstImage(), getSecondImage(), maskForProcessing);
          double src = Process.computeSRC(getFirstImage(), getSecondImage(), maskForProcessing);
          textOutput.appendWithoutUpdate(String.format("%d\t%.4f\t%.4f\t%.4f", (i + 1), pcc, src, randomPcc));
        }
        //all cells
        ImagePlus andedMask = mask2.duplicate();
        Process.andMaskStack(andedMask, mask1);
        scatterStack.addSlice("all cells", Process.scattergram(getFirstImage(), getSecondImage(), andedMask).getProcessor());
        double pcc = Process.computeCorrelation(getFirstImage(), getSecondImage(), andedMask);
        double randomPcc = Process.computeRandomCorrelation(getFirstImage(), getSecondImage(), andedMask);
        double src = Process.computeSRC(getFirstImage(), getSecondImage(), andedMask);
        textOutput.appendLine(String.format("all\t%.4f\t%.4f\t%.4f", pcc, src, randomPcc));
        //roi
        Roi roi = selectROI();
        if (roi != null) {
          ImagePlus roiMask = new ImagePlus("", new ByteProcessor(getFirstImage().getWidth(), getFirstImage().getHeight()));
          roiMask.getProcessor().setColor(255);
          roiMask.getProcessor().fill(roi);
          pcc = Process.computeCorrelation(getFirstImage(), getSecondImage(), roiMask);
          randomPcc = Process.computeRandomCorrelation(getFirstImage(), getSecondImage(), roiMask);
          src = Process.computeSRC(getFirstImage(), getSecondImage(), roiMask);
          textOutput.appendLine(String.format("ROI\t%.4f\t%.4f\t%.4f", pcc, src, randomPcc));

          scatterStack.addSlice("ROI", Process.scattergram(getFirstImage(), getSecondImage(), roiMask).getProcessor());
        }

        //show scattergram image
        getScatterImage().setStack(scatterStack);
        Overlay o = new Overlay();
        Roi textRoi = new TextRoi(4, 0, "ch2");
        textRoi.setStrokeColor(Color.YELLOW);
        o.add(textRoi);
        textRoi = new TextRoi(244, 244, "ch1");
        textRoi.setStrokeColor(Color.YELLOW);
        o.add(textRoi);
        getScatterImage().setOverlay(o);
        getScatterImage().show();
        getScatterImage().updateAndDraw();

        //show preview
        IJ.showStatus("generating preview image");
        Process.drawOutlineStack(imageToShow, mask1, 0x00ff00); //green
        Process.drawOutlineStack(imageToShow, andedMask, 0xff0000); //red
        getResultImage().setStack(imageToShow.getStack());
        getResultImage().show();
        Process.drawCellNumbers(getResultImage(), cellMasks);
        getResultImage().updateAndDraw();

        //change lookup table of scattergram to red hot
        WindowManager.setTempCurrentImage(getScatterImage());
        LutLoader l = new LutLoader();
        l.run(IJ.getDirectory("luts") + "Red Hot" + ".lut");
        oldParams = params;
      } else if ("Save".equals(e.getActionCommand())) {
        if (oldParams != null) {
          FileDialog fd = new FileDialog(this, "Where to save", FileDialog.SAVE);
          if(getFirstImage() != null && getFirstImage().getOriginalFileInfo() != null){
            fd.setDirectory(getFirstImage().getOriginalFileInfo().directory);
          }
          fd.setFile("correlation.txt");
          fd.setVisible(true);

          if (fd.getDirectory() != null & fd.getFile() != null) {
            String filePath = fd.getDirectory() + fd.getFile();
            Process.saveResults(filePath, oldParams, textOutput.getText());
          }
        }
      }
    } catch (Throwable t) {
      StringWriter s = new StringWriter();
      t.printStackTrace(new PrintWriter(s));
      IJ.log(s.toString());
    }
  }

  /**
   * New image opened in ImageJ. Add it to input data selection Choice.
   */
  public void imageOpened(ImagePlus imp) {
    image1Choice.add(imp.getID() + " " + imp.getTitle());
    image2Choice.add(imp.getID() + " " + imp.getTitle());
  }

  /**
   * ImageJ image closed remove it from input data selection Choice, if it was
   * there.
   */
  public void imageClosed(ImagePlus imp) {
    try {
      image1Choice.remove(imp.getID() + " " + imp.getTitle());
      image2Choice.remove(imp.getID() + " " + imp.getTitle());
    } catch (IllegalArgumentException ex) {//
    }
  }

  public void imageUpdated(ImagePlus imp) {
//    if(imp == getFirstImage() || imp == getSecondImage()){
//      ImageCache.getInstance().invalidate(imp);
//    }
  }

  /**
   * Threshold method or input data changed. Recalculate thresholds.
   */
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

  /**
   * KeyListener. Fires the run button on enter key.
   */
  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
      runButton.dispatchEvent(new ActionEvent(runButton, ActionEvent.ACTION_PERFORMED, runButton.getActionCommand()));
    }
  }

  public void keyReleased(KeyEvent e) {
  }

  /**
   * Selects ROI from one of the three relevant images: first channel, second
   * channel, or preview image. Also sets the same selected ROI in all the
   * relevant images. The priority is as follows: current image > result image >
   * first channel > second channel. Current channel is used only if its one of
   * the relevant images.
   *
   * @return the ROI
   */
  private Roi selectROI() {
    ImagePlus first = getFirstImage();
    ImagePlus second = getSecondImage();
    ImagePlus result = getResultImage();
    ImagePlus current = IJ.getImage();

    Roi activeRoi;
    if ((current == first || current == second || current == result) && current.getRoi() != null) {
      activeRoi = current.getRoi();
    } else if (result.getRoi() != null) {
      activeRoi = result.getRoi();
    } else if (first.getRoi() != null) {
      activeRoi = first.getRoi();
    } else if (second.getRoi() != null) {
      activeRoi = second.getRoi();
    } else {
      activeRoi = null;
    }
    //set the same roi in all images
    first.setRoi(activeRoi);
    second.setRoi(activeRoi);
    result.setRoi(activeRoi);
    return activeRoi;
  }

  /**
   * Extract parameters from user Interface
   *
   * @return uiparams
   */
  public UIParams getUIParams() {
    UIParams p = new UIParams();
    try {
      p.channel1 = getFirstImage();
      p.channel2 = getSecondImage();
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
      if (preview1.getState()) {
        p.preview = 1;
      } else if (preview2.getState()) {
        p.preview = 2;
      } else {
        p.preview = 3;
      }
      p.border1 = borderCheckbox1.getState();
      p.border2 = borderCheckbox2.getState();
      return p;
    } catch (Throwable t) {
      throw new RuntimeException("Invalid parameter values", t);
    }
  }

  /**
   * class for holding parameters selected in user interface
   */
  public class UIParams {

    ImagePlus channel1;
    ImagePlus channel2;
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
    int preview;

    public boolean ch1changed(UIParams old) {
      if (!sigma1changed(old) && threshold1 == old.threshold1 && fillHoles1 == old.fillHoles1 && minArea1 == old.minArea1 && border1 == old.border1 && useMaximumProjection1 == old.useMaximumProjection1) {
        return false;
      }
      return true;
    }

    public boolean ch2changed(UIParams old) {
      if (!sigma2changed(old) && threshold2 == old.threshold2 && fillHoles2 == old.fillHoles2 && minArea2 == old.minArea2 && border2 == old.border2) {
        return false;
      }
      return true;
    }

    public boolean sigma1changed(UIParams old) {
      if (old != null && old.channel1 == channel1 && Math.abs(old.sigma1 - sigma1) < 1e-5) {
        return false;
      }
      return true;

    }

    public boolean sigma2changed(UIParams old) {
      if (old != null && old.channel2 == channel2 && Math.abs(old.sigma2 - sigma2) < 1e-5) {
        return false;
      } else {
        return true;
      }
    }

    public boolean projectionChanged(UIParams old) {
      if (old != null && useMaximumProjection1 == old.useMaximumProjection1) {
        return false;
      }
      return true;
    }
  }
}