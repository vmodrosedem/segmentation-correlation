/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.lf1.imagej.imagejtestik;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.HistogramWindow;
import ij.gui.Plot;
import ij.plugin.PlugIn;

/**
 *
 * @author Matlab
 */
public class Plugin implements PlugIn {

  public void run(String string) {
   
    
    int count = WindowManager.getImageCount();
    IJ.showMessage("images: " + WindowManager.getImageCount());
    for (int i = 1; i <= count; i++){
      ImagePlus ip = WindowManager.getImage(i);
      HistogramWindow hw = new HistogramWindow(ip);
      hw.show();

    }
    
    
    double[] x = {1, 2, 3, 4, 5, 6, 7};
    double[] y = {9, 8, 7, 6, 6, 9, 5};
    Plot pl = new Plot("plot", "x axis", "y axis", x, y);
    pl.show();

  }
}
