package wfz.vrplayerdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    EditText et;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et = findViewById(R.id.et);
        checkPermission();
    }

    public void play(View view) {
        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.setData(Uri.parse(et.getText().toString()));
        startActivity(intent);
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)){
                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (123 == requestCode){
            if(PackageManager.PERMISSION_GRANTED == grantResults[0]){
                Toast.makeText(this, permissions[0]+"\n权限获取成功！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, permissions[0]+"\n权限获取失败！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void changeUrl(View view) {
        if (view instanceof TextView){
            et.setText(((TextView) view).getText());
        }
    }
}
