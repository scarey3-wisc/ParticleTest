# ParticleTest
Contains a variety of programs for experimenting with Stephen's quad-tree particle simulator

The original purpose of this project was to study how air particles interact, by trying to simulate a bunch of interacting particles
with gravity and collisions. As it turned out, many of my assumptions were flawed (in particular, particle-particle interactions have
essentially no effect on the behavior of gases), but the visual effect of the quad tree is quite striking. There are two main
methods that can be run by opening a command prompt in the root directory.

Type "java -cp bin CellVisualizer" to be able to drag points around a quad tree and watch the quad tree update in real time.

Type "java -cp bin IdealGas" to watch a bunch of particles move around the quad tree, where the particles themselves are not displayed,
but rather an average-over-time of the velocities (represented as colors) of the particles inside a cell of the quad tree.
