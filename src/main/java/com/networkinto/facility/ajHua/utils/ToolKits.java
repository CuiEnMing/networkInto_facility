package com.networkinto.facility.ajHua.utils;

import com.networkinto.facility.ajHua.module.AjHuaModule;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.text.SimpleDateFormat;

/**
 * @author Administrator
 */
@Component
public class ToolKits {

    static NetSDKLib netsdkapi = NetSDKLib.NETSDK_INSTANCE;
    @Resource
    private AjHuaModule ajHuaModule;

    /***************************************************************************************************
     *                          				工具方法       	 										   *
     ***************************************************************************************************/
    public void GetPointerData(Pointer pNativeData, Structure pJavaStu) {
        GetPointerDataToStruct(pNativeData, 0, pJavaStu);
    }

    public void GetPointerDataToStruct(Pointer pNativeData, long OffsetOfpNativeData, Structure pJavaStu) {
        pJavaStu.write();
        Pointer pJavaMem = pJavaStu.getPointer();
        pJavaMem.write(0, pNativeData.getByteArray(OffsetOfpNativeData, pJavaStu.size()), 0,
                pJavaStu.size());
        pJavaStu.read();
    }

    public void GetPointerDataToStructArr(Pointer pNativeData, Structure[] pJavaStuArr) {
        long offset = 0;
        for (int i = 0; i < pJavaStuArr.length; ++i) {
            GetPointerDataToStruct(pNativeData, offset, pJavaStuArr[i]);
            offset += pJavaStuArr[i].size();
        }
    }

    /**
     * 将结构体数组拷贝到内存
     *
     * @param pNativeData
     * @param pJavaStuArr
     */
    public void SetStructArrToPointerData(Structure[] pJavaStuArr, Pointer pNativeData) {
        long offset = 0;
        for (int i = 0; i < pJavaStuArr.length; ++i) {
            SetStructDataToPointer(pJavaStuArr[i], pNativeData, offset);
            offset += pJavaStuArr[i].size();
        }
    }

    public void SetStructDataToPointer(Structure pJavaStu, Pointer pNativeData, long OffsetOfpNativeData) {
        pJavaStu.write();
        Pointer pJavaMem = pJavaStu.getPointer();
        pNativeData.write(OffsetOfpNativeData, pJavaMem.getByteArray(0, pJavaStu.size()), 0, pJavaStu.size());
    }

