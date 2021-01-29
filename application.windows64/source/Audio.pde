import ddf.minim.*;
import ddf.minim.analysis.*;
import ddf.minim.effects.*;
import ddf.minim.signals.*;
import ddf.minim.spi.*;
import ddf.minim.ugens.*;

import java.util.Map;
import java.util.HashMap;
public Minim minim;

/*
  Esta clase encapsula todos los métodos de tratamiento de audio
*/
public static class Audio {
  private final static String THEME_1 = "t1";
  private final static String THEME_2 = "t2";
  private final static String THEME_3 = "t3";
  private final static String BUTTON_CLICK = "btn-click";
  private final static String BUTTON_OVER = "btn-over";
  private final static String GET_COIN = "coin1";
  private final static String GET_HEART = "heart";
  private final static String GET_FLASK = "flask";
  private final static String TURKEY_HIT = "hit";
  private final static String FLAME = "flame";
  
  // Instancia de minim
  private static Minim minim;
  // Reproductor de música de fondo
  private static AudioPlayer backgroundMusicPlayer;
  // Lista de temas musicales elegibles
  private static Map<String, String> themes;
  // Colección de samples de audio usados en la interfaz y juego
  private static Map<String, AudioSample> samples;
  // Volumen global
  private static float globalVolume = 0f;
  
  // Inicializa la instancia de minim y carga todos los recursos de audio
  static void start(Object handler) {
    minim = new Minim(handler);
    themes = new HashMap<String, String>();
    samples = new HashMap<String, AudioSample>();

    themes.put(THEME_1, ASSETS_PATH+"Chiptune.mp3");
    themes.put(THEME_2, ASSETS_PATH+"Jingle bells.mp3");
    themes.put(THEME_3, ASSETS_PATH+"MrAsterB - 01 Cursed Dream.mp3");
    
    loadSample(BUTTON_CLICK, ASSETS_PATH+"171697__nenadsimic__menu-selection-click.mp3");
    loadSample(BUTTON_OVER, ASSETS_PATH+"533937__soundshelves__ui-menu-button-scroll-down-hover-over.mp3");
    loadSample(GET_COIN, ASSETS_PATH+"336937__free-rush__coin8.wav");
    loadSample(GET_HEART, ASSETS_PATH+"345297__scrampunk__itemize.wav");
    loadSample(GET_FLASK, ASSETS_PATH+"flask.wav");
    loadSample(TURKEY_HIT, ASSETS_PATH+"435882__dersuperanton__chicken.wav");
    loadSample(FLAME,ASSETS_PATH+"540828__eminyildirim__fire-fuse-ignite-flame_16b.wav");
  }
  // Carga un sample con nombre en memoria
  static void loadSample(String id, String path) {
    println("Loading sample "+path);
    samples.put(id, minim.loadSample(path));
  }
  // Reproduce un tema de audio
  static void playTheme(String id) {
    if (backgroundMusicPlayer != null) backgroundMusicPlayer.close();
    backgroundMusicPlayer = minim.loadFile(themes.get(id));
    // La música se reproducirá en bucle
    backgroundMusicPlayer.loop();
    // Le aplicamos el valor de ganancia establecido
    backgroundMusicPlayer.setGain(globalVolume);
  }
  // Reproduce una vez un sample de audio
  static void playSample(String id) {
    samples.get(id).trigger();
  }

  // Establece el volumen global
  // NOTA: En el equipo donde se ha desarrollado el programa, AudioSample no exhibe el control VOLUME, 
  // por lo que se varía el volumen global mediante la ganancia. El valor de la ganancia se hace en decibelios
  // por lo que crecerá exponencialmente con el valor de volume
  static void setVolume(float volume) {
    // Estos valores han sido obtenidos mediante ensayo y error
    globalVolume = -35 + (volume *45f / 100); 
    
    // Variamos la ganancia de todos los audios cargados
    if (backgroundMusicPlayer != null) backgroundMusicPlayer.setGain(globalVolume);

    for (AudioSample sample : samples.values()) {
      sample.setGain(globalVolume);
    }
  }

  // Devuelve el valor del volumen en el rango 1-100
  static float getVolume() {
    return (35 + globalVolume) * 100 / 45f;
  }
}
