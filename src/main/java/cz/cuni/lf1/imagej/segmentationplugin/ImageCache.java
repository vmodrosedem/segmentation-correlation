package cz.cuni.lf1.imagej.segmentationplugin;

import ij.ImagePlus;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Josef Borkovec
 */
public class ImageCache {

  public LinkedHashMap<ImageKey, ImagePlus> images;
  private static ImageCache instance = new ImageCache();

  private ImageCache() {
    images = new LinkedHashMap<ImageKey, ImagePlus>() {
      @Override
      protected boolean removeEldestEntry(Map.Entry<ImageKey, ImagePlus> eldest) {
        if (size() > 4) {
          return true;
        } else {
          return false;
        }
      }
    };
  }

  public static ImageCache getInstance() {
    return instance;

  }

  public void saveImage(ImagePlus key, ImagePlus value, String op) {
    images.put(new ImageKey(key, op), value);
  }

  public ImagePlus getImage(ImagePlus key, String op) {
    return images.get(new ImageKey(key, op));
  }

  public void invalidate(ImagePlus key, String op) {
    images.remove(new ImageKey(key, op));
  }

  public void destroy() {
    images.clear();
  }

  class ImageKey {

    ImagePlus img;
    String op;

    public ImageKey(ImagePlus img, String op) {
      this.img = img;
      this.op = op;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof ImageKey)) {
        return false;
      }
      ImageKey other = (ImageKey) o;
      if (other != null && other.img.equals(this.img) && this.op.equals(other.op)) {
        return true;
      } else {
        return false;
      }
    }

    @Override
    public int hashCode() {
      return img.hashCode() + op.hashCode();
    }
  }
}
