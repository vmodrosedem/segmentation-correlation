package cz.cuni.lf1.imagej.imagejtestik;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * Hello world!
 *
 */
public class Filter implements PlugInFilter {

  ImagePlus imp;
  
  public int setup(String string, ImagePlus ip) {
  imp = ip;
    return DOES_ALL;
  }

  public void run(ImageProcessor ip) {
    ip.invert();
    imp.updateAndDraw();
    IJ.wait(500);
    ip.invert();
    imp.updateAndDraw();
  }
}
