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

  public static ImagePlus segmentStack(ImagePlus image, double sigma, int threshold) {
    ImageStack stack = image.getStack();
    ImageStack resultStack = new ImageStack(stack.getWidth(), stack.getHeight(), stack.getSize());

    GaussianBlur gaussPlugin = new GaussianBlur();
    for (int i = 0; i < stack.getSize(); i++) {
      ImageProcessor ip = stack.getProcessor(i).convertToByte(true);
      gaussPlugin.blurGaussian(ip, sigma, sigma, 1e-5);

      ip.threshold(threshold);
      resultStack.addSlice(i + "", ip);
    }

    return new ImagePlus("mask", resultStack);
  }

  public static int getOptimumStackThreshold(ImagePlus image, AutoThresholder.Method method) {
    ImageStack stack = image.getStack();

    int[] histogramSum = new int[stack.getProcessor(0).getHistogramSize()];
    for (int i = 0; i < histogramSum.length; i++) {
      histogramSum[i] = 0;
    }

    for (int i = 0; i < stack.getSize(); i++) {
      int[] histogram = stack.getProcessor(i).getHistogram();
      for (int j = 0; j < histogramSum.length; j++) {
        histogramSum[j] += histogram[j];
      }

    }
    AutoThresholder thresholder = new AutoThresholder();
    return thresholder.getThreshold(method, histogramSum);

  }
  
  public static void drawOutlineStack(ImagePlus image, ImagePlus mask, int color){
    if(image.getType() != ImagePlus.COLOR_RGB){
      throw new RuntimeException("image must be of COLOR_RGB type");
    }
    if(mask.getStackSize() != 1 && mask.getStackSize() != image.getStackSize()){
      throw new RuntimeException("mask stack size must be the same as that of image or 1");
    }
    ImageStack imageStack = image.getStack();
    ImageStack maskStack = null;
    ByteProcessor commonMask = null;
    boolean oneImageMask = false;
    if(mask.getStackSize()==1){
      oneImageMask = true;
      commonMask = (ByteProcessor)mask.getProcessor().duplicate();
      commonMask.outline();
    }else {
      maskStack = mask.getStack();
    }
          
    for (int i =0; i < imageStack.getSize(); i++){
      int[] imagePixels = (int[]) imageStack.getProcessor(i).getPixels();
      byte[] maskPixels = (byte[]) ((oneImageMask)?(commonMask.getPixels()):(maskStack.getProcessor(i).getPixels()));
      for(int j = 0; j < imagePixels.length; j++){
        if(maskPixels[j] == 0){
          imagePixels[j] |=color;
        }
      }
      
    }
  }
}
