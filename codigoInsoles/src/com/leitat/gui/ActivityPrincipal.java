package com.leitat.gui;

import com.leitat.R;
import com.leitat.servicio.AtrapaExcepciones;
import com.leitat.servicio.Constantes;
import com.leitat.servicio.Servicio;
import com.leitat.sonido.EfectosSonidoSingleton;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityPrincipal extends Activity {
	private final String TAG = "ActivityPrincipal";

	// Variables de manipulacion de la activity, com.leitat.gui...
	private Button cambiaestadoconexion;
	private EfectosSonidoSingleton efectosonido = EfectosSonidoSingleton
			.getInstance(this);
	public final static String MENSAJE = "com.leitat.insoles.MENSAJE";
	public final static int SELECTORTIPOEFECTO = 0;
	public final static int SELECTOREFECTOAMBIENTAL = 1;
	public final static int SELECTOREFECTOMUSICAL = 2;
	public final static int SELECTORMUSICAFONDO = 3;
	private Handler handlerServicio;

	// Menus

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.opciones_extras, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_esperar_reproduccion:
			AlertDialog.Builder alert2 = new AlertDialog.Builder(this);
			alert2.setTitle(R.string.esperarreproduccion);
			LinearLayout linear2 = new LinearLayout(this);
			linear2.setOrientation(1);			
			final CheckBox esperar = new CheckBox(this);
			esperar.setText(R.string.esperarreproduccion);
			esperar.setChecked(efectosonido.getEsperar());
			linear2.addView(esperar);
			alert2.setView(linear2);
			alert2.setPositiveButton(R.string.botonaplicar,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, final int idbutton) {
							if (esperar.isChecked()) {
								efectosonido.setEsperar(true);
							} else {
								efectosonido.setEsperar(false);
							}
							Toast.makeText(getApplicationContext(),
									R.string.toastaplicado, Toast.LENGTH_LONG)
									.show();

						}
					});
