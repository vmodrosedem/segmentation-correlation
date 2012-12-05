/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.lf1.imagej.segmentationplugin;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.ZProjector;
import ij.plugin.filter.GaussianBlur;
import ij.process.AutoThresholder;
import ij.process.ByteProcessor;
import ij.process.FloodFiller;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

/**
 *
 * @author Matlab
 */
public class Process {

  public static ImagePlus scatterPlot(ImagePlus image1, ImagePlus image2, ImagePlus mask) {
    boolean singleImage = image1.getStackSize() == 1;
    boolean bit16 = (image1.getType() == ImagePlus.GRAY16);

    ImageProcessor plot = new ShortProcessor(256, 256);

    if (bit16) {
      for (int i = 1; i <= image1.getStackSize(); i++) {
        short[] pixels1 = (short[]) (singleImage ? image1.getProcessor().getPixels() : image1.getStack().getProcessor(i).getPixels());
        short[] pixels2 = (short[]) (singleImage ? image2.getProcessor().getPixels() : image2.getStack().getProcessor(i).getPixels());
        byte[] pixelsMask = (byte[]) ((mask.getStackSize() == 1) ? mask.getProcessor().getPixels() : mask.getStack().getProcessor(i).getPixels());

        for (int j = 0; j < pixels1.length; j++) {
          if (pixelsMask[j] != 0) {
            int intensity1 = pixels1[j] & 0xffff;
            intensity1 >>= 8;
            int intensity2 = pixels2[j] & 0xffff;
            intensity2 >>= 8;
            int value = plot.getPixel(intensity1, intensity2);
            plot.putPixel(intensity1, intensity2, value + 1);
          }
        }

      }
    } else {//8bit
      for (int i = 1; i <= image1.getStackSize(); i++) {
        byte[] pixels1 = (byte[]) (singleImage ? image1.getProcessor().getPixels() : image1.getStack().getProcessor(i).getPixels());
        byte[] pixels2 = (byte[]) (singleImage ? image2.getProcessor().getPixels() : image2.getStack().getProcessor(i).getPixels());
        byte[] pixelsMask = (byte[]) ((mask.getStackSize() == 1) ? mask.getProcessor().getPixels() : mask.getStack().getProcessor(i).getPixels());

        for (int j = 0; j < pixels1.length; j++) {
          if (pixelsMask[j] != 0) {
            int intensity1 = pixels1[j] & 0xff;
            int intensity2 = pixels2[j] & 0xff;
            int value = plot.getPixel(intensity1, intensity2);
            plot.putPixel(intensity1, intensity2, value + 1);
          }
        }
      }
    }

    return new ImagePlus("Scatter plot", plot);
  }

  public static void andMaskStack(ImagePlus res, ImagePlus mask) {
    if (res.getStackSize() != mask.getStackSize() && mask.getStackSize() != 1) {
      throw new RuntimeException("Mask stack size must match or be equal to 1");
    }
    if (mask.getType() != ImagePlus.GRAY8) {
      throw new RuntimeException("Only 8-bit gray level images are supported");
    }

    for (int i = 1; i <= res.getStackSize(); i++) {
      byte[] pixels1 = (byte[]) ((res.getStackSize() == 1) ? res.getProcessor().getPixels() : res.getStack().getProcessor(i).getPixels());
      byte[] pixels2 = (byte[]) ((mask.getStackSize() == 1) ? mask.getProcessor().getPixels() : mask.getStack().getProcessor(i).getPixels());
      for (int j = 0; j < pixels1.length; j++) {
        if (pixels1[j] != 0 && pixels2[j] == 0) {
          pixels1[j] = 0;
        }
      }
    }
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

  public static ImagePlus maximumIntensityProjection(ImagePlus imp) {
    ZProjector zProjector = new ZProjector();
    zProjector.setImage(imp);
    zProjector.setMethod(ZProjector.MAX_METHOD);
    zProjector.setStopSlice(imp.getStackSize());
    zProjector.doProjection();
    return zProjector.getProjection();
  }

  public static ImagePlus segmentStack(ImagePlus image, double sigma, int threshold, boolean fillHoles) {
    boolean singleImage = image.getStackSize() == 1;
    ImageStack resultStack = new ImageStack(image.getWidth(), image.getHeight(), image.getStackSize());

    GaussianBlur gaussPlugin = new GaussianBlur();
    for (int i = 1; i <= image.getStackSize(); i++) {
      ImageProcessor ip;
      if (image.getType() == ImagePlus.GRAY8) {
        ip = (singleImage)
                ? (image.getProcessor().duplicate())
                : (image.getStack().getProcessor(i).duplicate());
      } else {
        ip = (singleImage)
                ? (image.getProcessor().convertToByte(true))
                : (image.getStack().getProcessor(i).convertToByte(true));
      }
      gaussPlugin.blurGaussian(ip, sigma, sigma, 1e-5);
      ip.threshold(threshold);
      if (fillHoles) {
        fill(ip, 255, 0);
      }

      resultStack.setPixels(ip.getPixels(), i);
      resultStack.setSliceLabel("" + i, i);
      IJ.showProgress(i, image.getStackSize());
    }

    return new ImagePlus("mask", resultStack);
  }

  // Binary fill by Gabriel Landini, G.Landini at bham.ac.uk
  // 21/May/2008
  private static void fill(ImageProcessor ip, int foreground, int background) {
    int width = ip.getWidth();
    int height = ip.getHeight();
    FloodFiller ff = new FloodFiller(ip);
    ip.setColor(127);
    for (int y = 0; y < height; y++) {
      if (ip.getPixel(0, y) == background) {
        ff.fill(0, y);
      }
      if (ip.getPixel(width - 1, y) == background) {
        ff.fill(width - 1, y);
      }
    }
    for (int x = 0; x < width; x++) {
      if (ip.getPixel(x, 0) == background) {
        ff.fill(x, 0);
      }
      if (ip.getPixel(x, height - 1) == background) {
        ff.fill(x, height - 1);
      }
    }
    byte[] pixels = (byte[]) ip.getPixels();
    int n = width * height;
    for (int i = 0; i < n; i++) {
      if (pixels[i] == 127) {
        pixels[i] = (byte) background;
      } else {
        pixels[i] = (byte) foreground;
      }
    }
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
      commonMask.invertLut();
      commonMask.outline();
    } else {
      maskStack = mask.getStack();
    }

    for (int i = 1; i <= imageStack.getSize(); i++) {
      int[] imagePixels = (int[]) imageStack.getProcessor(i).getPixels();
      byte[] maskPixels;
      if (oneImageMask) {
        maskPixels = (byte[]) commonMask.getPixels();
      } else {
        ByteProcessor mp = (ByteProcessor) maskStack.getProcessor(i);
        mp.invertLut();
        mp.outline();
        maskPixels = (byte[]) mp.getPixels();
      }

      for (int j = 0; j < imagePixels.length; j++) {
        if (maskPixels[j] != 0) {
          imagePixels[j] |= color;
        }
      }

    }
  }

