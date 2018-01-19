
package jp.co.ricoh.advop.idcardscanprint.logic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.R.bool;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

import jp.co.ricoh.dsdk.util.image.FormatException;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.idcardscanprint.logic.jpeg2pdf.jpeg2pdf;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.attribute.MediaSizeName;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.PaperSize;
import jp.co.ricoh.advop.idcardscanprint.util.Const;
import jp.co.ricoh.advop.idcardscanprint.util.Util;
import jp.co.ricoh.dsdk.util.image.MultipageTiff;

public class JpegProcessManager {

    public static final int ORIENTATION_ORIGINAL = 1;

    public static boolean jpeg2in1(String jpegPath1, String jpegPath2, String dstPath)
            throws FileNotFoundException
    {
        if (jpegPath1 == null || jpegPath2 == null || dstPath == null)
        {
            return false;
        }

        Bitmap bitmap1 = BitmapFactory.decodeFile(jpegPath1);
        if(bitmap1 == null) {
            return false;
        }
        int width = bitmap1.getWidth();
        int height = bitmap1.getHeight();

       // if (height > width) // Portrait image, come from A3 MFP
      //  {
            // Draw jpegPath1
            Bitmap dst = Bitmap.createBitmap(height, width * 2, Config.ARGB_8888);
            Canvas canvas = new Canvas(dst);
            Matrix ct = new Matrix();
            ct.preRotate(90, 0, 0);
            ct.postTranslate(height, 0);
            canvas.drawBitmap(bitmap1, ct, null);
            bitmap1.recycle();

            // Draw jpegPath2
            Bitmap bitmap2 = BitmapFactory.decodeFile(jpegPath2);
            ct.reset();
            ct.preRotate(90, 0, 0);
            ct.postTranslate(height, width);
            canvas.drawBitmap(bitmap2, ct, null);
            bitmap2.recycle();

            // Clear board
            Paint p = new Paint();
            p.setColor(Color.WHITE);
            p.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, 0, height, 50, p);
            canvas.drawRect(0, width * 2 - 50, height, width * 2, p);
            canvas.drawRect(0, 0, 50, width * 2, p);
            canvas.drawRect(height - 50, 0, height, width * 2, p);
            canvas.drawRect(0, width - 50, height, width + 50, p);

            FileOutputStream stream = new FileOutputStream(dstPath);
            dst.compress(CompressFormat.JPEG, 100, stream);
            dst.recycle();
      //  }

//        else
//        { // Landscape image, come from A4 MFP
//
//            // Draw jpegPath1
//            Bitmap dst = Bitmap.createBitmap(width, height * 2, Config.ARGB_8888);
//            Canvas canvas = new Canvas(dst);
//            Matrix ct = new Matrix();
//            ct.preRotate(180, 0, 0);
//            ct.postTranslate(width, height);
//            canvas.drawBitmap(bitmap1, ct, null);
//            bitmap1.recycle();
//
//            // Draw jpegPath2
//            Bitmap bitmap2 = BitmapFactory.decodeFile(jpegPath2);
//            ct.reset();
//            ct.preRotate(180, 0, 0);
//            ct.postTranslate(width, height * 2);
//            canvas.drawBitmap(bitmap2, ct, null);
//            bitmap2.recycle();
//
//            // Clear board
//            Paint p = new Paint();
//            p.setColor(Color.WHITE);
//            p.setStyle(Paint.Style.FILL);
//            canvas.drawRect(0, 0, width, 50, p);
//            canvas.drawRect(0, height * 2 - 50, width, height * 2, p);
//            canvas.drawRect(0, 0, 50, height * 2, p);
//            canvas.drawRect(width - 50, 0, width, height * 2, p);
//            canvas.drawRect(0, height - 50, width, height + 50, p);
//
//            FileOutputStream stream = new FileOutputStream(dstPath);
//            dst.compress(CompressFormat.JPEG, 100, stream);
//            dst.recycle();
//        }

        return true;
    }

    public static Boolean JpegToPdf(String jpgPath, String pdfPath) {        
        File file = new File(jpgPath);
//        width = 210mm / 25.4 * 72 = 595.3 pt
//        height = 297mm / 25.4 * 72 = 842 pt
//        A4 = 297mm * 210 mm
        long pdfW = 595;
        long pdfH = 842;
        
        if (Util.getDefaultDest().equalsIgnoreCase("na")) {//8 1/2×11
            pdfW = 612;
            pdfH = 792;
        } 
        if (file.exists()) {

            jpeg2pdf test = new jpeg2pdf();
            test.start(pdfPath);
            test.setPaperSize(pdfW, pdfH); 
            test.addPageJpeg(jpgPath);
            test.end();
            return true;
        }

        return false;
    }

    public static Boolean JpegToTiff(String jpgPath, String tiffPath) {
        File file = new File(jpgPath);
        if (file.exists()) {
            boolean result = generateSingleTiffFromJpeg(jpgPath, tiffPath, true, ORIENTATION_ORIGINAL);
            return result;
        }

        return false;
        

    }

    public static boolean generateSingleTiffFromJpeg(String jpgFile, String outputTiff,
            boolean isColor, int orientation) {
        if ((jpgFile == null) || (jpgFile.trim().length() == 0)) {
            return false;
        }

        if ((outputTiff == null) || (outputTiff.trim().length() == 0)) {
            return false;
        }

        MultipageTiff multiTiff = new MultipageTiff();
        OutputStream ops = null;
        try {

            multiTiff.addPage(jpgFile);
            ops = new FileOutputStream(new File(outputTiff));
            multiTiff.writeSingleTiff(jpgFile, ops, isColor, orientation);
            return true;
        } catch (FileNotFoundException e) {
            LogC.w(e);
        } catch (IOException e) {
            LogC.w(e);
        } catch (FormatException e) {
            LogC.w(e);
        } finally {
            if (ops != null) {
                try {
                    ops.close();
                } catch (IOException e) {
                    LogC.w(e);
                }
            }

        }
        return false;
    }

}
