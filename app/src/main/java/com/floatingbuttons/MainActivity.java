package com.floatingbuttons;

import android.app.Activity;
import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
    private WindowManager windowManager;
    private View floatingView;
    private boolean showing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 120, 60, 60);
        layout.setBackgroundColor(0xFF0F0F1A);

        TextView status = new TextView(this);
        status.setText("Floating buttons: INACTIVE");
        status.setTextColor(0xFFFFB74D);
        status.setTextSize(18);
        status.setPadding(0, 0, 0, 40);

        Button startBtn = new Button(this);
        startBtn.setText("Start Floating Buttons");

        Button stopBtn = new Button(this);
        stopBtn.setText("Stop");

        startBtn.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                startActivityForResult(new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())), 1001);
            } else {
                showFloating();
                status.setText("Floating buttons: ACTIVE");
            }
        });

        stopBtn.setOnClickListener(v -> {
            hideFloating();
            status.setText("Floating buttons: INACTIVE");
        });

        layout.addView(status);
        layout.addView(startBtn);
        layout.addView(stopBtn);
        setContentView(layout);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    private void showFloating() {
        if (showing) return;
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setGravity(Gravity.CENTER);
        c.setPadding(16, 16, 16, 16);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#CC1A1A2E"));
        bg.setCornerRadius(40);
        c.setBackground(bg);

        TextView up = makeBtn("▲");
        TextView down = makeBtn("▼");
        up.setOnClickListener(v -> scroll(true));
        down.setOnClickListener(v -> scroll(false));
        c.addView(up); c.addView(down);

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
            WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
            140, WindowManager.LayoutParams.WRAP_CONTENT, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.END;
        p.x = 20; p.y = 300;

        c.setOnTouchListener(new View.OnTouchListener() {
            int ix, iy; float tx, ty; boolean d;
            public boolean onTouch(View v, MotionEvent e) {
                WindowManager.LayoutParams lp = (WindowManager.LayoutParams) floatingView.getLayoutParams();
                if (e.getAction()==0){ix=lp.x;iy=lp.y;tx=e.getRawX();ty=e.getRawY();d=false;}
                else if(e.getAction()==2){int dx=(int)(e.getRawX()-tx),dy=(int)(e.getRawY()-ty);
                    if(Math.abs(dx)>10||Math.abs(dy)>10)d=true;
                    if(d){lp.x=ix-dx;lp.y=iy+dy;windowManager.updateViewLayout(floatingView,lp);}}
                return d;
            }
        });

        floatingView = c;
        windowManager.addView(floatingView, p);
        showing = true;
    }

    private void hideFloating() {
        if (showing && floatingView != null) {
            windowManager.removeView(floatingView);
            showing = false;
        }
    }

    private TextView makeBtn(String t) {
        TextView b = new TextView(this);
        b.setText(t); b.setTextSize(24); b.setTextColor(Color.WHITE);
        b.setGravity(Gravity.CENTER); b.setPadding(16,16,16,16);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(Color.parseColor("#CC4A90D9"));
        b.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(100,100);
        lp.setMargins(0,6,0,6); b.setLayoutParams(lp);
        return b;
    }

    private void scroll(boolean up) {
        new Thread(()->{try{Runtime.getRuntime().exec(new String[]
            {"input","swipe","540",up?"800":"400","540",up?"400":"800","300"});}
            catch(Exception e){e.printStackTrace();}}).start();
    }

    @Override protected void onActivityResult(int req, int res, Intent d) {
        super.onActivityResult(req,res,d);
        if (req==1001 && Settings.canDrawOverlays(this)) showFloating();
    }

    @Override protected void onDestroy() { super.onDestroy(); hideFloating(); }
}