/*
			alert2.setNegativeButton(R.string.botoncancelar,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {

						}
					});
*/
			alert2.show();
			// linear.addView(text);
			break;
			

		case R.id.menu_a_cerca_de:

			final AlertDialog dialog = new AlertDialog.Builder(ActivityPrincipal.this)
					.setTitle(R.string.icono_a_cerca_de)
					.setMessage(R.string.popupacercadeTXT)
					.setPositiveButton(R.string.aceptar, null).show();

			final WindowManager.LayoutParams layoutparams = dialog.getWindow().getAttributes(); // retrieves
																				// the
																				// windows
																				// attributes
			layoutparams.dimAmount = 0.0f; // sets the dimming amount to zero

			dialog.getWindow().setAttributes(layoutparams); // sets the updated windows
													// attributes
			dialog.getWindow().getAttributes().dimAmount = 0.5f;
			dialog.getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_DIM_BEHIND); 
			break;
		case R.id.salir:
			alert2 = new AlertDialog.Builder(this);
			alert2.setTitle(R.string.salir);
			linear2 = new LinearLayout(this);
			linear2.setOrientation(1);
			final TextView confirmacion = new TextView(this);
			confirmacion.setText(R.string.salirConfirmacion);
			linear2.addView(confirmacion);
			alert2.setView(linear2);
			alert2.setPositiveButton(R.string.aceptar,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, final int idbutton) {
							stopService(new Intent(ActivityPrincipal.this, Servicio.class));
							finish();
						}
					});

			alert2.setNegativeButton(R.string.botoncancelar,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {

						}
					});
			alert2.show(); 
			break;
		default:
			break;
		}
		return true;
	}

	// -------------------------------Comunicación con el servicio_piezo_uuid y
	// adaptadores del BTLE--------------------------
	private transient Servicio servicio = null;
	//private BluetoothDevice dispositivoBTLE = null;
	//private BluetoothAdapter adaptadorBTLEmovil = null;
    
	
	private final ServiceConnection conexionConServicio = new ServiceConnection() {
		@Override
		public void onServiceConnected(final ComponentName className,
				final IBinder rawBinder) {
			servicio = ((Servicio.LocalBinder) rawBinder)
					.getServicio();
			Log.d(TAG, "conexionConServicio,onServiceConnected - "
					+ servicio);
			servicio.setHandlerActivityPrincipal(mHandler);
			handlerServicio=servicio.getHandlerServicio();
			inicializaView();
		}
        
		@Override
		public void onServiceDisconnected(final ComponentName classname) {
			
		}
	};
	
	private Handler mHandler = new Handler(new Handler.Callback() {
		@Override
	    public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case Constantes.ID_CONECTADO:
				Log.d(TAG,"handleMessage->State_connected");
				cambiarEstadoGui(Constantes.ID_CONECTADO);
				/**
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// estadoConexion = PERFIL_HRP_ID_CONECTADO;
						cambiarEstadoGui(true);
					}
				});*/
				//getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
				break;
			case Constantes.ID_CONECTANDO:
				cambiarEstadoGui(Constantes.ID_CONECTANDO);
				Log.d(TAG,"handleMessage->STATE_CONNECTING");
				//getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
				// WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
				break;
			case Constantes.ID_DESCONECTANDO:
				Log.d(TAG,"handleMessage->STATE_DISCONNECTING");
				cambiarEstadoGui(Constantes.ID_DESCONECTANDO);
				//getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
				break;
			case Constantes.ID_DESCONECTADO:
				Log.d(TAG,"handleMessage->STATE_DISCONNECTED");
				
				cambiarEstadoGui(Constantes.ID_DESCONECTADO);
				/**
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// estadoConexion = PERFIL_HRP_ID_CONECTADO;
						cambiarEstadoGui(false);
					}
				});*/
				//getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
				break;
				
			case Constantes.ID_ERROR:
				Log.d(TAG,"handleMessage->ID_ERROR");
				
				//cambiarEstadoGui(BluetoothProfile.STATE_DISCONNECTED);
			
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						
						cambiarEstadoGui(BluetoothProfile.STATE_DISCONNECTED);
					}
				});
				//getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
				break;
				
			case Constantes.ID_NOBT:
				Log.d(TAG,"handleMessage->ID_NOBT");
			
				cambiarEstadoGui(BluetoothProfile.STATE_DISCONNECTED);
				Toast.makeText(getApplicationContext(),
						R.string.no_ble_devices, Toast.LENGTH_LONG)
						.show();
				//getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
				break;
				
			case Constantes.ID_VALOR:
				Log.d(TAG, "mHandler.MENS_VALOR");
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
				ImageView estado = ((ImageView) findViewById(R.id.estadoconexionon));
				estado.startAnimation(AnimationUtils.loadAnimation(
						ActivityPrincipal.this, R.anim.rotate_1));
				Bundle data1 = msg.getData();
				if (data1.getBoolean(Constantes.ES_BATERIA)) {
					final int bateria_por = data1.getInt(
							Constantes.PORCENTAJE_BAT, 0);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							TextView bateriatv = (TextView) findViewById(R.id.porcientoBateriaTXT);
							bateriatv.setText("\t" + bateria_por + "%");
						}
					});
				}
				break;
			}
			return true;
		}
	});

	
	
	private int estadoConexion=222;

	private void cambiarEstadoGui(int estadoConexion) {
		this.estadoConexion=estadoConexion;
		switch(this.estadoConexion){
		case Constantes.ID_DESCONECTADO:
			Log.i(TAG, "cambiarEstadoGui - desconectado");
			cambiaestadoconexion.setText(R.string.principalbotonconectar);
			cambiaestadoconexion.setActivated(true);
			cambiaestadoconexion.setAlpha((float) 1);
			((ImageView) findViewById(R.id.estadobluetoothon))
					.setVisibility(View.INVISIBLE);
			((ImageView) findViewById(R.id.estadobluetoothoff))
					.setVisibility(View.VISIBLE);
			((ImageView) findViewById(R.id.estadoconexionon))
					.setVisibility(View.INVISIBLE);
			((ImageView) findViewById(R.id.estadoconexionoff))
					.setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.estadoConexionTXT))
					.setText(R.string.principalestadoconexiontxtDesc);
			((TextView) findViewById(R.id.estadoConexionTXT))
					.setTextColor(Color.parseColor(getString(R.color.granate)));
			break;
		case Constantes.ID_CONECTADO:
			Log.d(TAG, "cambiarEstadoGui - conectado al dispositivo ");
			cambiaestadoconexion
					.setText(R.string.principalbotondesconectar);
			cambiaestadoconexion.setActivated(true);
			cambiaestadoconexion.setAlpha((float) 1);
			((ImageView) findViewById(R.id.estadobluetoothoff))
					.setVisibility(View.INVISIBLE);
			((ImageView) findViewById(R.id.estadobluetoothon))
					.setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.estadoConexionTXT))
					.setTextColor(Color.parseColor(getString(R.color.verde)));
			((TextView) findViewById(R.id.estadoConexionTXT))
					.setText(R.string.principalestadoconexiontxtConec);
			((ImageView) findViewById(R.id.estadoconexionon))
					.setVisibility(View.VISIBLE);
			((ImageView) findViewById(R.id.estadoconexionoff))
					.setVisibility(View.INVISIBLE);
			break;
		case Constantes.ID_DESCONECTANDO:
			Log.i(TAG, "cambiarEstadoGui - desconectando");
			cambiaestadoconexion.setText(R.string.principalbotondesconectar);
			cambiaestadoconexion.setActivated(false);
			cambiaestadoconexion.setAlpha((float) 0.2);
			((ImageView) findViewById(R.id.estadobluetoothon))
					.setVisibility(View.INVISIBLE);
			((ImageView) findViewById(R.id.estadobluetoothoff))
					.setVisibility(View.VISIBLE);
			((ImageView) findViewById(R.id.estadoconexionon))
					.setVisibility(View.INVISIBLE);
			((ImageView) findViewById(R.id.estadoconexionoff))
					.setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.estadoConexionTXT))
					.setText(R.string.principalestadoconexiontxtDesconectando);
			((TextView) findViewById(R.id.estadoConexionTXT))
					.setTextColor(Color.parseColor(getString(R.color.amarillo)));
			break;
		case Constantes.ID_CONECTANDO:
			Log.i(TAG, "cambiarEstadoGui - conectando");
			
			cambiaestadoconexion.setText(R.string.principalbotonconectar);
			cambiaestadoconexion.setActivated(false);
			cambiaestadoconexion.setAlpha((float) 0.2);
			((ImageView) findViewById(R.id.estadobluetoothon))
					.setVisibility(View.INVISIBLE);
			((ImageView) findViewById(R.id.estadobluetoothoff))
					.setVisibility(View.VISIBLE);
			((ImageView) findViewById(R.id.estadoconexionon))
					.setVisibility(View.INVISIBLE);
			((ImageView) findViewById(R.id.estadoconexionoff))
					.setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.estadoConexionTXT))
					.setText(R.string.principalestadoconexiontxtConectando);
			((TextView) findViewById(R.id.estadoConexionTXT))
					.setTextColor(Color.parseColor(getString(R.color.amarillo)));
			break;
		}
	}

	// ---------------------------------- Ciclo de vida Activitys
	// -------------------------------
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.principal);
		Thread.setDefaultUncaughtExceptionHandler(new AtrapaExcepciones());
		Intent bindIntent = new Intent(this, Servicio.class);
		startService(bindIntent);
		bindService(bindIntent, conexionConServicio, Context.BIND_AUTO_CREATE);
		//inicializaView();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		getWindow().getDecorView().findViewById(android.R.id.content).invalidate();
		getWindow().getDecorView().findViewById(android.R.id.content).requestLayout();
		setContentView(R.layout.principal);
	    Log.d(TAG, "Cambiado orientacion");
	}
	
	private void inicializaView(){
		cambiaestadoconexion = (Button) findViewById(R.id.cambiaestadoconexion);
		((TextView) findViewById(R.id.porcientoBateriaTXT)).setText("0%");
		Log.d(TAG,"Definido  handler cambio estado conexion boton");
		((Button) findViewById(R.id.cambiaestadoconexion))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (estadoConexion==Constantes.ID_CONECTADO ||estadoConexion==Constantes.ID_CONECTANDO) {
							Log.d(TAG,"Puslado para desconectar");
							//Constantes.desconectar();
							Message msg = Message.obtain(handlerServicio,
									Constantes.ID_DESCONECTAR);
							msg.sendToTarget();
							/**
							((ImageView) findViewById(R.id.estadobluetoothoff))
									.setVisibility(View.VISIBLE);
							((ImageView) findViewById(R.id.estadobluetoothon))
									.setVisibility(View.INVISIBLE);
							((TextView) findViewById(R.id.estadoConexionTXT))
									.setText(R.string.principalestadoconexiontxtDesc);
							((TextView) findViewById(R.id.estadoConexionTXT)).setTextColor(Color
									.parseColor(getString(R.color.granate)));
							((ImageView) findViewById(R.id.estadoconexionon))
									.setVisibility(View.INVISIBLE);
							((ImageView) findViewById(R.id.estadoconexionoff))
									.setVisibility(View.VISIBLE);
									*/
						} else if(estadoConexion==Constantes.ID_DESCONECTADO ||estadoConexion==Constantes.ID_DESCONECTANDO){
							Log.d(TAG,"Puslado para conectar");
							Message msg = Message.obtain(handlerServicio,
									Constantes.ID_CONECTAR);
							msg.sendToTarget();
							//Constantes.conectarAutomatico();
						} else {Log.d(TAG,"Estado Indetermiado");}

					}
				});

		((Button) findViewById(R.id.btn_opciones))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent myIntent = new Intent(ActivityPrincipal.this,
								ActivityEfectos.class);
						myIntent.putExtra(MENSAJE, SELECTORTIPOEFECTO); // Optional
																		// parameters
						ActivityPrincipal.this.startActivity(myIntent);
					}
				});

		((ImageButton) findViewById(R.id.btn_estado_reproduccion))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (efectosonido.getEstadoReproduccion()) {// esta
																	// reproduciendo
																	// se
																	// muestra
																	// boton
																	// pause
							((ImageButton) findViewById(R.id.btn_estado_reproduccion))
									.setImageResource(R.drawable.btn_estado_reproduccion_pause);
							efectosonido.setReproduccion(false);
						} else {// Reproduccion desactivada switchear entre
								// botones de play
							((ImageButton) findViewById(R.id.btn_estado_reproduccion))
									.setImageResource(R.drawable.btn_estado_reproduccion_play);
							efectosonido.setReproduccion(true);
						}
					}
				});

		// Set the hardware buttons to control the music
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart() servicio_piezo_uuid= " + servicio);
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		//actualizarGui(conectado);
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		Log.d(TAG, "onBackPressed");
		if (estadoConexion==Constantes.ID_CONECTADO) {
			Intent startMain = new Intent(Intent.ACTION_MAIN);
			startMain.addCategory(Intent.CATEGORY_HOME);
			startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(startMain);
		} else {
			new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(String.format(getString(R.string.popup_modal_salir), getString(R.string.nombre_app)))
					.setMessage(
							R.string.popup_dispositivo_desconectado_quieres_salir)
					.setPositiveButton(R.string.popup_si,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									stopService(new Intent(ActivityPrincipal.this, Servicio.class));
									finish();
								}
							}).setNegativeButton(R.string.popup_no, null)
					.show();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopService(new Intent(this, Servicio.class));	
	}
}
