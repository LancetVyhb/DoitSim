package com.DoIt.Medias;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.DoIt.R;
import com.bumptech.glide.Glide;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ImageViewer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setTheme(R.style.ImageViewerTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        PhotoView photo = findViewById(R.id.photo);
        String image = getIntent().getStringExtra("image");
        Glide.with(this).load(image).into(photo);
        photo.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float v, float v1) { }
        });
    }
}
