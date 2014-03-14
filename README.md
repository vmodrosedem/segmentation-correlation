Segmentation-correlation
========================

An ImageJ plugin for computing correlation coefficient between two channels in a segmented region.

Binary releases
===============
v1.0 - http://github.com/vmodrosedem/segmentation-correlation/releases/tag/v1.0

Installation instructions
=========================

Just copy the compiled jar file(i.e. SegmentationPlugin_1.0.jar) to the ImageJ plugins folder. 
"Segmentation plugin" menu item should appear in the "Plugins" menu.


Compilation instructions
========================

Compile using apache maven.

The build script tries to copy the compiled artifact to the ImageJ plugins directory. You will need 
to set the plugins directory path in pom.xml for that to work.