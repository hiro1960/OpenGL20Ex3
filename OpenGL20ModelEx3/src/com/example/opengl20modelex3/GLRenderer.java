package com.example.opengl20modelex3;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import OpenGL20common.GLES;
import OpenGL20common.ObjLoader;
import OpenGL20common.Object3D;
import android.content.Context;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

public class GLRenderer implements GLSurfaceView.Renderer {

	//システム
    private float aspect;//アスペクト比
    private int   angle; //回転角度
    
    //モデル
    private Object3D model=new Object3D();             //モデル
    private Object3D floor=new Object3D();             //床
    private float[] lightPos    ={5.0f,3.0f,5.0f,1.0f};//光源の位置
    private float[] floorPos    ={0.0f,0.01f,0.0f};    //床の位置
    private float[] floorNormal ={0.0f,-1.0f,0.0f};    //床の法線の逆
    private float[] shadowMatrix=new float[16];        //影行列
    
    //コンストラクタ
    public GLRenderer(Context context) {
        GLES.context=context;
    }
    
    //サーフェイス生成時に呼ばれる
    @Override
    public void onSurfaceCreated(GL10 gl10,EGLConfig eglConfig) {
        //シェーダの初期化
        GLES.makeProgram();
        
        //デプスバッファの有効化
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        
        //光源色の指定
        GLES20.glUniform4f(GLES.lightAmbientHandle,0.2f,0.2f,0.2f,1.0f);
        GLES20.glUniform4f(GLES.lightDiffuseHandle,0.7f,0.7f,0.7f,1.0f);
        GLES20.glUniform4f(GLES.lightSpecularHandle,0.9f,0.9f,0.9f,1.0f);
        
        //影行列の計算
        calcShadowMatrix(shadowMatrix,lightPos,floorPos,floorNormal);
        
        //モデルの読み込み
        try {
            model.figure=ObjLoader.load("droid.obj");
            floor.figure=ObjLoader.load("floor.obj");
        } catch (Exception e) {
            android.util.Log.e("debug",e.toString());
            for (StackTraceElement ste:e.getStackTrace()) {
                android.util.Log.e("debug","    "+ste);
            }
        }      
    }
    
    //画面サイズ変更時に呼ばれる
    @Override
    public void onSurfaceChanged(GL10 gl10,int w,int h) {
        //ビューポート変換
        GLES20.glViewport(0,0,w,h);
        aspect=(float)w/(float)h;
    }
    
    //毎フレーム描画時に呼ばれる
    @Override
    public void onDrawFrame(GL10 gl10) {
        //画面のクリア
        GLES20.glClearColor(0.5f,0.5f,1.0f,1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|
            GLES20.GL_DEPTH_BUFFER_BIT);
                
        //射影変換
        Matrix.setIdentityM(GLES.pMatrix,0);
        GLES.gluPerspective(GLES.pMatrix,
            45.0f,  //Y方向の画角
            aspect, //アスペクト比
            0.01f,  //ニアクリップ
            100.0f);//ファークリップ                
        
        //ビュー変換
        Matrix.setIdentityM(GLES.mMatrix,0);
        float eyeX=(float)(10.0f*Math.cos(angle*Math.PI/180));
        float eyeZ=(float)(10.0f*Math.sin(angle*Math.PI/180));
        angle++;
        GLES.gluLookAt(GLES.mMatrix,
            eyeX,4.0f,eyeZ, //カメラの視点
            0.0f,0.8f,0.0f, //カメラの焦点
            0.0f,1.0f,0.0f);//カメラの上方向
        
        //光源位置の指定
        float[] resultM=new float[4];
        Matrix.multiplyMV(resultM,0,GLES.mMatrix,0,lightPos,0);
        GLES20.glUniform4f(GLES.lightPosHandle,
            resultM[0],resultM[1],resultM[2],resultM[3]);
        
        //モデルの描画
        GLES20.glUniform1i(GLES.useLightHandle,1);
        model.draw();   
        
        //床の描画
        GLES20.glUniform1i(GLES.useLightHandle,0);
        GLES20.glUniform4fv(GLES.colorHandle,1,new float[]{1,1,1,1},0);
        floor.draw();
        
        //影の描画
        GLES20.glUniform4fv(GLES.colorHandle,1,new float[]{0,0,0,1},0);
        GLES.glPushMatrix();
        float[] workM=new float[16];
        System.arraycopy(GLES.mMatrix,0,workM,0,16);
        Matrix.multiplyMM(GLES.mMatrix,0,workM,0,shadowMatrix,0);
        model.draw();
        GLES.glPopMatrix();
    }
    
    //影行列の計算
    private void calcShadowMatrix(float[] m,float[] l,float[] g,float[] n) {
        float d=(n[0]*l[0])+(n[1]*l[1])+(n[2]*l[2]);
        float c=(g[0]*n[0])+(g[1]*n[1])+(g[2]*n[2])-d;
        m[0]=l[0]*n[0]+c;     m[1]=l[1]*n[0];       m[2]=l[2]*n[0];       m[3]=n[0];
        m[4]=l[0]*n[1];       m[5]=l[1]*n[1]+c;     m[6]=l[2]*n[1];       m[7]=n[1];
        m[8]=l[0]*n[2];       m[9]=l[1]*n[2];       m[10]=l[2]*n[2]+c;    m[11]=n[2];
        m[12]=-l[0]*c-l[0]*d; m[13]=-l[1]*c-l[1]*d; m[14]=-l[2]*c-l[2]*d; m[15]=-d;
    }
}
