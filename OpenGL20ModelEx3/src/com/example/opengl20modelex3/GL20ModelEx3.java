package com.example.opengl20modelex3;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class GL20ModelEx3 extends Activity {

private GLSurfaceView glView;
    
    //�A�N�e�B�r�e�B�������ɌĂ΂��
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        
        //GL�T�[�t�F�C�X�r���[
        GLRenderer renderer=new GLRenderer(this);
        glView=new GLSurfaceView(this);
        glView.setEGLContextClientVersion(2);
        glView.setRenderer(renderer);
        setContentView(glView);
    }
    
    //�A�N�e�B�r�e�B���W���[�����ɌĂ΂��
    @Override
    public void onResume() {
        super.onResume();
        glView.onResume();
    }
    
    //�A�N�e�B�r�e�B�|�[�Y���ɌĂ΂��
    @Override
    public void onPause() {
        super.onPause();
        glView.onPause();
    }
}
