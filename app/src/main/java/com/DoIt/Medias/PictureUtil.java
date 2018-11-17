package com.DoIt.Medias;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.UUID;

public class PictureUtil {
    /**
     * 获取修改后的图片
     * @param context 上下文
     * @param uri 图片资源本地地址
     */
    public static File getPictureFile(Context context,Uri uri) {
        //使用随机字符为图片文件命名
        String name = UUID.randomUUID().toString() + ".jpeg";
        ContentResolver resolver = context.getContentResolver();
        BitmapFactory.Options op = new BitmapFactory.Options();
        Matrix matrix = new Matrix();
        Bitmap bm, bmp = null;
        float scaleWidth, scaleHeight;
        int width, height;
        try {
            op.inJustDecodeBounds = true;
            //得到图片的原始大小
            BitmapFactory.decodeStream(resolver.openInputStream(uri), null, op);
            width = op.outWidth;
            height = op.outHeight;
            //获取屏幕高宽度，根据原始图片大小计算图片大小修改比例,尽量保持宽度
            scaleWidth = (float)(context.getResources().getDisplayMetrics().widthPixels / width);
            scaleHeight = (float)(context.getResources().getDisplayMetrics().heightPixels / height);
            if (scaleHeight > scaleWidth)
                matrix.postScale((float)(scaleWidth * 0.7), (float)(scaleWidth * 0.7));
            else matrix.postScale((float)(scaleHeight * 0.7), (float)(scaleHeight * 0.7));
            //获取原始图片
            op.inJustDecodeBounds = false;
            bm = BitmapFactory.decodeStream(resolver.openInputStream(uri), null, op);
            //用原始图片生成经过修改后的图片
            if (bm != null)
                bmp = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        } catch (FileNotFoundException e) {
            Toast.makeText(context, "获取图片失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (bmp != null) {
            try {
                //生成修改过后的图片文件
                File outputFile = new File(context.getFilesDir().getPath() + name);
                if (!outputFile.exists() && outputFile.createNewFile()) {
                    FileOutputStream output = new FileOutputStream(outputFile.getPath());
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, output);//生成文件
                    if (outputFile.exists()) return outputFile;
                }
            } catch (Exception e) {
                Toast.makeText(context, "写入失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        return null;
    }
    /**
     * 剪切图片
     * @param imageFile 剪切后的头像所将要存储的文件
     * @param selectedImage 要剪切的图片的位置
     */
    public static Intent photoClip(File imageFile, Uri selectedImage) {
        // 调用系统中自带的图片剪裁
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(selectedImage, "image/*");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));//设置输出文件地址
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());//设置输出文件格式
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("return-data", false);
        //true则直接返回bitmap,false则把裁剪后的文件输出到指定文件中
        return intent;
    }
}
