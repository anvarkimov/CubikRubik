package uz.kubyechuvchi;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.*;

public class MainActivity extends Activity {
    private WebView web;
    private ValueCallback<Uri[]> cb;
    private Uri camUri;
    private static final int REQ = 7;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        web.setOverScrollMode(View.OVER_SCROLL_NEVER);
        web.setLongClickable(false);
        web.setOnLongClickListener(v -> true);
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView w, ValueCallback<Uri[]> cbk, FileChooserParams p) {
                if (cb != null) cb.onReceiveValue(null);
                cb = cbk;
                Intent pick = new Intent(Intent.ACTION_GET_CONTENT);
                pick.addCategory(Intent.CATEGORY_OPENABLE);
                pick.setType("image/*");
                Intent chooser;
                if (p.isCaptureEnabled()) {
                    ContentValues v = new ContentValues();
                    v.put(MediaStore.Images.Media.DISPLAY_NAME, "kub_" + System.currentTimeMillis() + ".jpg");
                    v.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    camUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v);
                    Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cam.putExtra(MediaStore.EXTRA_OUTPUT, camUri);
                    chooser = cam;
                } else {
                    camUri = null;
                    chooser = Intent.createChooser(pick, "Расм танланг");
                }
                try { startActivityForResult(chooser, REQ); }
                catch (Exception e) { cb.onReceiveValue(null); cb = null; return false; }
                return true;
            }
        });
        web.loadUrl("file:///android_asset/index.html");
    }

    @Override protected void onActivityResult(int rq, int rs, Intent d) {
        if (rq != REQ || cb == null) { super.onActivityResult(rq, rs, d); return; }
        Uri[] r = null;
        if (rs == RESULT_OK) {
            if (camUri != null) r = new Uri[]{camUri};
            else if (d != null && d.getData() != null) r = new Uri[]{d.getData()};
        }
        cb.onReceiveValue(r);
        cb = null; camUri = null;
    }

    @Override public void onBackPressed() { moveTaskToBack(true); }
    @Override protected void onPause() { super.onPause(); web.onPause(); }
    @Override protected void onResume() { super.onResume(); web.onResume(); }
}
