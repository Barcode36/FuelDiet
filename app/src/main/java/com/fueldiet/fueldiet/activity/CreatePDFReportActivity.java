package com.fueldiet.fueldiet.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.object.DriveObject;
import com.fueldiet.fueldiet.object.ManufacturerObject;
import com.fueldiet.fueldiet.object.PetrolStationObject;
import com.fueldiet.fueldiet.object.VehicleObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.fueldiet.fueldiet.Utils.toCapitalCaseWords;

public class CreatePDFReportActivity extends AppCompatActivity {

    private static final String TAG = "CreatePDFReportActivity";
    private FuelDietDBHelper dbHelper;
    private Button createPdf;
    private Locale locale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pdf_report);

        dbHelper = new FuelDietDBHelper(getApplicationContext());

        ActionBar toolbar = getSupportActionBar();
        assert toolbar != null;
        toolbar.setTitle(getString(R.string.create_pdf_report));

        createPdf = findViewById(R.id.create_pdf_button);
        createPdf.setOnClickListener(v -> {
           createPDF();
        });

        Configuration configuration = getApplication().getResources().getConfiguration();
        locale = configuration.getLocales().get(0);
    }

    private void createPDF() {

        float pageWidth = 595f;
        float pageHeight = 842f;

        Log.d(TAG, "createPDF: creating new pdf document");

        PdfDocument document = new PdfDocument();
        Paint paint = new Paint();
        Paint vehiclePaint = new Paint();

        vehiclePaint.setTextAlign(Paint.Align.LEFT);
        vehiclePaint.setTypeface(Typeface.DEFAULT_BOLD);
        vehiclePaint.setTextSize(20f);
        vehiclePaint.setColor(Color.BLACK);
        
        Paint textPaint = new Paint();
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setTextSize(10f);
        textPaint.setColor(Color.BLACK);

        //A4 dimensions
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();

        Log.d(TAG, "createPDF: creating header");
        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.fuel_diet_trans_logo);
        Bitmap scaledLogo = Bitmap.createScaledBitmap(logo, 300, 100, false);

        Bitmap logoWithBg = Bitmap.createBitmap(scaledLogo.getWidth(), scaledLogo.getHeight(), scaledLogo.getConfig());
        Canvas canvasLogo = new Canvas(logoWithBg);
        canvasLogo.drawColor(getColor(R.color.colorPrimary));
        canvasLogo.drawBitmap(scaledLogo, 0, 0, null);

        Bitmap bg = Bitmap.createBitmap(595, 140, Bitmap.Config.RGB_565);
        Canvas canvasBg = new Canvas(bg);
        canvasBg.drawColor(getColor(R.color.colorPrimary));

        canvas.drawBitmap(bg, 0f, 0f, paint);
        canvas.drawBitmap(logoWithBg, 147.5f, 20.0f, paint);

        Log.d(TAG, "createPDF: getting all vehicles");
        List<VehicleObject> vehicles = dbHelper.getAllVehicles();

        CustomValue y = new CustomValue(210f);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", locale);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", locale);

        for (VehicleObject vehicle : vehicles) {
            Log.d(TAG, "createPDF: get all drives for vehicle " + vehicle.getMake() + " " + vehicle.getModel());

            Log.d(TAG, "createPDF: writing vehicle model");
            canvas.drawText(String.format("%s %s", vehicle.getMake(), vehicle.getModel()), 80f, 180f, vehiclePaint);

            Log.d(TAG, "createPDF: drawing vehicle logo");
            Bitmap vLogo = getVehicleLogo(vehicle, 50, 30);
            float w = (50f - vLogo.getWidth()) / 2f;
            float h = (30f - vLogo.getHeight()) / 2f;
            canvas.drawBitmap(vLogo, 20f+w, 160f+h, paint);

            drawTableHeader(canvas, y, paint, pageWidth, textPaint);

            /*
            canvas.drawLine(107.6f, y.getValue(), 107.6f, pageHeight,  textPaint);
            canvas.drawLine(195.2f, y.getValue(),195.2f, pageHeight, textPaint);
            canvas.drawLine(253.6f, y.getValue(),253.6f, pageHeight, textPaint);
            canvas.drawLine( 297.4f, y.getValue(),297.4f, pageHeight, textPaint);
            canvas.drawLine(355.8f, y.getValue(),355.8f, pageHeight, textPaint);
            canvas.drawLine( 399.6f, y.getValue(),399.6f, pageHeight, textPaint);
            canvas.drawLine( 487.4f, y.getValue(),487.4f, pageHeight, textPaint);
            */

            List<DriveObject> drives = dbHelper.getAllDrives(vehicle.getId());

            for (DriveObject drive : drives) {

                if (y.getValue()+60 > pageHeight) {
                    //new page
                    Log.d(TAG, "createPDF: new page");
                    document.finishPage(page);
                    page = document.startPage(pageInfo);
                    y.resetValue();
                    canvas = page.getCanvas();
                    drawTableHeader(canvas, y, paint, pageWidth, textPaint);
                }

                if (drive.getPetrolStation().equals("Other")) {
                    canvas.drawBitmap(scaleBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_help_outline_black_24dp),85, 55), 20f, y.getValue()-17, paint);
                } else {
                    String fileName = dbHelper.getPetrolStation(drive.getPetrolStation()).getFileName();
                    File storageDIR = getDir("Images", MODE_PRIVATE);
                    Bitmap pLogo = scaleBitmap(BitmapFactory.decodeFile(storageDIR+"/"+fileName),85, 55);
                    w = (85f - pLogo.getWidth()) / 2f;
                    h = (55f - pLogo.getHeight()) / 2f;
                    canvas.drawBitmap(pLogo, 20f + w, y.getValue() + h, paint);
                }

                //canvas.drawText("Petrol Station", 63.8f, y.getValue(), textPaint);
                canvas.drawText(dateFormat.format(drive.getDate().getTime()), 151.4f, y.getDateValue(), textPaint);
                canvas.drawText(timeFormat.format(drive.getDate().getTime()), 151.4f, y.getTimeValue(), textPaint);
                canvas.drawText(drive.getOdo() + " km", 224.4f, y.getOtherValue(), textPaint);
                canvas.drawText(drive.getTrip() + " km", 275.5f, y.getOtherValue(), textPaint);
                canvas.drawText(drive.getCostPerLitre() + " €/l", 326.6f, y.getOtherValue(), textPaint);
                canvas.drawText(drive.getLitres() + " l", 377.7f, y.getOtherValue(), textPaint);
                canvas.drawText(Utils.calculateConsumption(drive.getTrip(), drive.getLitres()) + " l/100km", 443.4f, y.getOtherValue(), textPaint);
                String note = drive.getNote() == null ? "" : drive.getNote();
                //canvas.drawText(note, 530.5f, y.getValue(), textPaint);

                canvas.drawLine(30, y.getValue()+55f,pageWidth - 30, y.getValue()+56f, paint);

                y.newDriveLine();
            }

        }

        document.finishPage(page);

        String filePath = Environment.getExternalStorageDirectory().getPath()+"/Download/"+"test.pdf";
        File file = new File(filePath);
        try {
            document.writeTo(new FileOutputStream(file));
            Log.d(TAG, "createPDF: pdf created");
        } catch (IOException e) {
            e.printStackTrace();
        }
        document.close();
    }

    private void drawTableHeader(Canvas canvas, CustomValue y, Paint paint, float pageWidth, Paint textPaint) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);

        canvas.drawRect(20f,y.getValue()-10f,pageWidth-20f, y.getValue()+10f, paint);
        canvas.drawText("Petrol Station", 63.8f, y.getValue()+5, textPaint);
        canvas.drawText("Date", 151.4f, y.getValue()+5, textPaint);
        canvas.drawText("ODO", 224.4f, y.getValue()+5, textPaint);
        canvas.drawText("Trip", 275.5f, y.getValue()+5, textPaint);
        canvas.drawText("€/l", 326.6f, y.getValue()+5, textPaint);
        canvas.drawText("Litres", 377.7f, y.getValue()+5, textPaint);
        canvas.drawText("Consumption", 443.4f, y.getValue()+5, textPaint);
        canvas.drawText("Notes", 530.5f, y.getValue()+5, textPaint);
        y.newLine();
    }

    private Bitmap getVehicleLogo(VehicleObject vehicleObject, int maxWidth, int maxHeight) {
        String fileName = vehicleObject.getCustomImg();
        File storageDIR = getDir("Images",MODE_PRIVATE);
        try {
            if (fileName == null) {
                ManufacturerObject mo = MainActivity.manufacturers.get(toCapitalCaseWords(vehicleObject.getMake()));
                if (!mo.isOriginal()){
                    Utils.downloadImage(getResources(), getApplicationContext(), mo);
                }
                int idResource = getResources().getIdentifier(mo.getFileNameModNoType(), "drawable", getPackageName());

                //Bitmap logo = BitmapFactory.decodeResource(getResources(), idResource);
                Bitmap logo = BitmapFactory.decodeFile(storageDIR+"/"+mo.getFileNameMod());
                return scaleBitmap(logo, maxWidth, maxHeight);

            } else {
                Bitmap logo = BitmapFactory.decodeFile(storageDIR+"/"+fileName);
                return scaleBitmap(logo, maxWidth, maxHeight);
            }
        } catch (Exception e){
            Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.ic_help_outline_black_24dp);
            return scaleBitmap(logo, maxWidth, maxHeight);
        }
    }

    private Bitmap scaleBitmap(Bitmap bm, int maxWidth, int maxHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        Log.d("Pictures", "Width and height are " + width + "--" + height);

        float ratioW = (float) width / maxWidth;
        float ratioH = (float) height / maxHeight;

        if (ratioH > ratioW) {
            height = (int)(height / ratioH);
            width = (int)(width / ratioH);
        } else {
            height = (int)(height / ratioW);
            width = (int)(width / ratioW);
        }

        Log.d("Pictures", "after scaling Width and height are " + width + "--" + height);

        bm = Bitmap.createScaledBitmap(bm, width, height, true);
        return bm;
    }
}

class CustomValue {
    private float value;

    CustomValue(float value) {
        this.value = value;
    }

    float getValue() {
        return this.value;
    }

    void newDriveLine() {
        this.value += 60f;
    }

    void newLine() {
        this.value += 20f;
    }

    float getTimeValue() {
        return this.value + 40;
    }

    float getDateValue() {
        return this.value + 20;
    }

    float getOtherValue() {
        return this.value + 30;
    }

    void resetValue() {
        this.value = 40;
    }
}
