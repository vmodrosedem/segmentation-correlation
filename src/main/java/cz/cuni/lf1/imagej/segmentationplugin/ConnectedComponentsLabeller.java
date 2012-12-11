package cz.cuni.lf1.imagej.segmentationplugin;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implements connected components labelling. Can be used to filter regions by their area, discard border-touching regions or create a binary mask for each region separately.
 * Works on stacks or single images.
 * @author Matlab
 */
public class ConnectedComponentsLabeller {

  private ArrayList<Integer> parent;
  private int next;

  public ConnectedComponentsLabeller() {
    parent = new ArrayList<Integer>();
    next = 1;
  }

  public ImagePlus[] splitCellRegions(ImagePlus mask){
    ImageProcessor maskProcessor = mask.getProcessor();
    ShortProcessor labellingProcessor = new ShortProcessor(mask.getWidth(), mask.getHeight());
    componentLabelling(maskProcessor, labellingProcessor);
    Map<Integer,Integer> valueToIndex = new HashMap<Integer, Integer>();
    int nextIndex = 0;
    for(int i = 0; i < labellingProcessor.getPixelCount(); i++){
      int value = labellingProcessor.get(i);
      if(value != 0){
        if(!valueToIndex.containsKey(value)){
          valueToIndex.put(value, nextIndex);
          nextIndex++;
        }
      }
    }
    
    ImagePlus[] ret = new ImagePlus[nextIndex];
    for(int i = 0; i < ret.length; i++){
      ret[i] = new ImagePlus("cell "+ (i+1), new ByteProcessor(mask.getWidth(), mask.getHeight()));
    }
    
    for(int i = 0; i < labellingProcessor.getPixelCount(); i++){
      int value = labellingProcessor.get(i);
      if(value != 0){
        ret[valueToIndex.get(value)].getProcessor().set(i, (byte)255);
      }
    }
    return ret;
  }
  
  public void filterRegions(ImagePlus image, int minPixels, boolean discardTouching) {
    if (minPixels <= 0 && !discardTouching) {
      return;
    }
    boolean isStack = image.getStackSize() > 1;
    ImageProcessor labelsProcessor = new ShortProcessor(image.getWidth(), image.getHeight());  //new short processor, byte is too small and overflows
    for (int slice = 1; slice <= image.getStackSize(); slice++) {
      //label components
      ImageProcessor originalProcessor = isStack ? image.getStack().getProcessor(slice) : image.getProcessor();
      componentLabelling(originalProcessor, labelsProcessor);
      //find which regions are touching the border
      Set<Integer> toDiscard = discardTouching ? findRegionsTouchingBorder(labelsProcessor) : null;
      //discard small and marked regions
      int[] hist = labelsProcessor.getHistogram();
      for (int i = 0; i < labelsProcessor.getPixelCount(); i++) {
        if (originalProcessor.get(i) != 0) {
          if (hist[labelsProcessor.get(i)] >= minPixels && (!discardTouching || !toDiscard.contains(labelsProcessor.get(i)))) {
            originalProcessor.set(i, (byte) 255);
          } else {
            originalProcessor.set(i, 0);
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

  private Set<Integer> findRegionsTouchingBorder(ImageProcessor sp) {
    Set<Integer> toDiscard = new HashSet<Integer>();
    for (int i = 0; i < sp.getWidth(); i++) {
      int pix1 = sp.getPixel(i, 0);
      if (pix1 > 0) {
        toDiscard.add(pix1);
      }
      int pix2 = sp.getPixel(i, sp.getHeight() - 1);
      if (pix2 > 0) {
        toDiscard.add(pix2);
      }
    }
    for (int i = 0; i < sp.getHeight(); i++) {
      int pix1 = sp.getPixel(0, i);
      if (pix1 > 0) {
        toDiscard.add(pix1);
      }
      int pix2 = sp.getPixel(sp.getWidth() - 1, i);
      if (pix2 > 0) {
        toDiscard.add(pix2);
      }
    }
    return toDiscard;
  }

  private void componentLabelling(ImageProcessor origProcessor, ImageProcessor labelProcessor) {
    resetUnionFind();
    //copy to short processor
    byte[] px = (byte[]) origProcessor.getPixels();
    short[] spx = (short[]) labelProcessor.getPixels();
    for (int i = 0; i < px.length; i++) {
      spx[i] = (short) (px[i] & 0xff);
    }
    //first pass
    for (int column = 0; column < origProcessor.getWidth(); column++) {
      for (int row = 0; row < origProcessor.getHeight(); row++) {
        int pixValue = labelProcessor.get(row, column);
        if (pixValue != 0) {
          int north = row > 0 ? labelProcessor.get(row - 1, column) : 0;
          int west = column > 0 ? labelProcessor.get(row, column - 1) : 0;

          if (north == 0 && west == 0) {
            labelProcessor.set(row, column, makeSet());               //make new region
          } else if (north > 0 && west > 0 && north != west) {
            union(north, west);                            //merge regions
            labelProcessor.set(row, column, north);
          } else {
            labelProcessor.set(row, column, Math.max(north, west));    //assign to neighboring region
          }

        }
      }
    }
    //second pass
    for (int column = 0; column < origProcessor.getWidth(); column++) {
      for (int row = 0; row < origProcessor.getHeight(); row++) {
        int pixValue = labelProcessor.get(row, column);
        if (pixValue != 0) {
          labelProcessor.set(row, column, find(pixValue));
        }
      }
    }
  }
}
