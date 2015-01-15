/**
 * Se encarga de la reproducción de sonidos , contiene los sonidos posibles y toda la implementación
 * necesaria para permitir su selección y reproducción. 
 * <p>
 * A parte del archivo de audio a reproducir contiene el texto que indica que 
 * archivo de audio es (estan incluidos en /res/raw) y su correspondiente icono 
 * (contenidos en los diferentes /res/drwable segun resolucion).
 * <p>
 * Se ha implementado como clase singlenton ya que se quiere poder controlar desde
 * varios activitys como por ejemplo: {@link com.leitat.gui.ActivityEfectos}, {@link com.leitat.gui.ActivityPrincipal}
 * 
 * Si se desea implementar un selector de archivos de audio externos pueden implementar una activity
 * y cambiar la tabla estatica con los efectos predefinidos actualmente implementada por una tabla
 * dinamica.
 * @author Alejandro Blanco Martinez (LEITAT Technological Center)
 *
 */
package com.leitat.sonido;