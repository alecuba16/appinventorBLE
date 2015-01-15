package com.leitat.sonido;

import java.util.HashSet;
import com.leitat.R;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class EfectosSonidoSingleton {
	private final String TAG = "EfectosSonidoSingleton";
	private HashSet<MediaPlayer> mpSet = new HashSet<MediaPlayer>();
	private boolean reproduciendoEfecto = false;
	private boolean reproduccionActivada = false;
	public final int TIPOEFECTOSIMULACIONAMBIENTAL = 0;
	public final int TIPOMUSICADEFONDO = 1;
	public final int EFECTOAMBIENTAL = 0;
	public final int EFECTOMUSICAL = 1;
	public final int MUSICAAMBIENTE = 2;
	private int tipoEfecto = TIPOEFECTOSIMULACIONAMBIENTAL;
	private static Context contexto;
	private MediaPlayer mp;
	private boolean esperar = false;

	public void setEsperar(boolean esperar) {
		this.esperar = esperar;
	}

	public boolean getEsperar() {
		return this.esperar;
	}

	/*
	 * sonidos[TipoSonido(musica/efectosonido)],[Tipo musica/Tipo de
	 * efecto(Ambiental/musical)], [Musica en concreto/Efecto
	 * contreto],[Raw,Txt,Ico]
	 */
	private int[][][][] sonidos = new int[][][][] {
			{/* Efectos Ambientales */
					{/* Ambientales */
							/* Efecto1 */
							{ R.raw.efecto_ambiental_hoja_seca,
									R.string.simulacionAmbientalEfectoHojaSeca,
									R.drawable.hoja },
							/* Efecto2 */
							{ R.raw.efecto_ambiental_nieve,
									R.string.simulacionAmbientalEfectoNieve,
									R.drawable.nieve },
							/* Efecto3 */
							{ R.raw.efecto_ambiental_arena,
									R.string.simulacionAmbientalEfectoArena,
									R.drawable.arena }, }, {/* Otros */
					} },
			{/* Musica Fondo */
					{/* Efectos */
							/* Efecto1 */
							{ R.raw.efecto_musical_tambor,
									R.string.musicaFondoEfectoTambor,
									R.drawable.tambor },
							/* Efecto2 */
							{ R.raw.efecto_musical_maraca,
									R.string.musicaFondoEfectoMaracas,
									R.drawable.maracas },
							/* Efecto3 */
							{ R.raw.efecto_musical_platillos,
									R.string.musicaFondoEfectoPlatillos,
									R.drawable.platillos }, },
					{/* Musica */
					/* Musica1 */
					{ R.raw.musicafondo1, R.string.musicaFondoMelodia1,
							R.drawable.nieve }, } } };

	private static EfectosSonidoSingleton instance = null;
	private int efectoAmbientalSelecionado = 0;
	private int efectoMusicalSelecionado = 0;
	private int musicaFondoSelecionada = 0;

	public void reset() {
		EfectosSonidoSingleton.getInstance(contexto).stop();
	    instance = new EfectosSonidoSingleton(contexto);
	}
	
	/**
	 * Obtiene datos sobre el efecto ambiental pedido
	 * 
	 * @param id
	 *            efecto ambiental a consultar
	 * @return Retorna un array de dos elementos,el R id del texto y el r id del
	 *         icono.
	 */
	public int[] getEfectoAmbiental(int id) {
		return (new int[] { sonidos[TIPOEFECTOSIMULACIONAMBIENTAL][0][id][1],
				sonidos[TIPOEFECTOSIMULACIONAMBIENTAL][0][id][2] });
	}

	/**
	 * Obtiene datos sobre el efecto ambiental pedido
	 * 
	 * @param id
	 *            efecto ambiental a consultar
	 * @param efecto
	 *            EFECTOAMBIENTAL,EFECTOMUSICAL,MUSICAAMBIENTE de
	 *            EfectoSonidoSinglenton..
	 * @return Retorna un array de dos elementos,el R id del texto y el r id del
	 *         icono.
	 */
	public int[] getEfecto(int id, int efecto) {
		int selecionado = 0;
		switch (efecto) {
		case EFECTOAMBIENTAL:
			if (id == efectoAmbientalSelecionado) {
				selecionado = 1;
			}
			return (new int[] { selecionado,
					sonidos[TIPOEFECTOSIMULACIONAMBIENTAL][0][id][1],
					sonidos[TIPOEFECTOSIMULACIONAMBIENTAL][0][id][2] });
		case EFECTOMUSICAL:
			if (id == efectoMusicalSelecionado) {
				selecionado = 1;
			}
			return (new int[] { selecionado,
					sonidos[TIPOMUSICADEFONDO][0][id][1],
					sonidos[TIPOMUSICADEFONDO][0][id][2] });
		case MUSICAAMBIENTE:
			if (id == musicaFondoSelecionada) {
				selecionado = 1;
			}
			return (new int[] { selecionado,
					sonidos[TIPOMUSICADEFONDO][1][id][1],
					sonidos[TIPOMUSICADEFONDO][1][id][2] });
		default:
			return (new int[] { 0, 0 });
		}
	}

	public int getEfectoNumeroElementos(int efecto) {
		switch (efecto) {
		case EFECTOAMBIENTAL:
			return (sonidos[TIPOEFECTOSIMULACIONAMBIENTAL][0].length);
		case EFECTOMUSICAL:
			return (sonidos[TIPOMUSICADEFONDO][0].length);
		case MUSICAAMBIENTE:
			return (sonidos[TIPOMUSICADEFONDO][1].length);
		default:
			return (0);
		}
	}

	public void setEfecto(int efecto, int id) {
		Log.d(TAG, "setEfecto:" + efecto + " , idEfecto:" + id);
		switch (efecto) {
		case EFECTOAMBIENTAL:
			if (id <= sonidos[TIPOEFECTOSIMULACIONAMBIENTAL][0].length
					&& id > 0) {
				efectoAmbientalSelecionado = (id - 1);
			} else {
				efectoAmbientalSelecionado = 0;
			}
			if (reproduccionActivada)
				this.play(false);
		case EFECTOMUSICAL:
			if (id <= sonidos[TIPOMUSICADEFONDO][0].length && id > 0) {
				efectoMusicalSelecionado = (id - 1);
			} else {
				efectoMusicalSelecionado = 0;
			}
			if (reproduccionActivada)
				this.play(false);
		case MUSICAAMBIENTE:
			if (id <= sonidos[TIPOMUSICADEFONDO][1].length && id > 0) {
				musicaFondoSelecionada = (id - 1);
			} else {
				musicaFondoSelecionada = 0;
			}
		default:
		}
	}

	public void setTipoEfecto(int tipoEfecto) {
		this.stop();
		Log.d(TAG, "setTipoEfecto:" + tipoEfecto);
		switch (tipoEfecto) {
		case TIPOEFECTOSIMULACIONAMBIENTAL:
			this.tipoEfecto = TIPOEFECTOSIMULACIONAMBIENTAL;

			break;
		case TIPOMUSICADEFONDO:
			this.tipoEfecto = TIPOMUSICADEFONDO;
			if (reproduccionActivada)
				this.play(true);
			break;
		default:
			this.tipoEfecto = TIPOEFECTOSIMULACIONAMBIENTAL;
			break;
		}
	}

	public int getEfectoAmbiental() {
		return (this.efectoAmbientalSelecionado + 1);
	}

	public int getEfectoAmbientalTXT() {
		return (sonidos[TIPOEFECTOSIMULACIONAMBIENTAL][0][this.efectoAmbientalSelecionado][1]);
	}

	public int getEfectoAmbientalIco() {
		return (sonidos[TIPOEFECTOSIMULACIONAMBIENTAL][0][this.efectoAmbientalSelecionado][2]);
	}

	public void setEfectoAmbiental(int idEfectoAmbiental) {
		if (idEfectoAmbiental <= sonidos[TIPOEFECTOSIMULACIONAMBIENTAL][0].length
				&& idEfectoAmbiental > 0) {
			efectoAmbientalSelecionado = (idEfectoAmbiental - 1);
		} else {
			efectoAmbientalSelecionado = 0;
		}
	}

	public void setEfectoMusical(int idEfectoMusical) {
		if (idEfectoMusical <= sonidos[TIPOMUSICADEFONDO][0].length
				&& idEfectoMusical > 0) {
			efectoMusicalSelecionado = (idEfectoMusical - 1);
		} else {
			efectoMusicalSelecionado = 0;
		}
	}

	public int getEfectoMusical() {
		return (this.efectoMusicalSelecionado + 1);
	}

	public int getEfectoMusicalTXT() {
		return (sonidos[TIPOMUSICADEFONDO][0][this.efectoMusicalSelecionado][1]);
	}

	public int getEfectoMusicalIco() {
		return (sonidos[TIPOMUSICADEFONDO][0][this.efectoMusicalSelecionado][2]);
	}

	public int getMusicaFondo() {
		return (this.musicaFondoSelecionada + 1);
	}

	public int getMusicaFondoTXT() {
		return (sonidos[TIPOMUSICADEFONDO][1][this.musicaFondoSelecionada][1]);
	}

	public int getMusicaFondoIco() {
		return (sonidos[TIPOMUSICADEFONDO][1][this.musicaFondoSelecionada][2]);
	}

	public boolean getEstadoReproduccion() {
		return (reproduccionActivada);
	}

	/**
	 * Retorna el tipo de efecto , si es Ambiental o reproduccion musica de
	 * fondo
	 * 
	 * @return TIPOEFECTOSIMULACIONAMBIENTAL o TIPOMUSICADEFONDO
	 */
	public int getTipoEfecto() {
		return this.tipoEfecto;
	}

	protected EfectosSonidoSingleton(Context context) {
		super();
		EfectosSonidoSingleton.contexto = context;
	}

	public static EfectosSonidoSingleton getInstance(Context context) {
		if (instance == null) {
			instance = new EfectosSonidoSingleton(context);
		}
		return instance;
	}

	private void play(boolean musicaDeFondo) {
		Log.d(TAG, "play - no se estaba reproduciendo efecto");

		if (musicaDeFondo) {
			mp = MediaPlayer
					.create(EfectosSonidoSingleton.contexto,
							this.sonidos[TIPOMUSICADEFONDO][1][musicaFondoSelecionada][0]);
			mp.setVolume(0.2f, 0.2f);
			mp.setLooping(true);
		} else {
			int subefecto = 0;
			if (tipoEfecto == EFECTOAMBIENTAL) {
				subefecto = efectoAmbientalSelecionado;
			} else {
				subefecto = efectoMusicalSelecionado;
			}
			mp = MediaPlayer.create(EfectosSonidoSingleton.contexto,
					this.sonidos[tipoEfecto][0][subefecto][0]);
			mp.setVolume(1f, 1f);
			reproduciendoEfecto = true;
			mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					Log.d(TAG, "onCompletion");
					if (mp == null) {
						Log.d(TAG, "onCompletion - mp vale null");
					}
					mpSet.remove(mp);
					mp.stop();
					mp.release();
					reproduciendoEfecto = false;
				}
			});
		}
		mpSet.add(mp);
		mp.start();
	}

	public void stop() {
		Log.d(TAG, "stop");
		for (MediaPlayer mp : mpSet) {
			if (mp != null) {
				mp.stop();
				mp.release();
			}
		}
		reproduciendoEfecto = false;
		mpSet.clear();
	}

	public void setReproduccion(boolean reproduccionActivada) {
		Log.d(TAG, "setReproduccion a " + reproduccionActivada);
		this.reproduccionActivada = reproduccionActivada;
		if (reproduccionActivada) {
			switch (tipoEfecto) {
			case TIPOEFECTOSIMULACIONAMBIENTAL:
				// TODO setReproduciendo Simulacion Ambiental implementar el
				// cambio de estado si es necesario.
				break;
			case TIPOMUSICADEFONDO:
				this.play(true);
				break;
			default:
				break;
			}
		} else {
			this.stop();
		}
	}

	public void reproducirEfectoDeSonido(int sonidoid) {
		if (reproduccionActivada) {
			Log.d(TAG, "reproducirEfectoSonido - Reproduccion Activada");
			if (esperar) {
				if (!reproduciendoEfecto) {
					this.play(false);
					Log.d(TAG,
							"reproducirEfectoSonido - Reproducido intensidad piezo id("
									+ sonidoid + ")");
				} else {
					Log.d(TAG,
							"reproducirEfectoSonido - Se estaba reproduciendo un efecto");
				}
			} else {
				this.play(false);
			}
		} else
			Log.d(TAG,
					"reproducirEfectoSonido - La reproduccion esta desactivada");
	}
}