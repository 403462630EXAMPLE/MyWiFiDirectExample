package com.mynfc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.support.v4.print.PrintHelper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;


public class MyActivity extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = "MyActivity";

    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;
    private Button button6;
    private ImageView imageView;
    private VideoView videoView;
    private SurfaceView surfaceView;
    private MediaController mediaController;
    private SurfaceHolder surfaceHolder;

    private String imagePath;
    private String videoPath;
    private List<Camera.Size> mSupportedPreviewSizes;

    private Camera camera;
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        button5 = (Button) findViewById(R.id.button5);
        button6 = (Button) findViewById(R.id.button6);

        imageView = (ImageView) findViewById(R.id.image1);
        videoView = (VideoView) findViewById(R.id.video1);
        surfaceView = (SurfaceView) findViewById(R.id.surface1);
//        mediaController = new MediaController(this);
//        videoView.setMediaController(mediaController);
//        mediaController.setMediaPlayer(videoView);
//        mediaController.setAnchorView(videoView);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(createFile()));
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, 1);
                }
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(createVideoFile()));
                if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takeVideoIntent, 2);
                }
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplication(), "surface----init", Toast.LENGTH_SHORT).show();
                camera.startPreview();
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplication(), "printHelp----onClick", Toast.LENGTH_SHORT).show();
                PrintHelper printHelper = new PrintHelper(MyActivity.this);
                printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
                Bitmap bitMap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
                printHelper.printBitmap("image_print", bitMap);
            }
        });

        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doWebViewPrint();
            }
        });

        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doPrint();
            }
        });

        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, final Camera camera) {
                        //将图片保存至相册
                        ContentResolver resolver = getContentResolver();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        MediaStore.Images.Media.insertImage(resolver, bitmap, "t", "des");
                        //拍照后重新开始预览
                        camera.startPreview();
                    }
                });
            }
        });
        initSurfaceView();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void doPrint() {
        PrintManager printManager = (PrintManager) getSystemService(PRINT_SERVICE);
        printManager.print("own_print", new MyPrintDocumentAdapter(this), null);
    }

    private void doWebViewPrint() {
        WebView webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                createWebPrintJob(view);
                //可以被垃圾回收器回收了
                mWebView = null;
            }
        });

        String htmlDocument = "<html><body><h1>Test Content</h1><p>Testing, " +
                "testing, testing...</p></body></html>";
        webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);
//        webView.loadUrl("http://www.baidu.com");
        //维护webView实例，确保在执行打印任务之前不会被垃圾回收器回收
        mWebView = webView;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void createWebPrintJob(WebView webView) {
        PrintManager printManager = (PrintManager) getSystemService(PRINT_SERVICE);
        PrintDocumentAdapter printDocumentAdapter = webView.createPrintDocumentAdapter();
        PrintJob printJob = printManager.print("html_print", printDocumentAdapter, null);

    }


    private void initSurfaceView() {
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void setCameraPictureFormats() {
        Camera.Parameters parameters = camera.getParameters();
//        parameters.getSupportedPictureFormats();
        parameters.setPictureFormat(PixelFormat.JPEG);
        camera.setParameters(parameters);
    }

    private void setCameraColorEffects() {
        Camera.Parameters parameters = camera.getParameters();
        List<String> colorEffects = parameters.getSupportedColorEffects();
        for (String color: colorEffects) {
            if (color.equals(Camera.Parameters.EFFECT_SOLARIZE)) { //是否支持过度曝光效果
                parameters.setColorEffect(Camera.Parameters.EFFECT_SOLARIZE);
                break;
            }
        }
        camera.setParameters(parameters);
    }

    private void setCameraFocusModes() {
        Camera.Parameters parameters = camera.getParameters();
        List<String> focusModes = parameters.getSupportedFocusModes();
        for (String focus: focusModes) {
            if (focus.equals(Camera.Parameters.FOCUS_MODE_AUTO)) { //是否支持自动对焦
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                break;
            }
        }
        camera.setParameters(parameters);
    }

    private void setCameraPreviewSize() {
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        for (Camera.Size size: sizes) {
            int width = size.width;
            int height = size.height;
            Log.i(TAG, "width:" + width + ", height:" + height);
            if (width == 240) {
                parameters.setPreviewSize(width, height);
            }
        }
        camera.setParameters(parameters);
    }

    private boolean cameraOpen() {
        cameraStop();
        camera = Camera.open(1);
        return camera != null;
    }

    private void cameraStop() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private File createFile() {
        File publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(publicDir, "JPEG_"+new Date().getTime()+".jpg");
        imagePath = imageFile.getAbsolutePath();
        Log.i(TAG, imagePath);
        return imageFile;
    }
    private File createVideoFile() {
        File publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        File videoFile = new File(publicDir, "VIDEO_"+new Date().getTime()+".mp4");
        videoPath = videoFile.getAbsolutePath();
        Log.i(TAG, videoPath);
        return videoFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
//            Bundle bundle = data.getExtras();
            Uri uri = Uri.fromFile(new File(imagePath));
            imageView.setImageURI(uri);
            galleryAddPic(imagePath);
        } if (requestCode == 2 && resultCode == RESULT_OK) {
            Log.i(TAG, "video_result:" + data.getData());
//            videoView.setVideoURI(data.getData());
            videoView.setVideoURI(Uri.fromFile(new File(videoPath)));
            videoView.start();
            galleryAddPic(videoPath);
        }
    }

    private void galleryAddPic(String filePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(filePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        boolean isOpen = cameraOpen();
        if (isOpen) {
            try {
//                setCameraColorEffects();
//                setCameraFocusModes();
//                setCameraPictureFormats();
//                setCameraPreviewSize();

                Camera.Parameters params = camera.getParameters();
                params.setJpegQuality(80);  // 设置照片的质量
                params.setPictureSize(1024, 768);
//                params.setPreviewFrameRate(5);  // 预览帧率
                camera.setParameters(params); // 将参数设置给相机

                camera.setPreviewDisplay(surfaceHolder);
//                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged");
        camera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        cameraStop();
    }
}
