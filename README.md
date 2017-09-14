# Cubes Live Wallpaper
Cubes Live Wallpaper is live wallpaper application for Android 2.3.3 using the OpenGL ES 2.0, gravity, acceleration sensors and real time physics.

Application is available on the Google Play Market <https://play.google.com/store/apps/details?id=mobile.wallpaper.cubeslivewallpaper&hl=en>.

![](https://github.com/H21lab/Cubes-Live-Wallpaper/blob/master/img/cubeswallpaper_1.png)
![](https://github.com/H21lab/Cubes-Live-Wallpaper/blob/master/img/cubeswallpaper_2.png)

## Compilation
Android SDK, Eclipse, Eclipse Android plugin is required. Please follow the official tutorial how to setup the environment.
Than import the project into Eclipse.

## Known limitations
Cubes are actually spheres. The source include balls elastic collision
Only last rotation apply to objects. The rotation matrix are not yet multiplied
The energy of objects is not shared between rotation and translation movement. Rotation can't cause movement after impact.

Android texture: Portion of this application (Android texture) is reproduced from work created and shared by Google and used according to terms described in the Creative Commons 3.0 Attribution License.
GLWallpaperService: Livewallpaper supports OpenGL ES 2.0 thanks to Robert Green's GLWallpaperService.

## Attribution
Copyright 2011, 2012 Martin Kacer

All the content and resources have been provided in the hope that it will be useful. 
Author do not take responsibility for any misapplication of it. The software is distributed
in the hope that will be useful, but WITHOUT ANY WARRANTY.

## License
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
