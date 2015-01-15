package com.leitat.servicio;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Environment;
import android.util.Log;

public class AtrapaExcepciones implements UncaughtExceptionHandler {

	    @Override
	    public void uncaughtException(Thread t, Throwable e) {
	    	Log.d("Excepciones","alex-recogiendo excepcion");
	    	Date now = new Date();
	    	String dateString = now.toString();
	    	Date parsed = null;
	    	SimpleDateFormat format = 
	            new SimpleDateFormat("InsolesCrash_dd_MM_yy_HH_mm_ss", Locale.US);
	    	try {
				parsed = format.parse(dateString);
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        final Writer result = new StringWriter();
	        final PrintWriter printWriter = new PrintWriter(result);
	        e.printStackTrace(printWriter);
	        String stacktrace = result.toString();
	        printWriter.close();
	        String filename = parsed.toString() + ".log";
            escribeAarchivo(stacktrace, filename);
	    }

	    private void escribeAarchivo(String stacktrace, String filename) {
	    	Log.d("Excepciones","alex-Escribiendo a archivo");
	    	String localPath=Environment.getExternalStorageDirectory().getPath();
	        try {
	            BufferedWriter bufferOutStream = new BufferedWriter(new FileWriter(
	                    localPath + "/" + filename));
	            bufferOutStream.write(stacktrace);
	            bufferOutStream.flush();
	            bufferOutStream.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
}
