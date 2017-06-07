package nl.everlutions.wifichat.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListAdapter;
import android.widget.ListView;

@SuppressLint("SimpleDateFormat")
public class ScreenUtils {

    public static final String TAG = ScreenUtils.class.getSimpleName();

    public static boolean isScreenSizeNormal(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                == Configuration.SCREENLAYOUT_SIZE_NORMAL;
    }

    public static boolean isScreenSizeLarge(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                == Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isScreenSizeXLarge(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                == Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * @param configOrientation use Confirugration.StaticOrientation reference
     */
    public static boolean isOrientation(int configOrientation, Context context) {
        int orientation = context.getResources().getConfiguration().orientation;
        return orientation == configOrientation;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public static float convertDpToPx(int dp, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources()
                .getDisplayMetrics());
    }

    public static void expandView(final View v) {
        v.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        final int targtetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? LayoutParams.WRAP_CONTENT
                        : (int) (targtetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (targtetHeight / v.getContext().getResources().getDisplayMetrics().density) * 3);
        v.startAnimation(a);
    }

    public static void collapseView(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density) * 3);
        v.startAnimation(a);
    }

    public static void lockOrientation(Activity activity) {
        Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int tempOrientation = activity.getResources().getConfiguration().orientation;
        int orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
        switch (tempOrientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                } else {
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                }
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_270) {
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                } else {
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                }
        }
        activity.setRequestedOrientation(orientation);
    }

    /**
     * @param diameter   the size of the circle in DP value not pixels, if smaller than 4 highest source size is taken.
     * @param source     the image size
     * @param padding    inside padding for image and background color, in DP value not pixels
     * @param resColorBg the resource id of the color for the circle bg color, if 0 transparent
     * @return circle bitmap
     */
    public static Bitmap getRoundBitmap(Context ctx, int diameter, final Bitmap source, int resColorBg, int padding) {

        if (diameter < 4) {
            diameter = source.getWidth() >= source.getHeight() ? source.getWidth() : source.getHeight();
        } else {
            diameter = (int) ScreenUtils.convertDpToPx(diameter, ctx);
        }

        if (padding != 0) {
            padding = (int) ScreenUtils.convertDpToPx(padding, ctx);
        }
        int width = source.getWidth();
        int height = source.getHeight();
        double scaleFactor = width >= height ? (double) (diameter - (padding * 2)) / Math
                .sqrt(Math.pow(width, 2) + Math.pow(width, 2))
                : (double) (diameter - (padding * 2)) / Math.sqrt(Math.pow(height, 2) + Math.pow(height, 2));

        Bitmap scaledBitmap = Bitmap
                .createScaledBitmap(source, (int) (scaleFactor * width), (int) (scaleFactor * height), true);

        Bitmap output = Bitmap.createBitmap(diameter,
                diameter, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);

        if (resColorBg != 0) {
            final Paint paintBg = new Paint();
            paintBg.setAntiAlias(true);
            paintBg.setColor(ctx.getResources().getColor(resColorBg));
            canvas.drawRoundRect(new RectF(0, 0, diameter, diameter), diameter / 2, diameter / 2, paintBg);
        }

        int startX = (diameter - scaledBitmap.getWidth()) / 2;
        int startY = (diameter - scaledBitmap.getHeight()) / 2;
        canvas.drawBitmap(scaledBitmap, startX, startY, null);

        source.recycle();
        scaledBitmap.recycle();

        return output;
    }

    public static Bitmap getRoundedCornerBitmap(Context context, Bitmap input, int pixels, int w, int h,
                                                boolean squareTL, boolean squareTR, boolean squareBL, boolean squareBR) {

        Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final float densityMultiplier = context.getResources().getDisplayMetrics().density;

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, w, h);
        final RectF rectF = new RectF(rect);

        //make sure that our rounded corner is scaled appropriately
        final float roundPx = pixels * densityMultiplier;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        //draw rectangles over the corners we want to be square
        if (squareTL) {
            canvas.drawRect(0, 0, w / 2, h / 2, paint);
        }
        if (squareTR) {
            canvas.drawRect(w / 2, 0, w, h / 2, paint);
        }
        if (squareBL) {
            canvas.drawRect(0, h / 2, w / 2, h, paint);
        }
        if (squareBR) {
            canvas.drawRect(w / 2, h / 2, w, h, paint);
        }

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(input, 0, 0, paint);

        return output;
    }

    public static int getScreenWidth(Context context) {
        return getScreenDimensions(context).x;
    }

    public static int getScreenHeight(Context context) {
        return getScreenDimensions(context).y;
    }

    public static int getScreenLongestSide(Context context) {
        Point screenDimensions = getScreenDimensions(context);
        return screenDimensions.y > screenDimensions.x ? screenDimensions.y : screenDimensions.x;
    }

    public static Point getScreenDimensions(Context context) {
        DisplayMetrics metrics = getDisplayMetrics(context);
        return new Point(metrics.widthPixels, metrics.heightPixels);
    }

    private static DisplayMetrics getDisplayMetrics(Context context) {
        Display display = getDefaultDisplay(context);
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics;
    }

    private static Display getDefaultDisplay(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        return display;
    }

    public static void hideKeyboardOnCreate(Activity activity) {
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    public static void hideKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity
                    .getSystemService(activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * Shows the activity in full screen mode, call before super.OnCreate().
     */
    public static void fullScreen(Activity activity) {
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        activity.getWindow()
                .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public static int getColor(Context context, int pine_green_dark) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return context.getResources().getColor(pine_green_dark);
        else {
            return context.getColor(pine_green_dark);
        }
    }

    public static String getHexOfColor(Context context, int colorResourceId, boolean withHastag) {
        int color = getColor(context, colorResourceId);
        String hex = Integer.toHexString(color);
        //remove alpha
        if (hex.length() == 8) {
            hex = hex.substring(2, hex.length());
        }
        return withHastag ? "#" + hex : hex;
    }

    public static float getCenterY(View view) {
        return view.getY() + (view.getHeight() / 2);
    }

    public static float getCenterX(View view) {
        return view.getX() + (view.getWidth() / 2);
    }

    public static String getDensityName(Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        if (density >= 4.0) {
            return "xxxhdpi";
        }
        if (density >= 3.0) {
            return "xxhdpi";
        }
        if (density >= 2.0) {
            return "xhdpi";
        }
        if (density >= 1.5) {
            return "hdpi";
        }
        if (density >= 1.0) {
            return "mdpi";
        }
        return "ldpi";
    }

}