    public void savePicture(byte[] pBuf, String sDstFile) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(sDstFile);
            fos.write(pBuf);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fos.close();
        }
    }

    public void savePicture(byte[] pBuf, int dwBufOffset, int dwBufSize, String sDstFile) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(sDstFile);
            fos.write(pBuf, dwBufOffset, dwBufSize);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fos.close();
        }
    }

    public void savePicture(Pointer pBuf, int dwBufSize, String sDstFile) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(sDstFile);
            fos.write(pBuf.getByteArray(0, dwBufSize), 0, dwBufSize);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fos.close();
        }
    }

    public void savePicture(Pointer pBuf, int dwBufOffset, int dwBufSize, String sDstFile) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(sDstFile);
            fos.write(pBuf.getByteArray(dwBufOffset, dwBufSize), 0, dwBufSize);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fos.close();
        }
    }

    // 将Pointer值转为byte[]
    public String GetPointerDataToByteArr(Pointer pointer) {
        String str = "";
        if (pointer == null) {
            return str;
        }

        int length = 0;
        byte[] bufferPlace = new byte[1];

        for (int i = 0; i < 2048; i++) {
            pointer.read(i, bufferPlace, 0, 1);
            if (bufferPlace[0] == '\0') {
                length = i;
                break;
            }
        }

        if (length > 0) {
            byte[] buffer = new byte[length];
            pointer.read(0, buffer, 0, length);
            try {
                str = new String(buffer, "GBK").trim();
            } catch (UnsupportedEncodingException e) {
                return str;
            }
        }

        return str;
    }

    /**
     * 获取当前时间
     */
    public String getDate() {
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = simpleDate.format(new java.util.Date()).replace(" ", "_").replace(":", "-");

        return date;
    }

    /**
     * 获取当前时间
     */
    public String getDay() {
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd");
        String date = simpleDate.format(new java.util.Date());
        return date;
    }


    // 限制JTextField 长度，以及内容
    public void limitTextFieldLength(final JTextField jTextField, final int size) {
        jTextField.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                String number = "0123456789" + (char) 8;
                if (number.indexOf(e.getKeyChar()) < 0 || jTextField.getText().trim().length() >= size) {
                    e.consume();
                    return;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }
        });
    }

    // 获取当前窗口
    public JFrame getFrame(ActionEvent e) {
        JButton btn = (JButton) e.getSource();
        JFrame frame = (JFrame) btn.getRootPane().getParent();

        return frame;
    }

    // 获取操作平台信息
    public String getLoadLibrary(String library) {
        String path = "";
        String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith("win")) {
            path = "resources/";
        } else if (os.toLowerCase().startsWith("linux")) {
            path = "";
        }

        return (path + library);
    }

    public String getOsName() {
        String osName = "";
        String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith("win")) {
            osName = "win";
        } else if (os.toLowerCase().startsWith("linux")) {
            osName = "linux";
        }

        return osName;
    }

    /**
     * 读取图片大小
     *
     * @param filePath 图片路径
     * @return
     */
    public long GetFileSize(String filePath) {
        File f = new File(filePath);
        if (f.exists() && f.isFile()) {
            return f.length();
        } else {
            return 0;
        }
    }

    /**
     * 读取图片数据
     *
     * @param file   图片路径
     * @param memory 图片数据缓存
     * @return
     * @throws IOException
     */
    public boolean ReadAllFileToMemory(String file, Memory memory) throws IOException {
        if (memory != Memory.NULL) {
            long fileLen = GetFileSize(file);
            if (fileLen <= 0) {
                return false;
            }
            FileInputStream in = null;
            try {
                File infile = new File(file);
                if (infile.canRead()) {
                    in = new FileInputStream(infile);
                    int buffLen = 1024;
                    byte[] buffer = new byte[buffLen];
                    long currFileLen = 0;
                    int readLen = 0;
                    while (currFileLen < fileLen) {
                        readLen = in.read(buffer);
                        memory.write(currFileLen, buffer, 0, readLen);
                        currFileLen += readLen;
                    }
                    return true;
                } else {
                    System.err.println("Failed to open file %s for read!!!\n");
                    return false;
                }
            } catch (Exception e) {
                System.err.println("Failed to open file %s for read!!!\n");
                e.printStackTrace();
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }

        return false;
    }

    public long readFile(File file) {
        return file.length();
    }

    public Memory read(File file) throws IOException {
        int nPicBufLen = 0;
        Memory memory = null;
        /*
         * 读取本地图片大小
         */
        nPicBufLen = (int) readFile(file);
        System.out.println("长度为：" + nPicBufLen);
        // 读取文件大小失败
        if (nPicBufLen <= 0) {
            System.err.println("读取图片大小失败，请重新选择！");
            return null;
        }
        /*
         * 读取图片缓存
         */
        memory = new Memory(nPicBufLen);   // 申请缓存
        memory.clear();
        return memory;
    }

    class JpgFilter extends FileFilter {
        @Override
        public boolean accept(File f) {
            if (f.getName().toLowerCase().endsWith(".JPG")
                    || f.getName().toLowerCase().endsWith(".jpg")
                    || f.isDirectory()) {
                return true;
            }
            return false;
        }

        @Override
        public String getDescription() {
            return "*.jpg; *.JPG";
        }
    }


    /**
     * 读取图片
     *
     * @return 图片缓存
     * @throws IOException
     */
    public Memory readPictureFile(String picPath) throws IOException {
        int nPicBufLen = 0;
        Memory memory = null;

        /*
         * 读取本地图片大小
         */
        nPicBufLen = (int) GetFileSize(picPath);

        // 读取文件大小失败
        if (nPicBufLen <= 0) {
            System.err.println("读取图片大小失败，请重新选择！");
            return null;
        }

        /*
         * 读取图片缓存
         */
        memory = new Memory(nPicBufLen);   // 申请缓存
        memory.clear();

        if (ReadAllFileToMemory(picPath, memory)) {
            System.err.println("读取图片数据，请重新选择！");
            return null;
        }

        return memory;
    }

    /**
     * 登录设备设备错误状态, 用于界面显示
     */

    public String getErrorCodeShow() {
        return ErrorCode.getErrorCode(ajHuaModule.netsdk.CLIENT_GetLastError());
    }

    /**
     * 获取接口错误码和错误信息，用于打印
     *
     * @return
     */
    public String getErrorCodePrint() {
        return "\n{error code: (0x80000000|" + (ajHuaModule.netsdk.CLIENT_GetLastError() & 0x7fffffff) + ").参考  NetSDKLib.java }"
                + " - {error info:" + ErrorCode.getErrorCode(ajHuaModule.netsdk.CLIENT_GetLastError()) + "}\n";
    }

    /**
     * 字符串拷贝，用于先获取，再设置(src → dst)
     *
     * @param src
     * @param dst
     */
    public static void StringToByteArray(String src, byte[] dst) {
        for (int i = 0; i < dst.length; i++) {
            dst[i] = 0;
        }
        System.arraycopy(src.getBytes(), 0, dst, 0, src.getBytes().length);
    }

}
