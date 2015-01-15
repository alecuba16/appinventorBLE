package com.leitat.gui;

import com.leitat.sonido.EfectosSonidoSingleton;
import com.leitat.R;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityEfectos extends Activity {
	private static final String TAG = "ActivityEfectos";
	private EfectosSonidoSingleton efectosonido = EfectosSonidoSingleton
			.getInstance(this);
	private int selector;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.efectos);
		((Button) findViewById(R.id.botonaplicar))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// Poner variable de cambio.
						Toast msg = Toast.makeText(ActivityEfectos.this,
								getText(R.string.toastaplicado),
								Toast.LENGTH_SHORT);
						msg.show();
						aplicadoCambio = true;
					}
				});

		// Get the message from the intent
		Intent intent = getIntent();
		int mensaje = intent.getIntExtra(ActivityPrincipal.MENSAJE, 0);
		switch (mensaje) {
		case ActivityPrincipal.SELECTORTIPOEFECTO:
			// selector=ActivityPrincipal.SELECTORTIPOEFECTO;
			tipoSelecionadoAntes = efectosonido.getTipoEfecto();
			pintaSelectorTipoEfecto();
			break;
		case ActivityPrincipal.SELECTOREFECTOAMBIENTAL:
			// selector=ActivityPrincipal.SELECTOREFECTOAMBIENTAL;
			efectoSelecionadoAntes = efectosonido.getEfectoAmbiental();
			pintaSelectorEfecto(efectosonido.EFECTOAMBIENTAL);
			break;
		case ActivityPrincipal.SELECTOREFECTOMUSICAL:
			// selector=ActivityPrincipal.SELECTOREFECTOMUSICAL;
			efectoSelecionadoAntes = efectosonido.getEfectoMusical();
			pintaSelectorEfecto(efectosonido.EFECTOMUSICAL);
			break;
		case ActivityPrincipal.SELECTORMUSICAFONDO:
			// selector=ActivityPrincipal.SELECTORMUSICAFONDO;
			efectoSelecionadoAntes = efectosonido.getMusicaFondo();
			pintaSelectorEfecto(efectosonido.MUSICAAMBIENTE);
			break;
		default:
			break;
		}
	}

	// Guardar el estado actual para si se aplica o no.
	private boolean aplicadoCambio = false;
	private int tipoSelecionadoAntes = efectosonido.getTipoEfecto();
	private int efectoSelecionadoAntes;

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart()");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop");
	}

	@Override
	public void onBackPressed() {
		Log.d(TAG, "onBackPressed");
		// Preguntar si ha aplicado.
		if (!aplicadoCambio) {
			switch (selector) {
			case ActivityPrincipal.SELECTORTIPOEFECTO:
				efectosonido.setTipoEfecto(tipoSelecionadoAntes);
				break;
			case ActivityPrincipal.SELECTOREFECTOAMBIENTAL:
				efectosonido.setEfectoAmbiental(efectoSelecionadoAntes);
				break;
			case ActivityPrincipal.SELECTOREFECTOMUSICAL:
				efectosonido.setEfectoMusical(efectoSelecionadoAntes);
				break;
			case ActivityPrincipal.SELECTORMUSICAFONDO:
				efectosonido.setEfectoMusical(efectoSelecionadoAntes);
				break;
			default:
				break;
			}
		}
		finish();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");

		super.onDestroy();
	}

	private void pintaSelectorEfecto(final int efecto) {
		LinearLayout contenedor = (LinearLayout) findViewById(R.id.contenedor);
		LinearLayout.LayoutParams margenentreelementos = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		margenentreelementos
				.setMargins(
						(int) getResources().getDimension(
								R.dimen.margenEntreElementos),
						0,
						(int) getResources().getDimension(
								R.dimen.margenEntreElementos), 0);
		LinearLayout.LayoutParams sinmargen = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		sinmargen.setMargins(0, 0, 0, 0);
		LinearLayout.LayoutParams margensuperior = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		margensuperior
				.setMargins(
						0,
						(int) getResources().getDimension(
								R.dimen.margenEntreElementos), 0, 0);

		LinearLayout layout1 = new LinearLayout(this);
		layout1.setOrientation(LinearLayout.VERTICAL);
		layout1.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		layout1.setBackground(null);
		layout1.setLayoutParams(sinmargen);
		LinearLayout temp = null;
		final RadioButton[] rb = new RadioButton[efectosonido
				.getEfectoNumeroElementos(efecto)];
		final ImageView[] iv = new ImageView[efectosonido
				.getEfectoNumeroElementos(efecto)];
		final TextView[] tv = new TextView[efectosonido
				.getEfectoNumeroElementos(efecto)];
		int[] res;
		int color = getResources().getColor(R.color.negro);
		for (int i = 0; i < efectosonido.getEfectoNumeroElementos(efecto); i++) {
			// Recogemos los datos
			res = efectosonido.getEfecto(i, efecto);
			// Creamos el layout horizontal donde meter los elementos
			temp = new LinearLayout(this);
			temp.setOrientation(LinearLayout.HORIZONTAL);
			temp.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
			temp.setBackground(null);
			temp.setLayoutParams(margensuperior);
			// Creamos los elementos
			rb[i] = new RadioButton(this);
			tv[i] = new TextView(this);
			tv[i].setTextColor(color);
			tv[i].setText(res[1]);
			iv[i] = new ImageView(this);
			iv[i].setImageResource(res[2]);
			iv[i].setLayoutParams(margenentreelementos);
			if (res[0] == 1) {
				rb[i].setChecked(true);
				rb[i].setAlpha(1);
				tv[i].setAlpha(1);
				iv[i].setAlpha((float) 1);
			} else {
				rb[i].setChecked(false);
				rb[i].setAlpha((float) 0.5);
				tv[i].setAlpha((float) 0.5);
				iv[i].setAlpha((float) 0.5);
			}
			// Eventos onclick
			rb[i].setId(i);
			rb[i].setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					efectosonido.setEfecto(efecto, (v.getId() + 1));
					Log.d(TAG, "Presionado:" + (v.getId() + 1));

					for (int j = 0; j < efectosonido
							.getEfectoNumeroElementos(efecto); j++) {
						if (j != v.getId()) {
							rb[j].setChecked(false);
							rb[j].setAlpha((float) 0.5);
							iv[j].setAlpha((float) 0.5);
							tv[j].setAlpha((float) 0.5);
						} else {

							rb[j].setChecked(true);
							rb[j].setAlpha(1);
							iv[j].setAlpha((float) 1);
							tv[j].setAlpha(1);
						}
					}
				}
			});

			// Finalmente los añadimos al view
			temp.addView(rb[i]);
			temp.addView(iv[i]);
			temp.addView(tv[i]);
			layout1.addView(temp);

		}
		contenedor.addView(layout1);
	}

	private void pintaSelectorTipoEfecto() {
		LinearLayout contenedor = (LinearLayout) findViewById(R.id.contenedor);
		LinearLayout.LayoutParams margenElementos = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		margenElementos
				.setMargins(
						(int) getResources().getDimension(
								R.dimen.margenEntreElementos),
						0,
						(int) getResources().getDimension(
								R.dimen.margenEntreElementos), 0);
		LinearLayout.LayoutParams margenizquierdo = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		margenizquierdo
				.setMargins(
						(int) getResources().getDimension(
								R.dimen.margenEntreElementos), 0, 0, 0);

		LinearLayout.LayoutParams margensuperior = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		margensuperior
				.setMargins(
						0,
						(int) getResources().getDimension(
								R.dimen.margenEntreElementos), 0, 0);

		LinearLayout.LayoutParams margensuperiorlayout = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		margensuperiorlayout.setMargins(0,
				(int) getResources().getDimension(R.dimen.margenEntreLayouts),
				0, 0);

		LinearLayout layoutSimulacionAmbiental1 = new LinearLayout(this);
		layoutSimulacionAmbiental1.setOrientation(LinearLayout.HORIZONTAL);
		layoutSimulacionAmbiental1.setGravity(Gravity.CENTER_HORIZONTAL
				| Gravity.CENTER_VERTICAL);
		layoutSimulacionAmbiental1.setBackground(null);
		float alfa = (float) 0.5;
		int color = getResources().getColor(R.color.negro);
		final RadioButton rb1 = new RadioButton(this);
		if (efectosonido.getTipoEfecto() == efectosonido.TIPOEFECTOSIMULACIONAMBIENTAL) {
			rb1.setChecked(true);
			alfa = 1;
		}
		rb1.setAlpha(alfa);

		layoutSimulacionAmbiental1.addView(rb1);

		final ImageView iv1 = new ImageView(this);
		iv1.setImageResource(R.drawable.simulacion_ambiental_ico);
		iv1.setLayoutParams(margenElementos);
		layoutSimulacionAmbiental1.addView(iv1);

		final TextView tv1 = new TextView(this);
		tv1.setText(R.string.simulacionAmbientalTXT);
		tv1.setTextColor(color);
		tv1.setAlpha(alfa);
		layoutSimulacionAmbiental1.addView(tv1);		
		contenedor.addView(layoutSimulacionAmbiental1);
		// Fin primera liena
		LinearLayout layoutSimulacionAmbiental2 = new LinearLayout(this);
		layoutSimulacionAmbiental2.setOrientation(LinearLayout.HORIZONTAL);
		layoutSimulacionAmbiental2.setGravity(Gravity.CENTER);
		layoutSimulacionAmbiental2.setBackground(null);
		layoutSimulacionAmbiental2.setLayoutParams(margensuperior);

		final TextView tv2 = new TextView(this);
		tv2.setText(R.string.seleccioneEfecto);
		tv2.setTextSize(getResources().getDimension(R.dimen.seleccioneEfecto));
		tv2.setTextColor(color);
		tv2.setAlpha(alfa);
		tv2.setLayoutParams(margenizquierdo);
		if(this.getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT){			
		layoutSimulacionAmbiental2.addView(tv2);
		}else{layoutSimulacionAmbiental1.addView(tv2);}

		final ImageView iv2 = new ImageView(this);
		iv2.setImageResource(efectosonido.getEfectoAmbientalIco());
		iv2.setLayoutParams(margenElementos);
		// iv2.setImageAlpha(alfa);
		iv2.setAlpha(alfa);
		if(this.getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT){			
			layoutSimulacionAmbiental2.addView(iv2);
			}else{layoutSimulacionAmbiental1.addView(iv2);}

		final TextView tv3 = new TextView(this);
		tv3.setText(efectosonido.getEfectoAmbientalTXT());
		tv3.setTextSize(getResources().getDimension(R.dimen.ActivityEfectosEfectoSelecionado));
		tv3.setTypeface(null, Typeface.BOLD_ITALIC);
		tv3.setTextColor(color);
		tv3.setAlpha(alfa);
		if(this.getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT){			
			layoutSimulacionAmbiental2.addView(tv3);
			}else{layoutSimulacionAmbiental1.addView(tv3);}
		if(this.getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT)
		contenedor.addView(layoutSimulacionAmbiental2);

		// Musica

		LinearLayout layoutMusica1 = new LinearLayout(this);
		layoutMusica1.setOrientation(LinearLayout.HORIZONTAL);
		layoutMusica1.setGravity(Gravity.CENTER_HORIZONTAL
				| Gravity.CENTER_VERTICAL);
		layoutMusica1.setBackground(null);
		layoutMusica1.setLayoutParams(margensuperiorlayout);
		color = getResources().getColor(R.color.negro);
		alfa = (float) 0.5;
		final RadioButton rb2 = new RadioButton(this);
		if (efectosonido.getTipoEfecto() == efectosonido.TIPOMUSICADEFONDO) {
			rb2.setChecked(true);
			alfa = 1;
		}
		layoutMusica1.addView(rb2);

		final ImageView iv3 = new ImageView(this);
		iv3.setImageResource(R.drawable.musica_fondo_ico);
		iv3.setLayoutParams(margenElementos);
		iv3.setAlpha(alfa);
		layoutMusica1.addView(iv3);

		final TextView tv4 = new TextView(this);
		tv4.setText(R.string.musicaFondoTXT);
		tv4.setTextColor(color);
		tv4.setAlpha(alfa);
		layoutMusica1.addView(tv4);

		contenedor.addView(layoutMusica1);
		// Fin primera liena

		LinearLayout layoutMusica2 = new LinearLayout(this);
		layoutMusica2.setOrientation(LinearLayout.HORIZONTAL);
		layoutMusica2.setGravity(Gravity.CENTER);
		layoutMusica2.setBackground(null);
		layoutMusica2.setLayoutParams(margensuperior);

		final TextView tv5 = new TextView(this);
		tv5.setText(R.string.seleccioneEfecto);
		tv5.setTextSize(getResources().getDimension(R.dimen.seleccioneEfecto));
		tv5.setTextColor(color);
		tv5.setAlpha(alfa);
		tv5.setLayoutParams(margenizquierdo);
		if(this.getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT){			
			layoutMusica2.addView(tv5);
			}else{layoutMusica1.addView(tv5);}

		final ImageView iv4 = new ImageView(this);
		iv4.setImageResource(efectosonido.getEfectoMusicalIco());
		iv4.setLayoutParams(margenElementos);
		iv4.setAlpha(alfa);

		if(this.getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT){			
			layoutMusica2.addView(iv4);
			}else{layoutMusica1.addView(iv4);}
		
		final TextView tv6 = new TextView(this);
		tv6.setText(efectosonido.getEfectoMusicalTXT());
		tv6.setTextSize(getResources().getDimension(R.dimen.efectoSelecionado));
		tv6.setTypeface(null, Typeface.BOLD_ITALIC);
		tv6.setTextColor(color);
		tv6.setAlpha(alfa);
		
		if(this.getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT){			
			layoutMusica2.addView(tv6);
			}else{layoutMusica1.addView(tv6);}
		if(this.getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT)
		contenedor.addView(layoutMusica2);
		// Fin segunda fila
		LinearLayout layoutMusica3 = new LinearLayout(this);
		layoutMusica3.setOrientation(LinearLayout.HORIZONTAL);
		layoutMusica3.setGravity(Gravity.CENTER);
		layoutMusica3.setBackground(null);
		layoutMusica3.setLayoutParams(margensuperior);

		final TextView tv7 = new TextView(this);
		tv7.setText(R.string.seleccioneMelodia);
		tv7.setTextSize(getResources().getDimension(R.dimen.seleccioneEfecto));
		tv7.setTextColor(color);
		tv7.setAlpha(alfa);
		tv7.setLayoutParams(margenizquierdo);
		layoutMusica3.addView(tv7);

		final ImageView iv5 = new ImageView(this);
		iv5.setImageResource(efectosonido.getMusicaFondoIco());
		iv5.setLayoutParams(margenElementos);
		iv5.setAlpha(alfa);
		layoutMusica3.addView(iv5);

		final TextView tv8 = new TextView(this);
		tv8.setText(efectosonido.getMusicaFondoTXT());
		tv8.setTextSize(getResources().getDimension(R.dimen.efectoSelecionado));
		tv8.setTypeface(null, Typeface.BOLD_ITALIC);
		tv8.setTextColor(color);
		tv8.setAlpha(alfa);
		layoutMusica3.addView(tv8);

		contenedor.addView(layoutMusica3);

		rb1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				efectosonido
						.setTipoEfecto(efectosonido.TIPOEFECTOSIMULACIONAMBIENTAL);
				rb1.setChecked(true);
				rb2.setChecked(false);
				tv4.setAlpha((float) 0.5);
				tv5.setAlpha((float) 0.5);
				tv6.setAlpha((float) 0.5);
				tv7.setAlpha((float) 0.5);
				tv8.setAlpha((float) 0.5);
				rb2.setAlpha((float) 0.5);
				iv3.setAlpha((float) 0.5);
				iv4.setAlpha((float) 0.5);
				iv5.setAlpha((float) 0.5);
				tv1.setAlpha(1);
				tv2.setAlpha(1);
				tv3.setAlpha(1);
				iv1.setAlpha((float) 1);
				iv2.setAlpha((float) 1);
				rb1.setAlpha(1);
			}
		});

		rb2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				efectosonido.setTipoEfecto(efectosonido.TIPOMUSICADEFONDO);
				rb2.setChecked(true);
				rb1.setChecked(false);
				tv1.setAlpha((float) 0.5);
				tv2.setAlpha((float) 0.5);
				tv3.setAlpha((float) 0.5);
				iv1.setAlpha((float) 0.5);
				iv2.setAlpha((float) 0.5);
				rb1.setAlpha((float) 0.5);
				tv4.setAlpha(1);
				tv5.setAlpha(1);
				tv6.setAlpha(1);
				tv7.setAlpha(1);
				tv8.setAlpha(1);
				rb2.setAlpha(1);
				iv3.setAlpha((float) 1);
				iv4.setAlpha((float) 1);
				iv5.setAlpha((float) 1);
			}
		});

		(tv3).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Abrir Efectos Ambientales
				finish();
				Intent myIntent = new Intent(ActivityEfectos.this,
						ActivityEfectos.class);
				myIntent.putExtra(ActivityPrincipal.MENSAJE,
						ActivityPrincipal.SELECTOREFECTOAMBIENTAL); // Optional
																	// parameters
				ActivityEfectos.this.startActivity(myIntent);
			}
		});

		(iv2).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				Intent myIntent = new Intent(ActivityEfectos.this,
						ActivityEfectos.class);
				myIntent.putExtra(ActivityPrincipal.MENSAJE,
						ActivityPrincipal.SELECTOREFECTOAMBIENTAL); // Optional
																	// parameters
				ActivityEfectos.this.startActivity(myIntent);
			}
		});

		(tv6).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Abrir Efectos Ambientales
				finish();
				Intent myIntent = new Intent(ActivityEfectos.this,
						ActivityEfectos.class);
				myIntent.putExtra(ActivityPrincipal.MENSAJE,
						ActivityPrincipal.SELECTOREFECTOMUSICAL); // Optional
																	// parameters
				ActivityEfectos.this.startActivity(myIntent);
			}
		});
		(tv8).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Abrir Efectos Ambientales
				finish();
				Intent myIntent = new Intent(ActivityEfectos.this,
						ActivityEfectos.class);
				myIntent.putExtra(ActivityPrincipal.MENSAJE,
						ActivityPrincipal.SELECTORMUSICAFONDO); // Optional
																// parameters
				ActivityEfectos.this.startActivity(myIntent);
			}
		});

		(iv4).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				Intent myIntent = new Intent(ActivityEfectos.this,
						ActivityEfectos.class);
				myIntent.putExtra(ActivityPrincipal.MENSAJE,
						ActivityPrincipal.SELECTOREFECTOMUSICAL); // Optional
																	// parameters
				ActivityEfectos.this.startActivity(myIntent);
			}
		});
		(iv5).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				Intent myIntent = new Intent(ActivityEfectos.this,
						ActivityEfectos.class);
				myIntent.putExtra(ActivityPrincipal.MENSAJE,
						ActivityPrincipal.SELECTORMUSICAFONDO); // Optional
																// parameters
				ActivityEfectos.this.startActivity(myIntent);
			}
		});

		((Button) findViewById(R.id.botonaplicar))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// Poner variable de cambio.
						Toast msg = Toast.makeText(ActivityEfectos.this,
								getText(R.string.toastaplicado),
								Toast.LENGTH_SHORT);
						msg.show();
						ActivityEfectos.this.aplicadoCambio = true;
					}
				});

	}

}
