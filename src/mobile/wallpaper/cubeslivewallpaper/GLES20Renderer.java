/*
 * Renderer/main loop class
 *
 * This file is part of Cubes Live Wallpaper
 * Cubes Live Wallpaper is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cubes Live Wallpaper is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cubes Live Wallpaper.  If not, see <http://www.gnu.org/licenses/>.
 */

package mobile.wallpaper.cubeslivewallpaper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import net.rbgrn.opengl.GLWallpaperService.GLEngine;

import mobile.wallpaper.cubeslivewallpaper.Game;
import mobile.wallpaper.cubeslivewallpaper.M3DM;
import mobile.wallpaper.cubeslivewallpaper.M3DMATRIX;
import mobile.wallpaper.cubeslivewallpaper.M3DVECTOR;
import mobile.wallpaper.cubeslivewallpaper.Game.mD3DObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;

public class GLES20Renderer implements GLSurfaceView.Renderer {

	final static String TAG = "GLES20Renderer";
	
	boolean reloadedTextures = false;
	boolean preferencesChanged = false;
    
    long _t1,_t2;				// used for FPS calculation
	float FPS = 30.0f;			// frames per seconds
	float Tc = 1.0f/FPS;		// duration of 1 frame
	int Nrenderedframe = 0;
    
	M3DM DEV = null;
	Game game = null;
	M3DM.mD3DFrame scene;
	M3DM.mD3DFrame fcube;
	M3DM.mD3DMesh cube;
	M3DM.mD3DTexture tcube;
	
	Context mContext;
	
	M3DMATRIX mRotation;
	
    Bitmap mBitmap = null;
    M3DM.mD3DTexture mTexture = new M3DM.mD3DTexture();
    
    private int mDelay = 10;
    private int mCubes = 5;

    private SharedPreferences preferences_;
	private SettingsUpdater settingsUpdater_;
  
	public GLES20Renderer(Context context) {
		super();
		mContext = context;
		initialize();
	}
	
    public void setDelay(int delay) {
    	mDelay = delay;
    }
    
    public void setCubes(int cubes) {
    	mCubes = cubes;
		initialize();
    }
    
	public void setSharedPreferences(SharedPreferences preferences)
	{
		settingsUpdater_ = new SettingsUpdater(this);
		preferences_ = preferences;
		preferences_.registerOnSharedPreferenceChangeListener(settingsUpdater_);
		settingsUpdater_.onSharedPreferenceChanged(preferences_, null);
	}
	
	private class SettingsUpdater implements SharedPreferences.OnSharedPreferenceChangeListener {
		private GLES20Renderer renderer_;
		
		public SettingsUpdater(GLES20Renderer renderer)
		{
			renderer_ = renderer;
		}
		
		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			try
			{

				int delay = sharedPreferences.getInt("delayPref",10);
				renderer_.setDelay(delay);
				
				int cubes = sharedPreferences.getInt("cubesPref",5);
				renderer_.setCubes(cubes);
				
			}
			catch(final Exception e)
			{
				Log.e(TAG, "PREF init error: " + e);			
			}
		}
	}
	
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
    	
    	_t2 = System.nanoTime();
    	
    	if (DEV == null || game == null) {
    		initialize();
    	}
    	
    	
    	DEV.initializeGL();
    	
    	
        // Texture
  		int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        mTexture.id = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture.id);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
        
        // TODO background/sphere texture not created
        /*  // Texture 2
  		int[] textures2 = new int[1];
        GLES20.glGenTextures(1, textures2, 0);

        M3DM.mD3DTexture texture2 = new M3DM.mD3DTexture();
        texture2.id = textures2[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture2.id);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        InputStream is2 = mContext.getResources().openRawResource(R.raw.background);
        Bitmap bitmap2;
        try {
            bitmap2 = BitmapFactory.decodeStream(is2);
        } finally {
            try {
                is.close();
            } catch(IOException e) {
                // Ignore.
            }
        }

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap2, 0);
        bitmap2.recycle(); 
  		*/
        
        game.setTextures(mTexture);
        
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        reloadedTextures = true;
    }

    public void initialize() {

    	DEV = new M3DM();
    	
    	// create 1st light
		DEV.N_Lights = 1;
		DEV.Light[0].AR = 0.1f;
		DEV.Light[0].AG = 0.1f;
		DEV.Light[0].AB = 0.1f;
		DEV.Light[0].DR = 1.0f;
		DEV.Light[0].DG = 1.0f;
		DEV.Light[0].DB = 1.0f;
		DEV.Light[0].SR = 1.0f;
		DEV.Light[0].SG = 1.0f;
		DEV.Light[0].SB = 1.0f;
		DEV.Light[0].AT = 0.0005f;
		DEV.Light[0].Pos = new M3DVECTOR(0.0f, 0.0f, 10.0f);

		
		// create scene
		scene = new M3DM.mD3DFrame();
		
		
		// TODO surrounding sphere not rendered
	/*	cube = M3DM.createSphere(Game.SPHERE_R, 20, 20, 0.0f, 0.0f, 1.0f, 1.0f);
		
		//cube.generateNormals();
		cube.Textures = 1;
		cube.setTexture(0, texture2);
		
		M3DM.M3DMATERIAL material = new M3DM.M3DMATERIAL(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.5f, 0.5f, 0.5f, 1.0f, 2.0f, 0.0f, 1.0f, 1.0f);
		cube.setMaterial(material);
		fcube = new M3DM.mD3DFrame(scene);
		fcube.Orientation = new M3DVECTOR(0.0f, 0.0f, -1.0f);
		cube.setFlags(M3DM.MD3DMESHF_FRONTCULLING);
		fcube.addMesh(cube);*/

	   	game = new Game(mCubes);
	    game.initSys(scene);
		
		
		if (mBitmap != null) {
			mBitmap.recycle();
		}
		
		InputStream is = mContext.getResources().openRawResource(R.raw.logo);

        try {
            mBitmap = BitmapFactory.decodeStream(is);
        } finally {
            try {
                is.close();
            } catch(IOException e) {
                // Ignore
            }
        }

    }
    
    public void onDrawFrame(GL10 unused) {

    	// Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        
        // Main Loop
 		
        game.Tc = Tc;
        game.computeScene();
        
		DEV.renderFrame(scene);
		if (reloadedTextures) { 
			reloadedTextures = false;
			DEV.renderFrame(scene);
		}
		
		int glError = GLES20.glGetError();
		if (glError != GLES20.GL_NO_ERROR) {
			Log.e("OpenGL error", "Error code " + glError);
		}
 
		/******** FPS *********/
		Nrenderedframe++;
		if (Nrenderedframe % 25 == 0) {
			_t1 = _t2; // used for FPS
			_t2 = System.nanoTime();
			double _t = ((double) (_t2 - _t1)) / 1.0e9;
			Tc = (float)(_t / ((double) Nrenderedframe));
			if (Tc != 0.0f) {
				FPS = 1.0f / Tc;
			}
			Nrenderedframe = 0;
		}
		
		try {
			Thread.sleep(mDelay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    }
    
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        
        DEV.initialize(width, height, 2.0f, 200.0f, new M3DVECTOR(0.0f, 0.0f, 45.0f), new M3DVECTOR(0.0f, 0.0f, -1.0f), new M3DVECTOR(0.0f, 1.0f, 0.0f));
        
    }
}
