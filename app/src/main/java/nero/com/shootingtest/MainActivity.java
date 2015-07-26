package nero.com.shootingtest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import nero.com.shootingtest.impl.PhotoShot;
import nero.com.shootingtest.utils.FileUtils;


public class MainActivity extends Activity implements View.OnClickListener {

    private ImageView ivShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.tv_shot).setOnClickListener(this);
        findViewById(R.id.tv_gallery).setOnClickListener(this);

        ivShow = (ImageView) findViewById(R.id.iv_show);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_shot:
                PhotoShot.startShotPhoto(this);
                break;
            case R.id.tv_gallery:
                PhotoShot.startLocalPhoto(this);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PhotoShot.SHOT_PHOTO:
                case PhotoShot.LOCAL_PHOTO:
                    PhotoShot.startCutPhoto(MainActivity.this, requestCode, data);
                    break;
                case PhotoShot.CUT_PHOTO:
                    File file = PhotoShot.getPhotoFile(MainActivity.this, requestCode, data);
                    if (file != null && file.exists()) {
                        ivShow.setImageURI(FileUtils.getFileUri(file));
                    } else {
                        Toast.makeText(MainActivity.this, "上传头像失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
