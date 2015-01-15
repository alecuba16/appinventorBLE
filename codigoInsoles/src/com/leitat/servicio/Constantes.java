package com.leitat.servicio;

import java.util.UUID;

public final class Constantes {
	public transient final static UUID UUID_SERVICIO = UUID
	.fromString("0000180d-0000-1000-8000-00805f9b34fb");
public transient final static UUID UUID_CARACT = UUID
	.fromString("00002a37-0000-1000-8000-00805f9b34fb");
public transient final static UUID UUID_CONFIG_DESC = UUID
	.fromString("00002902-0000-1000-8000-00805f9b34fb");

public final static int ID_NOBT = 6;
public final static int ID_CONECTAR = 1;
public final static int ID_CONECTANDO = 11;
public final static int ID_CONECTADO = 111;
public final static int ID_DESCONECTAR = 2;
public final static int ID_DESCONECTANDO = 22;
public final static int ID_DESCONECTADO = 222;
public final static int ID_VALOR = 4;
public final static int ID_ENCONTRADO = 5;
public final static int ID_ERROR = -11;

// Dispositivo
public static final String NOMBRE_PLACA = "ACUSHOES";


/** Source of device entries in the device list */
// public static final int DEVICE_SOURCE_SCAN = 0;

/** Intent extras */
public final static String EXTRA_RSSI = "RSSI";
public final static String ES_BATERIA = "com.leitat.insoles.Servicio.es_bateria ";
public final static String PORCENTAJE_BAT = "com.leitat.insoles.Servicio.bateria_porcentaje";
public final static int TIMEOUT_SEG=10;

 private Constantes(){
	 throw new AssertionError();
 }
}
