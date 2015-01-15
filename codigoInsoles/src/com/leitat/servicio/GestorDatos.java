package com.leitat.servicio;

import com.leitat.sonido.EfectosSonidoSingleton;
import android.os.Bundle;
import android.util.Log;

public class GestorDatos {
	private transient final String TAG = GestorDatos.class
	.getSimpleName();
	private transient final Servicio servicio;
 public GestorDatos(final Servicio servicio){
	 this.servicio=servicio;
 }
//------ Calculo Baterias --------------------
	private static final int RESOLUCION_ADC = 8192;
	private static final int VREF_ADC = 3300;// vref en mv.
	private static final int DIVISOR_ENTRADA = 1;// Factor dimensionar entrada
													// del adc.
	private static final int VMAX_BATERIA = 4200;// Voltaje máximo de bateria
													// para sacar el % en forma
													// lineal.

	private int calculaMilivoltsBateria(final int bateriaint) {
		return ((bateriaint * VREF_ADC) / RESOLUCION_ADC) * DIVISOR_ENTRADA;
	}

	private int calculaPorcientoBateria(final int bateriamv) {
		return ((bateriamv) * 100) / VMAX_BATERIA;
	}
	
 public Bundle tramaAGui(final byte[] arraybytes){
	 final Bundle mBundle = new Bundle();
	 final StringBuilder mensaje_debug = new StringBuilder();
     mensaje_debug.append("tramaAGui -");
	 if (arraybytes[1] == 0) {// 0 es un paquete con solo info de bateria
			mBundle.putBoolean(Constantes.ES_BATERIA, true);
			int bateriamv;
			if ((arraybytes[0] & 0x01) == 1) {//Uint8
				mensaje_debug.append(" FormatoUINT8");
				bateriamv = calculaMilivoltsBateria(((arraybytes[2] << 8) & 0xFF00)
						| (arraybytes[3] & 0x00FF));
			} else {
				mensaje_debug.append(" FormatoUINT16");
				bateriamv = calculaMilivoltsBateria(arraybytes[2]);
			}
			final int bateriapor = calculaPorcientoBateria(bateriamv);
			mensaje_debug.append(" Bateria(" + bateriapor + "%");
			// mBundle.putInt(intent_bateria_mv,bateriamv);
			mBundle.putInt(Constantes.PORCENTAJE_BAT, bateriapor);
		} else {
			mBundle.putBoolean(Constantes.ES_BATERIA, false);
			EfectosSonidoSingleton.getInstance(servicio).reproducirEfectoDeSonido(arraybytes[1]);
			mensaje_debug.append(tramaByteAstr(arraybytes,
					arraybytes.length));
			mensaje_debug.append(",");
		}
	 Log.d(TAG, mensaje_debug.toString());
	return mBundle;
 } 
 private String tramaByteAstr(final byte[] trama, final int longitud) {
		final StringBuilder temp = new StringBuilder();
		for (int i = 0; i < longitud; i++) {
			temp.append(String.format("%2x", trama[i] & 0xff));
		}
		return temp.toString();
	}
}
