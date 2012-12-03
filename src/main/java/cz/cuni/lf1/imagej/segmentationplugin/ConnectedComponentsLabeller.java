/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.lf1.imagej.segmentationplugin;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import java.util.ArrayList;

/**
 *
 * @author Matlab
 */
public class ConnectedComponentsLabeller {

  private ArrayList<Integer> parent;
  private int next;

  public ConnectedComponentsLabeller() {
    parent = new ArrayList<Integer>();
    next = 1;
  }

  public void discardSmallRegions(ImagePlus image, int pixels) {
    boolean isStack = image.getStackSize() > 1;
    
    ImageProcessor sp = new ShortProcessor(image.getWidth(), image.getHeight());  //new short processor, byte is too small and overflows
    for (int slice = 1; slice <= image.getStackSize(); slice++) {
      resetUnionFind();
      
      ImageProcessor ip = isStack ? image.getStack().getProcessor(slice) : image.getProcessor();
      //copy to short processor
      byte[] px = (byte[]) ip.getPixels();
      short[] spx = (short[]) sp.getPixels();
      for(int i = 0; i < px.length; i++){
        spx[i] = (short) (px[i] & 0xff);
      }
      //first pass
      for (int column = 0; column < image.getWidth(); column++) {
        for (int row = 0; row < image.getHeight(); row++) {
          int pixValue = sp.get(row, column);
          if (pixValue != 0) {
            int north = row > 0 ? sp.get(row - 1, column) : 0;
            int west = column > 0 ? sp.get(row, column - 1) : 0;

            if (north == 0 && west == 0) {
              sp.set(row, column, makeSet());               //make new region
            } else if (north > 0 && west > 0 && north != west) {
              union(north, west);                            //merge regions
              sp.set(row, column, north);
            } else {
              sp.set(row, column, Math.max(north, west));    //assign to neighboring region
            }

          }
        }
      }
      //second pass
      for (int column = 0; column < image.getWidth(); column++) {
        for (int row = 0; row < image.getHeight(); row++) {
          int pixValue = sp.get(row, column);
          if (pixValue != 0) {
            sp.set(row, column, find(pixValue));
          }
        }
      }
      //discard small ones
      int[] hist = sp.getHistogram();
      for (int i = 0; i < sp.getPixelCount(); i++) {
        if (ip.get(i) != 0) {
          if (hist[sp.get(i)] >= pixels) {
            ip.set(i, (byte) 255);
          } else {
            ip.set(i, 0);
          }
        }
      }

    }
  }

  private void union(int x, int y) {
    int xRoot = find(x);
    int yRoot = find(y);
    if (yRoot == xRoot) {
      return;
    }

    if (xRoot > yRoot) {
      parent.set(xRoot, yRoot);
    } else {
      parent.set(yRoot, xRoot);
    }
  }

  private int find(int x) {
    if (parent.get(x) != x) {
      parent.set(x, find(parent.get(x)));
    }
    return parent.get(x);
  }

  private int makeSet() {
    int ret = next;
    next++;
    parent.add(ret);
    return ret;
  }

  private void resetUnionFind() {
    parent.clear();
    parent.add(0);
    next = 1;
  }
}
