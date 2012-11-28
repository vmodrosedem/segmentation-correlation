/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.lf1.imagej.imagejtestik;

import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

/**
 *
 * @author Matlab
 */
public class Utils {

  public static String getImageType(ImageProcessor ip) {
    if (ip instanceof ByteProcessor) {
      return "Byte";
    } else if (ip instanceof ShortProcessor) {
      return "Short";
    } else if (ip instanceof FloatProcessor) {
      return "Float";
    } else {
      return "RGB";
    }
  }
}
