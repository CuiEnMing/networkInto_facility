package com.networkinto.facility.opencv;

import com.networkinto.facility.common.constant.IConst;
import lombok.extern.log4j.Log4j2;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.stereotype.Component;

/**
 * @author cuiEnMing
 * @date 2021/5/17 17:44
 */
@Log4j2
@Component
public class OpencvFace {
    private CascadeClassifier faceDetector;

    /**
     * 构造函数，加载默认模型文件
     */
    public OpencvFace() {
        faceDetector = new CascadeClassifier(IConst.openCv.DEFAULT_FACE_MODEL_PATH.getName());
    }

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public byte[] buttonFace(Mat inMat) {
        if (null == faceDetector || faceDetector.empty()) {
            System.out.println("加载模型文件失败: " + IConst.openCv.DEFAULT_FACE_MODEL_PATH.getName());
        }
        Mat grey = new Mat();
        ImageUtil.gray(inMat, grey);

        Mat gsBlur = new Mat();
        ImageUtil.gaussianBlur(grey, gsBlur);
        // 识别结果存储对象 // Rect矩形类
        MatOfRect faceDetections = new MatOfRect();
        // 识别人脸
        faceDetector.detectMultiScale(gsBlur, faceDetections);
        log.info(String.format("识别出 %s 张人脸", faceDetections.toArray().length));
        byte[] bates = new byte[8];
        // 在识别到的人脸部位，描框
        for (Rect rect : faceDetections.toArray()) {
            Mat sub = inMat.submat(rect);
            Mat mat = new Mat();
            Size size = new Size(1000, 1000);
            //将人脸进行截图并保存
            Imgproc.resize(sub, mat, size);
            Imgcodecs.imwrite("D:/FaceDetect/temp//test1" + ".jpg", mat);
            MatOfByte matb = new MatOfByte(mat);
            bates = matb.toArray();

        }
        return bates;
    }
}
