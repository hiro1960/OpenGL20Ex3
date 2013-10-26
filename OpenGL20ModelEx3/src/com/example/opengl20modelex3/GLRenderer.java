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

	//�V�X�e��
    private float aspect;//�A�X�y�N�g��
    private int   angle; //��]�p�x
    
    //���f��
    private Object3D model=new Object3D();             //���f��
    private Object3D floor=new Object3D();             //��
    private float[] lightPos    ={5.0f,3.0f,5.0f,1.0f};//�����̈ʒu
    private float[] floorPos    ={0.0f,0.01f,0.0f};    //���̈ʒu
    private float[] floorNormal ={0.0f,-1.0f,0.0f};    //���̖@���̋t
    private float[] shadowMatrix=new float[16];        //�e�s��
    
    //�R���X�g���N�^
    public GLRenderer(Context context) {
        GLES.context=context;
    }
    
    //�T�[�t�F�C�X�������ɌĂ΂��
    @Override
    public void onSurfaceCreated(GL10 gl10,EGLConfig eglConfig) {
        //�V�F�[�_�̏�����
        GLES.makeProgram();
        
        //�f�v�X�o�b�t�@�̗L����
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        
        //�����F�̎w��
        GLES20.glUniform4f(GLES.lightAmbientHandle,0.2f,0.2f,0.2f,1.0f);
        GLES20.glUniform4f(GLES.lightDiffuseHandle,0.7f,0.7f,0.7f,1.0f);
        GLES20.glUniform4f(GLES.lightSpecularHandle,0.9f,0.9f,0.9f,1.0f);
        
        //�e�s��̌v�Z
        calcShadowMatrix(shadowMatrix,lightPos,floorPos,floorNormal);
        
        //���f���̓ǂݍ���
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
    
    //��ʃT�C�Y�ύX���ɌĂ΂��
    @Override
    public void onSurfaceChanged(GL10 gl10,int w,int h) {
        //�r���[�|�[�g�ϊ�
        GLES20.glViewport(0,0,w,h);
        aspect=(float)w/(float)h;
    }
    
    //���t���[���`�掞�ɌĂ΂��
    @Override
    public void onDrawFrame(GL10 gl10) {
        //��ʂ̃N���A
        GLES20.glClearColor(0.5f,0.5f,1.0f,1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|
            GLES20.GL_DEPTH_BUFFER_BIT);
                
        //�ˉe�ϊ�
        Matrix.setIdentityM(GLES.pMatrix,0);
        GLES.gluPerspective(GLES.pMatrix,
            45.0f,  //Y�����̉�p
            aspect, //�A�X�y�N�g��
            0.01f,  //�j�A�N���b�v
            100.0f);//�t�@�[�N���b�v                
        
        //�r���[�ϊ�
        Matrix.setIdentityM(GLES.mMatrix,0);
        float eyeX=(float)(10.0f*Math.cos(angle*Math.PI/180));
        float eyeZ=(float)(10.0f*Math.sin(angle*Math.PI/180));
        angle++;
        GLES.gluLookAt(GLES.mMatrix,
            eyeX,4.0f,eyeZ, //�J�����̎��_
            0.0f,0.8f,0.0f, //�J�����̏œ_
            0.0f,1.0f,0.0f);//�J�����̏����
        
        //�����ʒu�̎w��
        float[] resultM=new float[4];
        Matrix.multiplyMV(resultM,0,GLES.mMatrix,0,lightPos,0);
        GLES20.glUniform4f(GLES.lightPosHandle,
            resultM[0],resultM[1],resultM[2],resultM[3]);
        
        //���f���̕`��
        GLES20.glUniform1i(GLES.useLightHandle,1);
        model.draw();   
        
        //���̕`��
        GLES20.glUniform1i(GLES.useLightHandle,0);
        GLES20.glUniform4fv(GLES.colorHandle,1,new float[]{1,1,1,1},0);
        floor.draw();
        
        //�e�̕`��
        GLES20.glUniform4fv(GLES.colorHandle,1,new float[]{0,0,0,1},0);
        GLES.glPushMatrix();
        float[] workM=new float[16];
        System.arraycopy(GLES.mMatrix,0,workM,0,16);
        Matrix.multiplyMM(GLES.mMatrix,0,workM,0,shadowMatrix,0);
        model.draw();
        GLES.glPopMatrix();
    }
    
    //�e�s��̌v�Z
    private void calcShadowMatrix(float[] m,float[] l,float[] g,float[] n) {
        float d=(n[0]*l[0])+(n[1]*l[1])+(n[2]*l[2]);
        float c=(g[0]*n[0])+(g[1]*n[1])+(g[2]*n[2])-d;
        m[0]=l[0]*n[0]+c;     m[1]=l[1]*n[0];       m[2]=l[2]*n[0];       m[3]=n[0];
        m[4]=l[0]*n[1];       m[5]=l[1]*n[1]+c;     m[6]=l[2]*n[1];       m[7]=n[1];
        m[8]=l[0]*n[2];       m[9]=l[1]*n[2];       m[10]=l[2]*n[2]+c;    m[11]=n[2];
        m[12]=-l[0]*c-l[0]*d; m[13]=-l[1]*c-l[1]*d; m[14]=-l[2]*c-l[2]*d; m[15]=-d;
    }
}
