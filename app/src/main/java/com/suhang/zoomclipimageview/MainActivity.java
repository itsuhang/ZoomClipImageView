package com.suhang.zoomclipimageview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.suhang.zoomclipimageview.views.ZoomClipImageView;

public class MainActivity extends AppCompatActivity {

	private ZoomClipImageView ziv;
	private ImageView iv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ziv = (ZoomClipImageView) findViewById(R.id.ziv);
		iv = (ImageView) findViewById(R.id.iv);
		Button button = (Button) findViewById(R.id.button);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				iv.setImageBitmap(ziv.getClipBitmap());
				ziv.setVisibility(View.INVISIBLE);
			}
		});
	}
}
