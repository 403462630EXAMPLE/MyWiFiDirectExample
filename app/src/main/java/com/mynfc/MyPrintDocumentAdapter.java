package com.mynfc;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.pdf.PrintedPdfDocument;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by rjhy on 14-10-29.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class MyPrintDocumentAdapter extends PrintDocumentAdapter {
    private static final String TAG = "MyPrintDocumentAdapter";
    private Context mContext;
    private PrintedPdfDocument printedPdfDocument;
    public int totalpages = 4;

    public MyPrintDocumentAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
        Log.i(TAG, "onLayout");
        printedPdfDocument = new PrintedPdfDocument(mContext, newAttributes);
        if (cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            Log.i(TAG, "callback.onLayoutCancelled()");
            return ;
        }
//        int pages = computePageCount(newAttributes);
        if (totalpages > 0) {
            PrintDocumentInfo info = new PrintDocumentInfo.Builder("test.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(totalpages)
                    .build();
            Log.i(TAG, "callback.onLayoutFinished()");
            callback.onLayoutFinished(info, true);
        } else {
            callback.onLayoutFailed("Page count calculation failed.");
            Log.i(TAG, "callback.onLayoutFailed()");
        }
    }

    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
        Log.i(TAG, "onWrite--pages_length:" + pages.length + "; pages:" + pages);
        for (int i=0; i<totalpages; i++) {
            if (pageInRange(pages, i)) {

                PdfDocument.Page page = printedPdfDocument.startPage(i);
                if (cancellationSignal.isCanceled()) {
                    callback.onWriteCancelled();
                    printedPdfDocument.close();
                    printedPdfDocument = null;
                    return ;
                }

                drawPage(page);
                printedPdfDocument.finishPage(page);
            }
        }
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(destination.getFileDescriptor());
            printedPdfDocument.writeTo(fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
            callback.onWriteFailed(e.toString());
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            printedPdfDocument.close();
            printedPdfDocument = null;
        }
        callback.onWriteFinished(pages);
    }

    private void drawPage(PdfDocument.Page page) {
        Canvas canvas = page.getCanvas();
        int titleBaseLine = 72;
        int leftMargin = 54;
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(36);
        canvas.drawText("Test Title", titleBaseLine, leftMargin, paint);

        paint.setTextSize(14);
        canvas.drawText("test paragraph", leftMargin, titleBaseLine + 25, paint);

        paint.setColor(Color.BLUE);
        canvas.drawRect(100, 100, 172, 172, paint);
    }

    private int computePageCount(PrintAttributes printAttributes) {
        int itemsPerPages = 4;
        PrintAttributes.MediaSize mediaSize = printAttributes.getMediaSize();
        if (mediaSize.isPortrait()) {
            itemsPerPages = 6;
        }
        int printItemCount = getPrintItemCount();
        return (int) Math.ceil(printItemCount / itemsPerPages);
    }

    private int getPrintItemCount() {
        return 6;
    }

    private boolean pageInRange(PageRange[] pageRanges, int page)
    {
        for (int i = 0; i<pageRanges.length; i++)
        {
            if ((page >= pageRanges[i].getStart()) &&
                    (page <= pageRanges[i].getEnd()))
                return true;
        }
        return false;
    }
}
