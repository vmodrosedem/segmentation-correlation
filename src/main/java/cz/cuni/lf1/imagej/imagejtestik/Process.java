/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.lf1.imagej.imagejtestik;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.filter.GaussianBlur;
import ij.process.AutoThresholder;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

/**
 *
 * @author Matlab
 */
public class Process {

  public static ImageProcessor scatterPlot(ImageProcessor red, ImageProcessor green) {

    red = red.convertToByte(true);
    green = green.convertToByte(true);
    if (red.getWidth() != green.getWidth() || red.getHeight() != green.getHeight()) {

      throw new RuntimeException("inconsistent dimensions");
    }

    ImageProcessor ip = new ShortProcessor(256, 256);
    for (int i = 0; i < red.getWidth(); i++) {
      for (int j = 0; j < red.getHeight(); j++) {
        int redIntensity = red.getPixel(i, j);
        int greenIntensity = green.getPixel(i, j);
        int currentValue = ip.getPixel(redIntensity, greenIntensity);
        ip.putPixel(redIntensity, greenIntensity, currentValue + 1);
      }
    }
    return ip;
  }

  public static ImagePlus convertStackToRGB(ImagePlus imp) {
    if (imp.getStackSize() == 1) {
      return new ImagePlus("", imp.getProcessor().convertToRGB());
    } else {
      ImageStack stack = new ImageStack(imp.getWidth(), imp.getHeight(), imp.getStackSize());
      for (int i = 1; i <= imp.getStackSize(); i++) {
        stack.setPixels(imp.getStack().getProcessor(i).convertToRGB().getPixels(), i);
        stack.setSliceLabel("" + i, i);
      }
      return new ImagePlus("", stack);
    }
  }

  public static ImagePlus segmentStack(ImagePlus image, double sigma, int threshold) {
    boolean singleImage = image.getStackSize() == 1;
    ImageStack resultStack = new ImageStack(image.getWidth(), image.getHeight(), image.getStackSize());

    GaussianBlur gaussPlugin = new GaussianBlur();
    for (int i = 1; i <= image.getStackSize(); i++) {
      ImageProcessor ip = (singleImage)?
              (image.getProcessor().convertToByte(true)):
              (image.getStack().getProcessor(i).convertToByte(true));
      gaussPlugin.blurGaussian(ip, sigma, sigma, 1e-5);
      ip.threshold(threshold);
      
      resultStack.setPixels(ip.getPixels(),i);
      resultStack.setSliceLabel(""+i, i);
    }
    
    return new ImagePlus("mask", resultStack);
  }

  /**
   * finds threshold value using specified method
   */
  public static int getOptimumStackThreshold(ImagePlus image, String method) {
    //compute histogram
    int[] histogramSum;
    if (image.getStackSize() == 1) {
      //single image
      histogramSum = image.getProcessor().convertToByte(true).getHistogram();
    } else {
      //stack
      ImageStack stack = image.getStack();
      histogramSum = new int[256];
      for (int i = 0; i < histogramSum.length; i++) {
        histogramSum[i] = 0;
      }
      //sum histogram values
      for (int i = 1; i <= stack.getSize(); i++) {
        int[] histogram = stack.getProcessor(i).convertToByte(true).getHistogram();
        for (int j = 0; j < histogramSum.length; j++) {
          histogramSum[j] += histogram[j];
        }
      }
    }
    //find threshold
    AutoThresholder thresholder = new AutoThresholder();
    return thresholder.getThreshold(method, histogramSum);

  }

  public static void drawOutlineStack(ImagePlus image, ImagePlus mask, int color) {
    if (image.getType() != ImagePlus.COLOR_RGB) {
      throw new RuntimeException("image must be of COLOR_RGB type");
    }
    if (mask.getStackSize() != 1 && mask.getStackSize() != image.getStackSize()) {
      throw new RuntimeException("mask stack size must be the same as that of image or 1");
    }
    ImageStack imageStack = image.getStack();
    ImageStack maskStack = null;
    ByteProcessor commonMask = null;
    boolean oneImageMask = false;
    if (mask.getStackSize() == 1) {
      oneImageMask = true;
      commonMask = (ByteProcessor) mask.getProcessor().duplicate();
      commonMask.outline();
    } else {
      maskStack = mask.getStack();
    }

    for (int i = 1; i <= imageStack.getSize(); i++) {
      int[] imagePixels = (int[]) imageStack.getProcessor(i).getPixels();
      byte[] maskPixels;
      if(oneImageMask){
        maskPixels = (byte[]) commonMask.getPixels();
      }else{
        ByteProcessor mp = (ByteProcessor) maskStack.getProcessor(i);
        mp.outline();
        maskPixels = (byte[]) mp.getPixels();
      }
      
      for (int j = 0; j < imagePixels.length; j++) {
        if (maskPixels[j] == 0) {
          imagePixels[j] |= color;
        }
      }

    }
  }
}
