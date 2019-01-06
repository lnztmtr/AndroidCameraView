package com.cjt2325.cameralibrary.util;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：1.1.4
 * 创建日期：2017/4/25
 * 描    述：
 * =====================================
 */
public class CameraParamUtil {
    private static final String TAG = "JCameraView";
    private CameraSizeComparator sizeComparator = new CameraSizeComparator();
    private static CameraParamUtil cameraParamUtil = null;

    private CameraParamUtil() {

    }

    public static CameraParamUtil getInstance() {
        if (cameraParamUtil == null) {
            cameraParamUtil = new CameraParamUtil();
            return cameraParamUtil;
        } else {
            return cameraParamUtil;
        }
    }

    /**
     * 为了让预览的时候图片不变形，预览的size需要和预览view的宽高比匹配，最好有一边尺寸能够相近
     *
     * @param list 可预览的屏幕尺寸--返回的数据有可能是正序的也有可能是倒序的。
     *             为了方便处理我们要先对其进行排序。并且，
     *             因为默认摄像头方向是水平的，我们在使用的时候要旋转90度，
     *             所以这里的width实际上是高度，heigth实际上是宽度
     *             PreviewSizes: width:640    height:480
     *             PreviewSizes: width:1920    height:1080
     * @param targetWidth 期望的最小宽度--使用的时候传预览view的最大边
     * @param rate 宽高比，横竖屏下都应该为>1的值
     * @return
     */
    public Camera.Size getPreviewSize(List<Camera.Size> list, int targetWidth, float rate) {
        double minDiff = Double.MAX_VALUE;
        Camera.Size optimalSize = null;
        Collections.sort(list, sizeComparator);
        for (Camera.Size s : list) {
            if ( equalRate(s, rate)) {
                if (Math.abs(s.width - targetWidth) < minDiff) {
                    optimalSize = s;
                    minDiff = Math.abs(s.width - targetWidth);
                }
            }
        }
        if (optimalSize == null) {
            return getBestSize(list, rate);
        } else {
            return optimalSize;
        }
    }
    /**
     * 为了让预览的时候图片不变形，预览的size需要和预览view的宽高比匹配，最好有一边尺寸能够相近
     *
     * @param list 可拍照的图片尺寸--返回的数据有可能是正序的也有可能是倒序的。
     *             为了方便处理我们要先对其进行排序。并且，
     *             因为默认摄像头方向是水平的，我们在使用的时候要旋转90度，
     *             所以这里的width实际上是高度，heigth实际上是宽度
     *             PictureSizes: width4032   height:3024
     *             PictureSizes: width2560   height:1920
     *
     *             注意：可拍照的图片尺寸范围会远远大于可预览的图片尺寸
     *
     * @param targetWidth 期望的最小宽度--使用的时候传想要的图片分辨率的最大边
     * @param rate 宽高比，横竖屏下都应该为>1的值
     * @return
     */
    public Camera.Size getPictureSize(List<Camera.Size> list, int targetWidth, float rate) {
        Collections.sort(list, sizeComparator);
        int i = 0;
        for (Camera.Size s : list) {
            if ((s.width >= targetWidth) && equalRate(s, rate)) {
                Log.i(TAG, "MakeSure Picture :w = " + s.width + " h = " + s.height);
                break;
            }
            i++;
        }
        if (i == list.size()) {
            return getBestSize(list, rate);
        } else {
            return list.get(i);
        }
    }

    private Camera.Size getBestSize(List<Camera.Size> list, float rate) {
        float previewDisparity = 100;
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            Camera.Size cur = list.get(i);
            float prop = (float) cur.width / (float) cur.height;
            if (Math.abs(rate - prop) < previewDisparity) {
                previewDisparity = Math.abs(rate - prop);
                index = i;
            }
        }
        return list.get(index);
    }


    private boolean equalRate(Camera.Size s, float rate) {
        float r = (float) (s.width) / (float) (s.height);
        return Math.abs(r - rate) <= 0.2;
    }

    public boolean isSupportedFocusMode(List<String> focusList, String focusMode) {
        for (int i = 0; i < focusList.size(); i++) {
            if (focusMode.equals(focusList.get(i))) {
                Log.i(TAG, "FocusMode supported " + focusMode);
                return true;
            }
        }
        Log.i(TAG, "FocusMode not supported " + focusMode);
        return false;
    }

    public boolean isSupportedPictureFormats(List<Integer> supportedPictureFormats, int jpeg) {
        for (int i = 0; i < supportedPictureFormats.size(); i++) {
            if (jpeg == supportedPictureFormats.get(i)) {
                Log.i(TAG, "Formats supported " + jpeg);
                return true;
            }
        }
        Log.i(TAG, "Formats not supported " + jpeg);
        return false;
    }

    private class CameraSizeComparator implements Comparator<Camera.Size> {
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width > rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }

    }

    public int getCameraDisplayOrientation(Context context, int cameraId) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int rotation = wm.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;   // compensate the mirror
        } else {
            // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }
}
