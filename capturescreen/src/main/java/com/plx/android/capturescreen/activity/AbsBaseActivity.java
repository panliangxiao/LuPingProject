package com.plx.android.capturescreen.activity;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public abstract class AbsBaseActivity extends AppCompatActivity {

    private static final String TAG = AbsBaseActivity.class.getSimpleName();
    // 权限请求码
    protected int mPermissionsRequestCode = 0;

    /**
     * 请求检查权限
     */
    public void requestCheckPermissions(String[] permissions, int requestCode) {
        if (null == permissions || permissions.length == 0) {
            return;
        }

        mPermissionsRequestCode = requestCode;
        List<String> listPerm = new ArrayList<>();
        for (String permission : permissions) {
            int checkSelfPermission;
            try {
                checkSelfPermission = ContextCompat.checkSelfPermission(this, permission);
            } catch (Exception e) {
                Log.e(TAG, e.getClass().getSimpleName(), e);
                Toast.makeText(this, String.format("获取权限：%s 失败，可能某些功能因此受限！", permission),
                        Toast.LENGTH_LONG).show();
                break;
            }
            if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
                listPerm.add(permission);
            }
        }

        Log.d(TAG, "requestPermissions");
        if (!listPerm.isEmpty()) {
            String[] perms = listPerm.toArray(new String[listPerm.size()]);
            ActivityCompat.requestPermissions(this,
                    perms,
                    requestCode);
        } else {
            onPermissionsGranted(requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == mPermissionsRequestCode) {
            boolean permissionsDenied = false;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    permissionsDenied = true;
                    break;
                }
            }

            if (permissionsDenied) {
                onPermissionsDenied(requestCode);
            } else {
                onPermissionsGranted(requestCode);
            }
            return;
        }
    }

    /**
     * 权限允许，子类可重写
     */
    protected void onPermissionsGranted(int requestCode) {
    }

    /**
     * 权限拒绝，子类可重写
     */
    protected void onPermissionsDenied(int requestCode) {
    }
}