  public static void filterRegions(ImagePlus image, int pixels, boolean discardBorderRegions) {
    ConnectedComponentsLabeller labeller = new ConnectedComponentsLabeller();
    labeller.filterRegions(image, pixels, discardBorderRegions);
  }

  public static double computeCorrelation(ImagePlus channel1, ImagePlus channel2, ImagePlus mask) {
    //first pass, compute means
    long ch1Sum = 0;
    long ch2Sum = 0;
    long pixels = 0;
    for (int slice = 1; slice <= channel1.getStackSize(); slice++) {
      ImageProcessor ch1Processor = (channel1.getStackSize() == 1) ? channel1.getProcessor() : channel1.getStack().getProcessor(slice);
      ImageProcessor ch2Processor = (channel2.getStackSize() == 1) ? channel2.getProcessor() : channel2.getStack().getProcessor(slice);
      ImageProcessor maskProcessor = (mask.getStackSize() == 1) ? mask.getProcessor() : mask.getStack().getProcessor(slice);
      for (int i = 0; i < ch1Processor.getPixelCount(); i++) {
        if (maskProcessor.get(i) != 0) {
          ch1Sum += ch1Processor.get(i);
          ch2Sum += ch2Processor.get(i);
          pixels++;
        }
      }
    }
    double ch1Mean = (double) ch1Sum / pixels;
    double ch2Mean = (double) ch2Sum / pixels;

    //second pass, compute sums
    double sumRG = 0;
    double sumRSquared = 0;
    double sumGSquared = 0;
    for (int slice = 1; slice <= channel1.getStackSize(); slice++) {
      ImageProcessor ch1Processor = (channel1.getStackSize() == 1) ? channel1.getProcessor() : channel1.getStack().getProcessor(slice);
      ImageProcessor ch2Processor = (channel2.getStackSize() == 1) ? channel2.getProcessor() : channel2.getStack().getProcessor(slice);
      ImageProcessor maskProcessor = (mask.getStackSize() == 1) ? mask.getProcessor() : mask.getStack().getProcessor(slice);
      for (int i = 0; i < ch1Processor.getPixelCount(); i++) {
        if (maskProcessor.get(i) != 0) {
          double dif = ch1Processor.get(i) - ch1Mean;
          sumRSquared += dif * dif;
          double dif2 = ch2Processor.get(i) - ch2Mean;
          sumRG += dif * dif2;
          sumGSquared += dif * dif;
        }
      }
    }
    //IJ.showMessage("ch1Mean: " + ch1Mean + "\nch2Mean: " + ch2Mean + "\nsumRG: " + sumRG + "\nsumRSquared: " + sumRSquared + "\nsumGSquared: " + sumGSquared);
    return sumRG / Math.sqrt(sumRSquared * sumGSquared);

  }
}