package cz.cuni.lf1.imagej.segmentationplugin;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.plugin.ZProjector;
import ij.process.AutoThresholder;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloodFiller;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import java.awt.Color;
import java.awt.Point;

/**
 * Actual image processing methods for segmentation plugin.
 *
 * @author Josef Borkovec
 */
public class Process {

  /**
   * Creates a scattergram of two grayscale images (or stacks) using a mask.
   * Only pixels for which there is a nonzero value int the mask image are
   * counted. Requires 8-bit or 16-bit grayscale values. For both 8-bit and
   * 16-bit images 256 values histogram is used.
   *
   * @param image1 (horizontal axis)
   * @param image2 (vertical axis)
   * @param mask mask image of the same width and height as images 1 and 2
   * @return 256*256 16-bit grayscale image with the scattergram.
   */
  public static ImagePlus scattergram(ImagePlus image1, ImagePlus image2, ImagePlus mask) {
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
            int value = plot.getPixel(intensity1, 255 - intensity2);
            plot.putPixel(intensity1, 255 - intensity2, value + 1);
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
            int value = plot.getPixel(intensity1, 255 - intensity2);
            plot.putPixel(intensity1, 255 - intensity2, value + 1);
          }
        }
      }
    }

    return new ImagePlus("Scattergram", plot);
  }

  /**
   * Performs logical and on two mask images. The result overrides the first
   * mask. If res is a stack, mask must have the same number of images or just
   * one image.
   *
   * @param res
   * @param mask
   */
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

  /**
   * converts two grayscale image to one colored overlay. Requires 8bit or 16bit
   * grayscale images (or stacks).
   *
   * @param green image used for green channel
   * @param red image user for red channel
   * @return
   */
  public static ImagePlus convertToRGBOverlay(ImagePlus green, ImagePlus red) {
    ImageStack newStack = new ImageStack(green.getWidth(), green.getHeight());
    for (int i = 1; i <= green.getStackSize(); i++) {
      ImageProcessor ip = new ColorProcessor(green.getWidth(), green.getHeight());
      ImageProcessor gr = (green.getStackSize() == 1) ? green.getProcessor() : green.getStack().getProcessor(i);
      ImageProcessor re = (green.getStackSize() == 1) ? red.getProcessor() : red.getStack().getProcessor(i);
      for (int p = 0; p < ip.getPixelCount(); p++) {
        int greenPix = gr.get(p);
        int redPix = re.get(p);
        if (green.getType() == ImagePlus.GRAY16) {
          greenPix = greenPix / 256;
          redPix = redPix / 256;
        }
        ip.set(p, (redPix << 16) | (greenPix << 8));
      }
      newStack.addSlice(i + "", ip);

    }
    return new ImagePlus("result", newStack);
  }

  /**
   * Simple conversion of every image in stack to RGB image.
   *
   * @param imp
   * @return
   */
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

  /**
   * Does maximum intensity projection of an image stack using ImageJ's
   * Zprojector
   *
   * @param imp
   * @return
   */
  public static ImagePlus maximumIntensityProjection(ImagePlus imp) {
    ZProjector zProjector = new ZProjector();
    zProjector.setImage(imp);
    zProjector.setMethod(ZProjector.MAX_METHOD);
    zProjector.setStopSlice(imp.getStackSize());
    zProjector.doProjection();
    return zProjector.getProjection();
  }

  /**
   * Performs segmentation on Image stack or single image. First the image is
   * blurred by gaussian filter of standard deviation sigma. Than the image is
   * thresholded by a specified threshold. if fillHoles is true than zero
   * regions completely surrounded by nonzero regions are set to 255
   *
   * @param image image for segmentation
   * @param sigma gaussian blur std
   * @param threshold
   * @param fillHoles
   * @return Mask image with non-zero values for identified regions
   */
  public static ImagePlus segmentStack(ImagePlus image, double sigma, int threshold, boolean fillHoles) {
    
    ImageStack resultStack = new ImageStack(image.getWidth(), image.getHeight());
    for (int i = 1; i <= image.getStackSize(); i++) {    
      ImageProcessor ip = image.getStack().getProcessor(i).convertToByte(true);
      //threshold
      ip.threshold(threshold);
      //fill holes
      if (fillHoles) {
        fill(ip, 255, 0);
      }

      resultStack.addSlice("" + i, ip);
      IJ.showProgress(i, image.getStackSize());
    }
    return new ImagePlus("mask", resultStack);
  }

  /**
   * Binary fill by Gabriel Landini, G.Landini at bham.ac.uk
   *
   * 21/May/2008
   *
   * copied from ImageJ source
   *
   * @param ip the images processor
   * @param foreground grayscale value
   * @param background grayscale value
   */
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
   * Finds threshold value from images histogram using specified method. Uses
   * methods from ImageJ's AutoThresholder. For stacks the histogram is summed
   * through all slices.
   *
   * @param image
   * @param method
   * @return
   */
  public static int getOptimumStackThreshold(ImagePlus image, String method) {
    if("K-Means".equals(method)){
      return KMeansThreshold(image);
    }
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

  /**
   * Draws outlines of the mask in a specified RGB images. The mask rewriten to
   * the outline of the mask as a side effect. Bitwise or with the color value
   * is performed with the pixels where there is non-zero value in the outline
   * of the mask.
   *
   * @param image RGB image where the outline is written
   * @param mask mask with non-zero values for the whole region (not only the
   * outline), changed as a side effect
   * @param color pixels are ored with this value, you can use 0xff0000 for red,
   * 0x00ff00 for green and 0x0000ff for blue
   */
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

  /**
   * Does connected components labelling and discards regions that are smaller
   * than a certain number of pixels. If discardBorderRegions is true, regions
   * that touch the border are discarded. Works on stack or single image.
   *
   * @param image binary mask image
   * @param pixels minimum area of regions in pixels
   * @param discardBorderRegions
   */
  public static void filterRegions(ImagePlus image, int pixels, boolean discardBorderRegions) {
    ConnectedComponentsLabeller labeller = new ConnectedComponentsLabeller();
    labeller.filterRegions(image, pixels, discardBorderRegions);
  }

  /**
   * Computes correlation coefficient of the intensities in two images (channel1
   * and channel2). Only uses pixels where the mask is non-zero. Works
   * throughout all images in the stack.
   *
   * @param channel1
   * @param channel2
   * @param mask
   * @return correlation coefficient
   */
  public static double computeCorrelation(ImagePlus channel1, ImagePlus channel2, ImagePlus mask) {
    //first pass, compute means
    double ch1Sum = 0;
    double ch2Sum = 0;
    double pixels = 0;
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
    double ch1Mean = ch1Sum / pixels;
    double ch2Mean = ch2Sum / pixels;

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
          sumGSquared += dif2 * dif2;
        }
      }
    }
    //IJ.showMessage("ch1Mean: " + ch1Mean + "\nch2Mean: " + ch2Mean + "\nsumRG: " + sumRG + "\nsumRSquared: " + sumRSquared + "\nsumGSquared: " + sumGSquared);
    return sumRG / Math.sqrt(sumRSquared * sumGSquared);

  }

  /**
   * Using connected components labelling. Splits separated regions and returns
   * a mask image for eachseparated region.
   *
   * @param mask with multiple separate regions
   * @return array of masks for each region
   */
  public static ImagePlus[] splitCellRegions(ImagePlus mask) {
    ConnectedComponentsLabeller l = new ConnectedComponentsLabeller();
    return l.splitCellRegions(mask);

  }

  public static void showCellMasks(ImagePlus mask) {
    ConnectedComponentsLabeller l = new ConnectedComponentsLabeller();
    ImagePlus[] imgs = l.splitCellRegions(mask);

    for (int i = 0; i < imgs.length; i++) {
      imgs[i].show();
    }
  }

  /**
   * Draws cell number in the centroid of each cell region. The text is drawn in
   * the overlay, not the pixel values.
   *
   * @param rgbImage
   * @param cellMasks an array of masks with non-zero values where the cell
   * region is
   */
  public static void drawCellNumbers(ImagePlus rgbImage, ImagePlus[] cellMasks) {
    Overlay o = new Overlay();
    for (int i = 0; i < cellMasks.length; i++) {
      Point p = computeCentroid(cellMasks[i].getProcessor());
      Roi roi = new TextRoi(p.x, p.y, i + 1 + "");
      roi.setStrokeColor(Color.green);
      o.add(roi);
    }
    rgbImage.setOverlay(o);
  }

  /**
   * Computes the centroid of the non-zero values in image ip.
   *
   * @param ip mask image
   * @return Point object with the centroid values
   */
  public static Point computeCentroid(ImageProcessor ip) {
    int x = 0;
    int y = 0;
    int area = 0;

    for (int i = 0; i < ip.getWidth(); i++) {
      for (int j = 0; j < ip.getHeight(); j++) {
        if (ip.get(i, j) != 0) {
          x += i;
          y += j;
          area += 1;
        }
      }
    }

    x = x / area;
    y = y / area;
    return new Point(x, y);
  }

  public static int KMeansThreshold(ImagePlus img) {
    double centroid1 = img.getProcessor().get(0);
    double centroid2 = 2*img.getProcessor().get(0);

    double change;
    int iteration = 0;
    do {

      long centroid1sum = 0;
      long centroid2sum = 0;
      int centroid1count = 0;
      int centroid2count = 0;
      for (int i = 1; i <= img.getStackSize(); i++) {
        ImageProcessor ip = (img.getStackSize() == 1) ? img.getProcessor() : img.getStack().getProcessor(i);
        for (int p = 0; p < ip.getPixelCount(); p++) {
          int value = ip.get(p);

          if (Math.abs(value - centroid1) < Math.abs(value - centroid2)) {
            centroid1sum += value;
            centroid1count++;
          } else {
            centroid2sum += value;
            centroid2count++;
          }
        }
      }

      double newCentroid1 = centroid1sum / (double) centroid1count;
      double newCentroid2 = centroid2sum / (double) centroid2count;

      double c1change = 0;
      if(centroid1count != 0) {
        c1change = Math.abs(centroid1-newCentroid1);
        centroid1 = newCentroid1;
      }
      double c2change = 0;
      if(centroid2count != 0){
        c2change = Math.abs(centroid2-newCentroid2);
        centroid2 = newCentroid2;
      }
      change = Math.max(c1change, c2change);
      iteration++;
      if(iteration >100) {
        IJ.log("Warning: K-means didn't converge after 50 iterations.");
      }
    } while (change > 0.5 );

    double threshold = img.getType() ==ImagePlus.GRAY16? ((centroid1 + centroid2) / (2*256)) :((centroid1 + centroid2) / 2);
    return (int) threshold;
  }

  public static ImagePlus toFloat(ImagePlus image) {
    ImageStack stack = new ImageStack(image.getWidth(), image.getHeight());
    
    for (int i = 1; i <= image.getStackSize(); i++) {
      //blur, the gaussian blur plugin uses only floating point images
      ImageProcessor ip;
      if (image.getType() == ImagePlus.GRAY32) {
        ip = (image.getStackSize() == 1)
                ? (image.getProcessor().duplicate())
                : (image.getStack().getProcessor(i).duplicate());
      } else {
        ip = (image.getStackSize() == 1)
                ? (image.getProcessor().convertToFloat())
                : (image.getStack().getProcessor(i).convertToFloat());
      }
      stack.addSlice(i+"", ip);
    }
    return new ImagePlus("",stack);
  }
}