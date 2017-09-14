/*
 * Physic engine class
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

import java.util.Random;

public class Game {
	// TODO Tc = 1/FPS value is updated from main loop. 
	// Fixed Tc is required if rotations matrixes shall be multiplied
	// Rotation matrix multiplication is required to keep more rotations applied to object
	float Tc = 1.0f / 20.0f;

	class mD3DObject {
		public M3DMATRIX Rotations;
		public M3DVECTOR Velocity;
		public M3DVECTOR New_Velocity;
		public M3DVECTOR Position;
		public M3DM.mD3DFrame Frame;
		public float R, M; // radius, mass in megatons
		public float _E;
	};

	int INSPHERE = 1;			// objects are inside large sphere
	float SPHERE_R = 18.0f;		// large sphere radius

	int OVERLAPTEST = 1;		// test objects get overlapped
	int Gravity = 0;			// gravity between objects
	int VGravity = 1;			// vertical gravity applied to objects
	
	static M3DVECTOR At = new M3DVECTOR(0.0f, 0.0f, 0.0f); // accelleration vector
	static M3DVECTOR Gt = new M3DVECTOR(0.0f, 0.0f, 0.0f); // vertical gravity vector m/(s*s)
														
	float Restitution = 0.4f;
	float Kapa = 6.7f / 100.0f;

	int N_BALLS = 0; // number of balls, read from preferences. But verify MAXNFRAMES in M3DM to be higher !!!
	mD3DObject[] Ball = null;
	M3DM.mD3DMesh[] mBall = null;

	Game(int cubes) {
		N_BALLS = cubes;
		Ball = new mD3DObject[N_BALLS];
		mBall = new M3DM.mD3DMesh[N_BALLS];
	}
	
	// called from renderer, when the texture is loaded
	void setTextures(M3DM.mD3DTexture texture) {
		for (int a = 0; a < N_BALLS; a++) {
			mBall[a].Textures = 1;
			mBall[a].setTexture(0, texture);
		}
	}
	
	void initSys(M3DM.mD3DFrame scene) {
		int a, r;
		Random generator = new Random();

		for (a = 0; a < N_BALLS; a++) {
			
			// create objects
			Ball[a] = new mD3DObject();
			Ball[a].New_Velocity = new M3DVECTOR(0.0f, 0.0f, 0.0f);
			Ball[a].M = (float) (generator.nextInt(100)) * 50.0f + 0.1f;
			Ball[a].R = (float) Math.pow(Ball[a].M, 1.0/3.0)/7.0f + 0.7f;
			//Ball[a].R = (float) (generator.nextInt(200)) / 100.0f + 1.0f;
			Ball[a].Position = new M3DVECTOR((float) (generator.nextInt(200) - 100) / 10.0f, (float) (generator.nextInt() % 200 - 100) / 10.0f,
					(float) (generator.nextInt(200) - 100) / 10.0f);

			Ball[a].Frame = new M3DM.mD3DFrame(scene);
			
			Ball[a].Frame.Orientation = new M3DVECTOR((float) (generator.nextInt(200) - 100) / 100.0f, (float) (generator.nextInt(200) - 100) / 100.0f, (float) (generator.nextInt(200) - 100) / 100.0f);
			if (Ball[a].Frame.Orientation.x == 0.0f && Ball[a].Frame.Orientation.y == 0.0f && Ball[a].Frame.Orientation.z == 0.0f) {
				Ball[a].Frame.Orientation = new M3DVECTOR(0.0f, 0.0f, 1.0f);
			}
			Ball[a].Frame.Orientation = M3DVECTOR.Normalize(Ball[a].Frame.Orientation);
			
			Ball[a].Frame.Up = new M3DVECTOR((float) (generator.nextInt(200) - 100) / 100.0f, (float) (generator.nextInt(200) - 100) / 100.0f, (float) (generator.nextInt(200) - 100) / 100.0f);
			if (Ball[a].Frame.Up.x == 0.0f && Ball[a].Frame.Up.y == 0.0f && Ball[a].Frame.Up.z == 0.0f) {
				Ball[a].Frame.Up = new M3DVECTOR(0.0f, 1.0f, 0.0f);
			}
			
			M3DVECTOR _right = M3DVECTOR.CrossProduct(Ball[a].Frame.Orientation, Ball[a].Frame.Up);
			_right = M3DVECTOR.Normalize(_right);
			Ball[a].Frame.Up = M3DVECTOR.CrossProduct(_right, Ball[a].Frame.Orientation);
			
			// create spheres
			//mBall[a] = M3DM.createSphere(Ball[a].R, 90, 90, 0.0f, 0.0f, 1.0f, 1.0f);
			
			// create cubes
			mBall[a] = M3DM.createCube((float)Math.sqrt(2.0)*Ball[a].R/2.0f);
			
			r = generator.nextInt(3);
			
			// set material
			M3DM.M3DMATERIAL mt = new M3DM.M3DMATERIAL(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 2.0f, 0.0f, 1.0f, 1.0f);
			
			
			// randomize material
			/*M3DM.M3DMATERIAL mt = null;
			M3DM.M3DMATERIAL mt1 = new M3DM.M3DMATERIAL(0.5f, 0.5f, 1.0f, 1.0f,  0.3f, 0.3f, 1.0f, 1.0f,  1.0f, 1.0f, 1.0f, 1.0f,  0.0f, 0.0f, 0.0f, 1.0f,  10.0f, 0.0f, 1.0f, 1.0f);
			M3DM.M3DMATERIAL mt2 = new M3DM.M3DMATERIAL(1.0f, 0.5f, 0.5f, 1.0f,  1.0f, 0.3f, 0.3f, 1.0f,  1.0f, 1.0f, 1.0f, 1.0f,  0.0f, 0.0f, 0.0f, 1.0f,  10.0f, 0.0f, 1.0f, 1.0f);
			M3DM.M3DMATERIAL mt3 = new M3DM.M3DMATERIAL(0.5f, 1.0f, 0.5f, 1.0f,  0.3f, 1.0f, 0.3f, 1.0f,  1.0f, 1.0f, 1.0f, 1.0f,  0.0f, 0.0f, 0.0f, 1.0f,  10.0f, 0.0f, 1.0f, 1.0f);
			if(r==0){ mt = mt1; }
			else if(r==1){ mt = mt2; }
			else if(r==2){ mt = mt3; }*/
			mBall[a].setMaterial(mt);
			
			
			// sphere map to simulate reflection
			// r=rand()%3;
			// if(r==0){ ATTEMPT(mBall[a]->SetFlags(MD3DMESHF_SPHERMAP)); }

			Ball[a].Frame.addMesh(mBall[a]);
			Ball[a].Frame.Position = Ball[a].Position;
			Ball[a]._E = -Kapa * (Ball[a].M);

		
			// will be set by setTextures, after texture is loaded
			mBall[a].Textures = 0;

			Ball[a].Rotations = M3DMATRIX.IdentityMatrix();
		}

	}
	
	// loop to to calculate physic called from renderer loop
	void computeScene() {
		int a, b;
		
		// apply the velocity vector and calculate new positions
		for (a = 0; a < N_BALLS; a++) {
			Ball[a].Velocity = Ball[a].New_Velocity;
			Ball[a].Position = M3DVECTOR.ADD(Ball[a].Position, M3DVECTOR.MUL(Ball[a].Velocity, Tc));
			Ball[a].Frame.Position = Ball[a].Position;

			Ball[a].Frame.Orientation = M3DMATRIX.VxM(Ball[a].Frame.Orientation, Ball[a].Rotations);
			Ball[a].Frame.Up = M3DMATRIX.VxM(Ball[a].Frame.Up, Ball[a].Rotations);
			Ball[a].Frame.setWorldM();
		}
		M3DVECTOR temp_V = new M3DVECTOR(0.0f, 0.0f, 0.0f);
		
		// calculate ball collision and inter object gravity
		for (a = 0; a < N_BALLS; a++) {
			for (b = 0; b < N_BALLS; b++) {
				if (a != b) {
					if (M3DVECTOR.SquareMagnitude(M3DVECTOR.DIF(Ball[b].Position, Ball[a].Position)) <= (Ball[a].R + Ball[b].R) * (Ball[a].R + Ball[b].R)) {
						ballsCollision(Ball[a], Ball[b]);
					} else if (Gravity == 1) {
						float r = M3DVECTOR.Magnitude(M3DVECTOR.DIF(Ball[a].Position, Ball[b].Position));
						if (r != 0.0f) {
							temp_V = M3DVECTOR.ADD(temp_V,
									M3DVECTOR.MUL(M3DVECTOR.DIF(Ball[a].Position, Ball[b].Position), Tc * ((Ball[b]._E) / (r * r * r))));
						}
					}
				}
			}
			Ball[a].New_Velocity = M3DVECTOR.ADD(Ball[a].New_Velocity, temp_V);
			temp_V = new M3DVECTOR(0.0f, 0.0f, 0.0f);
		}
		
		// check after if the objects are not overlapping and correct them back
		if (OVERLAPTEST == 1) {
			for (a = 0; a < N_BALLS; a++) {
				for (b = a + 1; b < N_BALLS; b++) {
					if (M3DVECTOR.SquareMagnitude(M3DVECTOR.DIF(Ball[b].Position, Ball[a].Position)) <= (Ball[a].R + Ball[b].R) * (Ball[a].R + Ball[b].R)) {
						// test prekryvania
						M3DVECTOR X = M3DVECTOR.DIF(Ball[b].Position, Ball[a].Position);
						float M = M3DVECTOR.Magnitude(X);
						if (M != 0.0f) {
							M3DVECTOR P, P1, P2;
							P = M3DVECTOR.MUL(X, (Ball[a].R + Ball[b].R - M) / M);
							P1 = M3DVECTOR.MUL(P, (Ball[a].M / (Ball[a].M + Ball[b].M)));
							P2 = M3DVECTOR.MUL(P, (Ball[b].M / (Ball[a].M + Ball[b].M)));
							Ball[b].Position = M3DVECTOR.ADD(Ball[b].Position, P1);
							Ball[a].Position = M3DVECTOR.DIF(Ball[a].Position, P2);
						}
					}
				}
			}
		}
		
		// calculate the outer sphere collisions
		if (INSPHERE == 1) {
			for (a = 0; a < N_BALLS; a++) {
				if ((M3DVECTOR.Magnitude(Ball[a].Position) + Ball[a].R) > SPHERE_R) {
					// overlap test
					M3DVECTOR NormalaS = M3DVECTOR.MUL(Ball[a].Position, -1.0f);
					float Nmagn = M3DVECTOR.Magnitude(NormalaS);
					if (Nmagn != 0.0f) {
						NormalaS = M3DVECTOR.MUL(NormalaS, 1.0f / Nmagn);
					}

					Ball[a].New_Velocity = M3DVECTOR.ADD(M3DVECTOR.MUL(NormalaS, (1.0f + Restitution) * M3DVECTOR.DotProduct(M3DVECTOR.MUL(Ball[a].New_Velocity, -1.0f), NormalaS)), Ball[a].New_Velocity);
					Ball[a].Position = M3DVECTOR.MUL(NormalaS, -(SPHERE_R - Ball[a].R));

					// pseudo rotation
					M3DVECTOR v1, v2, X;
					// impact position
					X = M3DVECTOR.ADD(M3DVECTOR.MUL(NormalaS, -Ball[a].R), Ball[a].Position);
					v1 = M3DVECTOR.DIF(M3DVECTOR.ADD(M3DMATRIX.VxM(M3DVECTOR.DIF(X, Ball[a].Position), Ball[a].Rotations), Ball[a].Position), X);

					// friction 100%
					M3DVECTOR N = M3DVECTOR.MUL(NormalaS, -1.0f);
					M3DVECTOR V1_2 = M3DVECTOR.ADD(Ball[a].New_Velocity, M3DVECTOR.MUL(N, -M3DVECTOR.DotProduct(N, Ball[a].New_Velocity)));
					M3DVECTOR vel = M3DVECTOR.ADD(v1, V1_2);
					M3DVECTOR os;
					float uhl = (M3DVECTOR.Magnitude(vel)) / (Ball[a].R);
					os = M3DVECTOR.CrossProduct(M3DVECTOR.MUL(vel, -1.0f), M3DVECTOR.DIF(Ball[a].Position, X));

					Ball[a].Rotations = M3DMATRIX.POINTROTATE_MATRIX(new M3DVECTOR(0.0f, 0.0f, 0.0f), os, uhl * Tc);
				}
			}
		}
		
		// vertical gravity
		if (VGravity == 1) {
			for (a = 0; a < N_BALLS; a++) {
				Ball[a].New_Velocity = M3DVECTOR.ADD(Ball[a].New_Velocity, M3DVECTOR.MUL(Gt, Tc*5.0f));
				Ball[a].New_Velocity = M3DVECTOR.ADD(Ball[a].New_Velocity, M3DVECTOR.MUL(At, Tc*20.0f));
			}
		}

	}

	// method to calculate balls collision
	void ballsCollision(mD3DObject B1, mD3DObject B2) {
		M3DVECTOR N;
		float SQM_N;
		float M1 = B1.M, M2 = B2.M;
		N = M3DVECTOR.DIF(B2.Position, B1.Position);
		SQM_N = M3DVECTOR.SquareMagnitude(N);

		M3DVECTOR V1_1, V1_2, V2_1, V2_2; // 1. velocity portion 1. object ...2.
											// ...

		V1_1 = M3DVECTOR.MUL(N, (M3DVECTOR.DotProduct(B1.Velocity, N) / SQM_N));
		V1_2 = M3DVECTOR.ADD(B1.Velocity, M3DVECTOR.MUL(N, (-M3DVECTOR.DotProduct(N, B1.Velocity) / SQM_N)));
		V2_1 = M3DVECTOR.MUL(N, (M3DVECTOR.DotProduct(B2.Velocity, N) / SQM_N));
		V2_2 = M3DVECTOR.ADD(B2.Velocity, M3DVECTOR.MUL(N, -M3DVECTOR.DotProduct(N, B2.Velocity) / SQM_N));

		M3DVECTOR x_V1_1, x_V2_1;
		x_V1_1 = M3DVECTOR.MUL(M3DVECTOR.ADD(M3DVECTOR.MUL(V1_1, M1 - M2), M3DVECTOR.MUL(V2_1, 2 * M2)), 1.0f / (M1 + M2));
		x_V2_1 = M3DVECTOR.MUL(M3DVECTOR.ADD(M3DVECTOR.MUL(V2_1, M2 - M1), M3DVECTOR.MUL(V1_1, 2 * M1)), 1.0f / (M1 + M2));

		B1.New_Velocity = M3DVECTOR.ADD(M3DVECTOR.MUL(x_V1_1, Restitution), V1_2);

		// pseudo rotation
		M3DVECTOR F1, v1, v2, X;
		// impact position
		X = M3DVECTOR.ADD(M3DVECTOR.MUL(N, B1.R / (B1.R + B2.R)), B1.Position);
		F1 = M3DVECTOR.MUL((M3DVECTOR.DIF(x_V1_1, V1_1)), M1 / Tc);
		v1 = M3DVECTOR.DIF(M3DVECTOR.ADD(M3DMATRIX.VxM(M3DVECTOR.DIF(X, B1.Position), B1.Rotations), B1.Position), X);
		v2 = M3DVECTOR.DIF(M3DVECTOR.ADD(M3DMATRIX.VxM(M3DVECTOR.DIF(X, B2.Position), B2.Rotations), B2.Position), X);

		/*
		 * M3DVECTOR vel=(v1+V1_2)-(v2+V2_2); float mi=0.01f; float
		 * Mv=Magnitude(vel); M3DVECTOR move, os, F;
		 * if(Mv==0.0f){F=M3DVECTOR(0.0f, 0.0f, 0.0f);} else{
		 * F=-(vel/Mv)*mi*Magnitude(F1); } float uhl; F_naTeleso(F, X, M1,
		 * B1->R, B1->Position, &move, &os, &uhl);
		 * B1->Rotations=B1->Rotations*POINTROTATE_MATRIX(M3DVECTOR(0.0f, 0.0f,
		 * 0.0f), os, uhl*Tc);
		 */
		// friction 100%

		M3DVECTOR vel = M3DVECTOR.DIF(M3DVECTOR.ADD(v1, V1_2), M3DVECTOR.ADD(v2, V2_2));
		M3DVECTOR os;
		float uhl = (M3DVECTOR.Magnitude(vel) * (M2 / (M1 + M2))) / B1.R;
		os = M3DVECTOR.CrossProduct(M3DVECTOR.MUL(vel, -1.0f), M3DVECTOR.MUL(M3DVECTOR.DIF(X, B1.Position), -1.0f));

		B1.Rotations = M3DMATRIX.POINTROTATE_MATRIX(new M3DVECTOR(0.0f, 0.0f, 0.0f), os, uhl * Tc);

	}

	// method to calculate intersection line and plane
	static M3DVECTOR linePlane(M3DVECTOR P, M3DVECTOR n, M3DVECTOR A, M3DVECTOR u) {
		float D = -n.x * P.x - n.y * P.y - n.z * P.z;
		float Dot_nu = M3DVECTOR.DotProduct(n, u);
		if (Dot_nu == 0.0f) {
			return new M3DVECTOR(99999999.0f, 99999999.0f, 99999999.0f);
		}
		float t;
		t = -(M3DVECTOR.DotProduct(n, A) + D) / Dot_nu;
		return new M3DVECTOR(M3DVECTOR.ADD(A, M3DVECTOR.MUL(u, t)));
	}

	// method to calculate intersection line and sphere
	static float lineSphere(M3DVECTOR A, M3DVECTOR u, mD3DObject obj) {
		if (u.equals(new M3DVECTOR(0.0f, 0.0f, 0.0f))) {
			return -9999999.0f;
		}
		float D, a, b, c;
		a = M3DVECTOR.SquareMagnitude(u);
		b = 2.0f * M3DVECTOR.DotProduct(A, u) - 2.0f * M3DVECTOR.DotProduct(obj.Position, u);
		c = M3DVECTOR.SquareMagnitude(A) + M3DVECTOR.SquareMagnitude(obj.Position) - obj.R * obj.R - 2.0f * M3DVECTOR.DotProduct(A, obj.Position);
		D = b * b - 4.0f * a * c;
		return D;
	}

	// method to calculate intersection line and objects in scene
	mD3DObject computeIntersection(M3DVECTOR A, M3DVECTOR u) {
		int a;
		mD3DObject Obj = null;
		float Dist = M3DM.P_FPlane*M3DM.P_FPlane, NDist;
		for (a = 0; a < N_BALLS; a++) {
			if (lineSphere(A, u, Ball[a]) >= 0) {
				NDist = M3DVECTOR.SquareMagnitude(M3DVECTOR.DIF(Ball[a].Position, A));
				if (NDist < Dist) {
					Obj = Ball[a];
					Dist = NDist;
				}
			}
		}
		return Obj;
	}

}
