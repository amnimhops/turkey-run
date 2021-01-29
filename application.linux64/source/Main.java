import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import controlP5.*; 
import ddf.minim.*; 
import ddf.minim.analysis.*; 
import ddf.minim.effects.*; 
import ddf.minim.signals.*; 
import ddf.minim.spi.*; 
import ddf.minim.ugens.*; 
import java.util.Map; 
import java.util.HashMap; 
import java.util.Iterator; 
import processing.core.PApplet; 
import java.util.List; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Main extends PApplet {



// Ancho de la ventana
final static int SCREEN_WIDTH = 1600;
// Alto de la ventana
final static int SCREEN_HEIGHT = 800;
// Ruta de los recursos de la aplicación
final static String ASSETS_PATH = "assets/";

float appCfgAudioVolume = 25.0f;
String appCfgAudioTheme = Audio.THEME_3;
String appCfgBackgroundImage = "a";


// Instancia de la clase que gestiona el ciclo de vida de las etapas
StageController controller = new StageController(this, SCREEN_WIDTH,SCREEN_HEIGHT);

// Reloj global que usaremos para hacer avanzar el tiempo en el juego
Clock clock = new Clock();

// Es necesario emplear el callback settings() para poder usar la función size() con parámetros
public void settings() {
  // Establecemos el tamaño de la pantalla al ancho y alto definidos
  size(SCREEN_WIDTH, SCREEN_HEIGHT);
}

// El callback settings() se invoca antes de que se cree la ventana, por lo que
// tenemos que establecer aquí el número de fotogramas por segundo
public void setup() {
  frameRate(60);
  Audio.start(this);
  Audio.setVolume(appCfgAudioVolume);
  Audio.playTheme(appCfgAudioTheme);
  loadBackgroundImages();
}

/*
 * Este método se encarga de actualizar el ciclo de vida de las etapas del juego y 
 * de hacer que cada etapa progrese conforme a su programación. El avance del tiempo
 * en cada etapa se hace suministrando el tiempo transcurrido desde la última invocación
 * en lugar de a intervalos regulares para evitar depender de la periodicidad del bucle
 * de redibujado
 */
public void draw() {
  // Actualizamos el reloj para calcular la distancia en el tiempo desde la última invocación
  clock.update();

  /* Este método controla la secuencia de cambio entre etapas. En su interior
   * está cableada toda la lógica de navegación del juego.
   */
  controller.processStageSequence();
  /* Este método actualiza la etapa en curso en función del tiempo */ 
  controller.procesStageLifecycle(clock.getDeltaMillis());
  /* Si hay una etapa cargada, volcamos su superficie de dibujo en la pantalla */
  if (controller.getStage() != null) {
    image(controller.getStage().getGraphics(), 0, 0);
  }
  /* Finalmente, limpiamos las interrupciones del buffer de entrada */
  controller.getInputHandler().clearInput();
}


public void mousePressed(){
  // Informamos al controlador del ratón de que se ha presionado un botón
  controller.getInputHandler().mousePressed(mouseButton);
}

public void mouseReleased(){
  // Informamos al controlador del ratón de que se ha soltado un botón
  controller.getInputHandler().mouseReleased(mouseButton);
}

public void mouseMoved(){
  controller.getInputHandler().mouseMoved();
}

public void keyPressed(){
  controller.getInputHandler().keyPressed(keyCode);
}
public void keyReleased(){
  controller.getInputHandler().keyReleased(keyCode);
}









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
  public static void start(Object handler) {
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
  public static void loadSample(String id, String path) {
    println("Loading sample "+path);
    samples.put(id, minim.loadSample(path));
  }
  // Reproduce un tema de audio
  public static void playTheme(String id) {
    if (backgroundMusicPlayer != null) backgroundMusicPlayer.close();
    backgroundMusicPlayer = minim.loadFile(themes.get(id));
    // La música se reproducirá en bucle
    backgroundMusicPlayer.loop();
    // Le aplicamos el valor de ganancia establecido
    backgroundMusicPlayer.setGain(globalVolume);
  }
  // Reproduce una vez un sample de audio
  public static void playSample(String id) {
    samples.get(id).trigger();
  }

  // Establece el volumen global
  // NOTA: En el equipo donde se ha desarrollado el programa, AudioSample no exhibe el control VOLUME, 
  // por lo que se varía el volumen global mediante la ganancia. El valor de la ganancia se hace en decibelios
  // por lo que crecerá exponencialmente con el valor de volume
  public static void setVolume(float volume) {
    // Estos valores han sido obtenidos mediante ensayo y error
    globalVolume = -35 + (volume *45f / 100); 
    
    // Variamos la ganancia de todos los audios cargados
    if (backgroundMusicPlayer != null) backgroundMusicPlayer.setGain(globalVolume);

    for (AudioSample sample : samples.values()) {
      sample.setGain(globalVolume);
    }
  }

  // Devuelve el valor del volumen en el rango 1-100
  public static float getVolume() {
    return (35 + globalVolume) * 100 / 45f;
  }
}
/* //<>//
  Esta clase representa una instancia de una animación, una
 secuencia de imágenes que se reproduce a 25fps
 */
class Animation {
  // Lista de fotogramas
  PImage[] frames;
  // Índice del fotograma que será dibujado
  int index;
  // Milisegundos transcurridos desde la última invocación,
  // se encarga de controlar que la animación se reproduzca a 25fps,
  // o lo que es lo mismo, una imagen cada 40ms
  long lastFrame;

  // Constructor de clase. Recibe el fragmento del nombre de la animación
  // y el número de fotogramas. Con estos datos busca en la carpeta de assets
  // todos los archivos con ese nombre y construye la animación.
  public Animation(String name, int numFrames) {
    PImage[] frames = new PImage[numFrames];
    for (int c=0; c<numFrames; c++) {
      frames[c] = loadImage(ASSETS_PATH+name+"_"+c+".png");
      lastFrame = 0;
    }

    this.frames = frames;
    this.index = 0;
  }
  // Devuelve la imagen correspondiente al fotograma actual
  public PImage getImage() {
    return frames[index];
  }
  // Añade el tiempo transcurrido al contador de tiempo
  // de la animación para deterimnar si debe
  // avanzar al siguiente fotograma
  public void nextFrame(long delta) {
    lastFrame += delta;
    // 40 = 1000 * ( 1 / 25s )
    if (lastFrame > 40) {
      index = index+1;
      // La animación es cíclica, volvemos a cero rebasado el último fotograma
      if (index >= frames.length) index = 0;
      // Si hemos avanzado un fotograma, reiniciamos el contador de tiempo
      lastFrame = 0;
    }
  }
}

/*
 Esta clase contiene una instancia de un item en el suelo. Los
 items son objetos que ayudan al jugador en su misión de salvar
 al pavo o sumar puntos.
 */
class Item {
  // Item para otorgar monedas
  public final static int COIN = 0;
  // Item para otorgar puntos de vida
  public final static int HEART = 1;
  // Item para otorgar puntos de vuelo
  public final static int FLASK = 2;
  // Animación que reproducirá
  Animation animation;
  // Posicion del item en la pantalla
  Vector position;
  // Este valor controla si el item ha colisionado con el pavo
  // y la dirección en la que se moverá para desaparecer
  Vector touch;
  // Deterimna si un item está habilitado (visible y recolectable)
  boolean enabled;
  // Valor del item, determina la cantidad de puntos de vida, puntuación o vuelo que da al tocarlo
  int value;
  // Determina el tipo de item (COINS,FLASK,HEART)
  int type;

  // Constructor, inicializa los valores del item
  public Item(Animation animation, Vector position, int type) {
    this.type = type;
    this.position = position;
    this.animation = animation;
    this.value = 0;
    this.enabled = false;
  }
}
/*
  Esta clase representa el estado de una cerilla.
 */
class Match {
  // Velocidad a la que se apaga la llama
  public final static int INTENSITY_DECAY = 2;
  // Calor máximo que una cerilla puede soportar antes de encenderse
  public final static int MAX_HEAT = 25;
  // Intensidad máxima de la llama
  public final static int MAX_INTENSITY = 150;

  public final static float HEAT_TRANSMISION = 5; 
  // Posición de la cerilla en el escenario
  Vector position;
  // Intensidad de la llama; 0 = apagada
  float intensity;
  // Calor de la cerilla. Las cerillas están juntas en el escenario y cuando una está
  // encendida transmite calor a las cercanas. Cuando llega al límite de calor soportado
  // se enciende, creando un efecto dominó.
  float heat;
  // Determina si la cerilla ha sido ya usada
  boolean used;
  // Este flag indica que la cerilla no tiene cabeza, y por tanto no se encenderá. Este mecanismo
  // evita incendios perpetuos por la parte derecha de la pantalla.
  boolean noFire;

  // Constructor de la clase
  Match(Vector position) {
    this.position = position;
    this.used = false;
    this.heat = 0; 
    this.intensity = 0;
    noFire = false;
  }
  // Añade intensidad a la llama hasta el límite establecido
  public void addIntensity(float amount) {
    this.intensity = min(this.intensity+amount, MAX_INTENSITY);
  }
}

/*
  Esta clase contiene el estado del pavo
 */
class Turkey {
  // Posición del pavo
  Vector position;
  // El pavo es un animal rico en sabores y expresiones, y cuenta con más de una animación
  Map<String, Animation> animations;
  // Animación que se reproduce actualmente
  Animation currentAnimation;
  // Ancho y alto del pavo
  int width, height;

  // Constructor
  public Turkey(int x, int y, int width, int height) {
    this.width = width;
    this.height = height;
    this.position = new Vector(x, y);

    animations = new HashMap<String, Animation>();
  }

  // Añade una nueva animación con nombre a la lista
  public void addAnimation(String name, int frames) {
    animations.put(name, new Animation(name, frames));
  }
  // Reproduce una animación dado su nombre
  public void playAnimation(String name) {
    currentAnimation = animations.get(name);
  }
}

/*
  Esta clase representa el estado de un enemigo
 */
class Enemy {
  // Determina si el enemigo está activo
  boolean enabled;
  // Animación asociada
  Animation animation;
  // Posición en la pantalla
  Vector position;
  // Daño que inflinge al pavo al colisionar con el
  int strength;
  // Tipo de enemigo
  int type;

  // Constructor
  public Enemy(Animation animation, Vector position, int type) {
    this.animation = animation;
    this.position = position;
    this.enabled = false;
    this.type = type;
    this.strength = (1+type) * 5;
  }
}

/**
 * GameStage - Etapa del videojuego
 *
 * Esta etapa contiene toda la lógica asociada a la visualización del videojuego en sí,
 * así como la gestión de entrada/salida para que los gráficos respondan a las acciones
 * del usuario.
 */
class GameStage extends Stage {
  // Ajuste de corrección de perspectiva, se usa para corregir la diferencia entre el ancho de los elementos y la cabeza de la cerilla
  private final static int PERSP_X_ADJUST = -75;
  // Radio de la cabeza de la cerilla
  private final static int MATCH_HEAD_RADIUS = 15;
  // Velocidad inicial
  private final static int SPEED = 10;
  // Puntos que otorga una moneda
  private final static int COIN_VALUE = 50;
  // Vida que otorga un corazón
  private final static int HEART_VALUE = 10;
  // Puntos de vuelo al conseguir una poción
  private final static int FLASK_VALUE = 30;
  // Puntos de vida iniciales
  private final static int INITIAL_LIFE_POINTS = 100;
  // Puntos de vuelo iniciales
  private final static int INITIAL_POWER_POINTS = 100;
  // Empuje de vuelo, determina cuanto asciende el pavo en cada vuelo
  private final static int FLY_THRUST = -20;
  // Tiempo de invulnerabilidad del pavo tras ser atacado
  private final static long BLINK_PERIOD_AFTER_HIT = 1500;
  // Flag de depuración, para mostrar las cajas de colisión
  private final static boolean DEBUG = false;
  // Interfaz gráfica
  ControlP5 ui;
  // Referencia al buffer de dibujado
  PGraphics gfx;
  // El pavo
  Turkey turkey;
  // Lista de animaciones de los enemigos, indexadas por tipo
  List<Animation> enemyAnimations;
  // Lista de animaciones de los items, indexadas por tipo
  List<Animation> itemAnimations;
  // Velocidad a la que se mueve el terreno
  float terrainSpeed;
  // Puntos de vida del pavo
  int lifePoints;
  // Puntos de vida pendientes de asignar, usado para las transiciones de las barras
  int pendingLifePoints;
  // Puntos de vuelo
  int powerPoints;
  // Puntos de vuelo pendientes de asignar, usado para las transiciones de las barras
  int pendingPowerPoints;
  // Milisegundos transcurridos entre el inicio del juego y la última vez que el pavo fue agredido
  long lastHitTime;
  // Items en pantalla
  List<Item> items;
  // Camino de cerillas
  List<Match> matches;
  // Ejércitos enemigos
  List<Enemy> enemies;
  // Contador de cerillas, sirve como semilla para la función de generación de terreno
  long matcherCount;
  // Contador de items, sirve como semilla para la función de generación de terreno
  long itemCount;
  // Contador de enemigos, sirve como semilla para la función de generación de terreno
  long enemyCount;
  // Flag que indica que el pavo se encuentra ahora en pastos mas verdes
  boolean dead;
  // Vector de desplazamiento vertical
  float vy;
  // Flag que indica que el pavo se encuentra volando
  boolean fly;
  // Puntos acumulados
  long score;
  // Puntos pendientes de asignar, usado para la animación del texto del marcador
  int pendingScore;
  // Paleta de colores del fuego, para pintar acorde a la intensidad de la llama 
  List<Integer> firePalette;

  // Constructor de la etapa
  GameStage(PApplet applet) {
    super(applet);
  }

  // Callback de inicialización
  public void onStageBegin() {
    // Establecemos los valores iniciales
    lifePoints = INITIAL_LIFE_POINTS;
    powerPoints = INITIAL_POWER_POINTS;
    terrainSpeed = SPEED;
    // Cargamos la imagen del boton de volver atrás
    loadButtonImages(new String[]{"btn-back"});
    // Creamos el boton de volver atrás
    ui = new ControlP5(this.applet);
    createImageButton(this, ui, "onClick", "btn-back", 25, this.height-BTN_IMAGE_HEIGHT-25);

    // Asignamos una referencia permanente al buffer de gráficos para no tener que extraerla constantemente
    // ya que esta clase hace uso intensivo de esta variable
    this.gfx = getGraphics();
    // Creamos el pavo y asignamos las dos animaciones (saltar y correr)
    turkey = new Turkey(width/2, height/2, 150, 150); 
    turkey.addAnimation("pavo_corre", 6);
    turkey.addAnimation("pavo_salta", 6);
    // Establecemos como animación actual la de "correr"
    turkey.playAnimation("pavo_corre");

    // Inicializamos las cerillas, los items y los enemigos
    matches = new ArrayList<Match>();
    items = new ArrayList<Item>();
    enemies = new ArrayList<Enemy>();

    // Construimos la paleta con los colores del fuego
    initializeFirePalette();
    // Cargamos las animaciones de los enemigos
    initializeEnemyAnimations();
    // Cargamos las animaciones de los items
    initializeItemAnimations();
  }

  // Carga las animaciones de los dos tipos de enemigos
  public void initializeEnemyAnimations() {
    enemyAnimations = new ArrayList<Animation>();

    for (int c=0; c<2; c++) {
      enemyAnimations.add(new Animation("enemy_"+c, 6));
    }
  }
  // Carga las animaciones de los tres tipos de items
  public void initializeItemAnimations() {
    itemAnimations = new ArrayList<Animation>();

    for (int c=0; c<3; c++) {
      itemAnimations.add(new Animation("item_"+c, 6));
    }
  }
  // Construye la paleta de colores del fuego.
  // Basándose en 6 colores básicos, crea un degradado de 384 colores usando 
  // interpolaciones de los elementos de dos en dos
  public void initializeFirePalette() {
    firePalette = new ArrayList<Integer>();
    //int[] colours = new int[]{color(255,255,255),color(255,255,0),color(255,255,0),color(255,194,0),color(255,194,0),color(100,100,0),color(255,0,0),color(0,0,0)};
    int[] colours = new int[]{color(255, 255, 255), color(255, 255, 255), color(255, 255, 0), color(255, 194, 0), color(100, 100, 0), color(50, 0, 0)};
    for (int c=0; c<colours.length-1; c++) {
      int colorA = colours[c];
      int colorB = colours[c+1];
      for (int d=0; d<64; d++) {
        firePalette.add(lerpColor(colorA, colorB, d/64f));
      }
    }
  }

  // Oculta la interfaz al terminar
  public void onStageEnd() {
    ui.hide();
  }

  // Callback del botón de volver, termina la etapa al hacer click
  public void onClick() {
    end();
  }

  // Borra la pantalla en cada iteración para volver a dibujar
  public void clear() {
    // En lugar de pintar la pantalla en negro, usamos la imagen de fondo establecida en la configuración
    PImage background = backgroundImages.get(appCfgBackgroundImage);
    gfx.image(background, (this.width-background.width)/2, (this.height-background.height)/2);
  }
  
  // Dibuja una única cerilla en la pantalla
  public void drawMatch(Match match) {
    int x = match.position.x;
    int y = match.position.y;

    if (DEBUG) {
      // Si el flag de depuración está activado, pintamos la caja
      // de colisión de la cerilla
      gfx.noFill();
      gfx.strokeWeight(2);
      gfx.stroke(255, 100, 100);
      gfx.rect(x, y, MATCH_HEAD_RADIUS, MATCH_HEAD_RADIUS);
    } else {
      // Dibujamos la cerilla. Las cerillas no son imagenes, son elementos básicos (arcos, lineas, círculos)
      // pintados de izquierda a derecha, superponiendose unos a otros de manera que den la impresión de estar
      // dibujadas en perspectiva.
      
      // Linea gruesa
      gfx.strokeWeight(10);
      // Usamos dos colores diferentes para dar sensación de profundidad
      gfx.stroke(255, 237, 181);
      // La linea está levemente inclinada para dar la sensación de perspectiva
      gfx.line(x, y, x+50, y-5);
      
      gfx.stroke(214, 199, 153);
      gfx.line(x+5, y, x+55, y-5);
      gfx.strokeWeight(0);

      if (!match.used) {
        // Si la cerilla no ha prendido, dibujamos una interpolación de color entre rojo fósforo y blanco
        // en función del calor transimitido a la cerilla. Sin calor = rojo, con calor máximo = blanco
        int headCol = lerpColor(color(188, 32, 0), color(255, 255, 255), 1f*match.heat/Match.MAX_HEAT);
        gfx.fill(headCol);
      } else {
        // Si la cerilla ya ha sido usada, le ponemos un color ceniza
        gfx.fill(color(74, 52, 52));
      }

      // Si la cerilla es activable, dibujamos la cabeza con el color elegido
      if (!match.noFire) gfx.circle(x, y, MATCH_HEAD_RADIUS);
    }
  }

  // Esta función dibuja un único enemigo
  public void drawEnemy(Enemy enemy) {
    int x = enemy.position.x;
    int y = enemy.position.y;
    
    // Pintamos en la posición del enemigo el fotograma actual de la animación asociada
    gfx.image(enemy.animation.getImage(), enemy.position.x + PERSP_X_ADJUST, enemy.position.y);
    
    if (DEBUG) {
      // Si está activo el flag de debug, mostramos la caja de colisión
      gfx.noFill();
      gfx.strokeWeight(1);
      gfx.stroke(255, 255, 0);
      gfx.rect(enemy.position.x, enemy.position.y, enemy.animation.getImage().width, enemy.animation.getImage().height);
    }
  }
  
  // Obtiene el color con índice 'value' de la paleta de fuego como si esta tuviera 'max' elementos 
  public int getFireColor(int value, int max) {
    int index = (int)(value * 1f * firePalette.size() / max);
    if (index >= firePalette.size()) {
      return firePalette.get(firePalette.size()-1);
    } else {
      return firePalette.get(index);
    }
  }
  // Dibuja un único item
  public void drawItem(Item item) {
    if (DEBUG) {
      // Si está activo el flag de debug, mostramos la caja de colisión
      gfx.strokeWeight(2);
      gfx.noFill();
      gfx.stroke(255, 0, 255);
      gfx.rect(item.position.x, item.position.y, item.animation.getImage().width, item.animation.getImage().height); 
      gfx.noStroke();
    }
    
    if (item.touch != null) {
      // Si el item ha sido recolectado, variamos el valor de transparencia para hacerlo desaparecer
      gfx.tint(255, 256 - 256 *(item.position.y - item.touch.y)/item.position.y); 
      gfx.image(item.animation.getImage(), item.touch.x, item.touch.y);
      gfx.noTint();
    } else {
      // Dibujamos el fotograma actual de la animación asociada en la posición del item
      gfx.image(item.animation.getImage(), item.position.x, item.position.y);
    }
  }

  // Devuelve la altura en pixeles de una llama dada su intensidad
  public int getFlameHeight(float intensity) {
    // El valor es un poco random, ha sido ajustado hasta encontrar un equilibrio aceptable
    return (int)(MATCH_HEAD_RADIUS * 8 * intensity / Match.MAX_INTENSITY);
  }

  // Dibuja las llamas de las cerillas
  // A diferencia del resto de métodos de dibujo, aquí se pintan todas a la vez. Esto se debe
  // a que mientras que el resto de elementos dependen de la cerilla sobre la que descansan 
  // para ser pintados antes o despues, las llamas ocultan todo el escenario, por lo que el orden
  // no importa.
  public void drawFire() {
    // Quitamos el color de línea, solo relleno
    gfx.noStroke();
    // Iteramos por todas las cerillas
    for (Match match : matches) {
      // Pintamos fuego solo si la intensidad es positiva
      if (match.intensity > 0) {
        int x = match.position.x;
        int y = match.position.y;
        // Obtenemos la altura de la llama
        int size = getFlameHeight(match.intensity);
        // Obtenemos el color de la llama
        int col = getFireColor((int)(Match.MAX_INTENSITY-match.intensity), Match.MAX_INTENSITY);
        
        gfx.fill(col);
        // Dibujamos dos semióvalos con las proporciones adecuadas
        gfx.arc(x, y, size/6, size/8, 0, PI);
        gfx.arc(x, y, size/6, size*2, PI, PI*2);

        if (DEBUG) {
          // Si está activo el flag de debug, mostramos la caja de colisión
          gfx.stroke(255, 0, 0);
          gfx.rect(match.position.x, match.position.y-size, MATCH_HEAD_RADIUS, size);
        }
      }
    }
  }

  // Dibuja el marcador de puntuación
  public void drawScore() {
    String scoreText = String.valueOf(score);
    gfx.textSize(32);
    gfx.fill(255, 255, 255);
    int textWidth = (int)gfx.textWidth(scoreText);
    // Lo pintamos arriba a la derecha, con 25px de padding
    gfx.text(scoreText, width-textWidth-25, 50);
  }

  // Crea la animación del marcador de puntos
  public void updateScore() {
    // Si quedan puntos pendientes de ser asignados, los asignamos y quitamos de la lista de pendientes
    if (pendingScore>0) {
      int scoreTaken = min(pendingScore, 5);
      pendingScore-=scoreTaken;
      score+=scoreTaken;
    }
  }
  // Crea la animación de la barra de vida y determina si el pavo vive o muere 
  public void updateLifePoints() {
    // Si quedan puntos de vida pendientes de asignar los vamos descontando poco a poco
    if (pendingLifePoints != 0) {
      int pointsTaken = pendingLifePoints > 0 ? 1 : -1;
      pendingLifePoints-=pointsTaken;
      lifePoints+=pointsTaken;

      // Si tras operar con los puntos de vida el pavo está seco, firmamos el acta de defunción 
      if (lifePoints==0) {
        dead = true;
      }
    }
  }

  // Crea la animación de la barra de puntos de vuelo
  public void updatePowerPoints() {
    // Si quedan puntos de vuelo pendientes de asignar los vamos descontando poco a poco
    if (pendingPowerPoints != 0) {
      int pointsTaken = pendingPowerPoints > 0 ? 1 : -1;
      pendingPowerPoints-=pointsTaken;
      powerPoints+=pointsTaken;
    }
  }
  
  // Añade puntos a la lista de puntos pendientes
  public void addScore(int amount) {
    pendingScore+=amount;
  }

  // Añade puntos de vida a la lista de puntos pendientes
  public void addLifePoints(int amount) {
    
    if (amount > 0) {
      // Si se añaden puntos, nos aseguramos que no se rebase el máximo
      if (lifePoints+pendingLifePoints+amount > INITIAL_LIFE_POINTS) {
        pendingLifePoints = INITIAL_LIFE_POINTS - lifePoints;
      } else {
        pendingLifePoints += amount;
      }
    } else if (amount < 0) {
      // Todo el daño inflingido al pavo pasa por aquí, de manera que
      // reproducimos el sample asociado cada vez que ocurra
      if (!dead) Audio.playSample(Audio.TURKEY_HIT);
      // Restamos puntos de vida asegurandonos que nunca sean menores que 0 (¿pavos zombies?)
      if (lifePoints + pendingLifePoints +amount < 0) {
        pendingLifePoints = -lifePoints;
      } else {
        pendingLifePoints += amount;
      }
    }
  }
  // Determina cuantos puntos de vida tiene el pavo, teniendo en cuenta el valor pendiente de asignar
  public int getLifePoints() {
    return pendingLifePoints + lifePoints;
  }
  // Determina cuantos puntos de vuelo tiene el pavo, teniendo en cuenta el valor pendiente de asignar
  public int getPowerPoints() {
    return pendingPowerPoints + powerPoints;
  }
  
  // Añade puntos de vuelo a la lista de puntos pendientes
  public void addPowerPoints(int amount) {
    if (amount > 0) {
      // Nos aseguramos que el valor sumado no haga desbordar del límite superior
      if (powerPoints+pendingPowerPoints+amount > INITIAL_POWER_POINTS) {
        pendingPowerPoints = INITIAL_POWER_POINTS - powerPoints;
      } else {
        pendingPowerPoints += amount;
      }
    } else if (amount < 0) {
      // Evitamos que al sustraer puntos sea menor que cero
      if (powerPoints + pendingPowerPoints +amount < 0) {
        pendingPowerPoints = -powerPoints;
      } else {
        pendingPowerPoints += amount;
      }
    }
  }

  // Devuelve un camino sinusoidal
  private float cosinePathFunction(float t, float amplitude) {
    return amplitude * cos(2*PI*t);
  }
  
  // Funcion determinista de generación del terreno
  private int getTerrainHeight(float x) {
    float period = x % (2*PI);
    // Operamos un par de veces con la función coseno para obtener diferentes series de crestas y valles
    return 2*height/3 + (int)( (-50 + x%100) +  cosinePathFunction(period, 50) * cosinePathFunction(period/2, 2) * cosinePathFunction(period/4, 1));
  }

  // Añade un item encima de cada cerilla, ocultando los suficientes como para que no se visualicen apilados
  // No es la función más óptima ni más bonita, pero funciona y mantiene la ilusión de perspectiva sin tener
  // que recurrir a estructuras más complejas.
  private void fillWithItems() {
    
    // Si hay items, calculamos la posición del último para empezar a añadir por ahí
    int x = 0;
    if (items.size() > 0) {
      x = items.get(items.size()-1).position.x + MATCH_HEAD_RADIUS; // Usamos este valor para que coincidan verticalmente monedas y cerillas
    }

    // Mientras no se haya rellenado toda la pantalla
    while (x<width) {
      // Calculamos aleatoriamente el tipo: 75% de monedas, 20% de viales y 5% de corazones
      int type = random(100) < 75 ? Item.COIN : random(100) < 80 ? Item.FLASK : Item.HEART;
      Animation itemAnimation = itemAnimations.get(type);
      // getTerrainHeight() es determinista, por lo que podemos confiar en que
      // el valor devuelto corresponde con la cerilla de debajo
      Item item = new Item(itemAnimation, new Vector(x, getTerrainHeight(itemCount/50f)-75), type);

      // Establecemos el valor en función del tipo
      switch(type) {
      case Item.COIN:
        item.value = COIN_VALUE;
        break;
      case Item.HEART:
        item.value = HEART_VALUE;
        break;
      case Item.FLASK:
        item.value = FLASK_VALUE;
        break;
      }
      
      // Solo el 5% de los items están activos
      if (random(1, 100) < 5) item.enabled = true;

      // Pasamos a la posición de la siguiente cerilla
      x+=MATCH_HEAD_RADIUS;
      itemCount++;
      items.add(item);
    }
  }

  // Añade una cerilla horizontalmente llenando toda la pantalla. Con ligeras
  // modificaciones a la altura creamos un camino sinuoso por el que se moverá el pavo.
  private void fillWithMatches() {
    int x = 0;
    // Calculamos la posición donde empezar a poner cerillas
    if (matches.size() > 0) {
      x = matches.get(matches.size()-1).position.x + MATCH_HEAD_RADIUS;
    }
    // Rellenamos con cerillas hasta llegar al ancho de la pantalla
    while (x<width) {
      Match match = new Match(new Vector(x, getTerrainHeight(matcherCount/50f)));
      // Un 5% de las cerillas vienen sin mecha. Empleamos este mecanismo para
      // evitar que un encendido aleatorio se propague hasta el ancho de la pantalla,
      // creando una combustión perenne de todas las cerillas nuevas
      if (random(100) > 95) match.noFire = true;
      
      match.intensity = 0;
      x+=MATCH_HEAD_RADIUS;
      matcherCount++;
      matches.add(match);
    }
  }

  // Añade un enemigo encima de cada cerilla, ocultando los suficientes como para que no se visualicen apilados
  // No es la función más óptima ni más bonita, pero funciona y mantiene la ilusión de perspectiva sin tener
  // que recurrir a estructuras más complejas.
  private void fillWithEnemies() {
    // Calculamos la posición donde empezar a poner enemigos
    int x = 0;
    if (enemies.size() > 0) {
      x = enemies.get(enemies.size()-1).position.x + MATCH_HEAD_RADIUS;
    }
    // Rellenamos con cerillas hasta llegar al ancho de la pantalla
    while (x<width) {
      // Solo el 1% de los enemigos está activo
      boolean enabled = random(1000) >990;
      // Elegimos aleatoriamente el tipo de enemigo
      int type = (int)random(enemyAnimations.size());
      // Calculamos la posición
      Vector position = new Vector(x, getTerrainHeight(enemyCount/50f)-150);
      // Asignamos la animación en función del tipo
      Enemy enemy = new Enemy(enemyAnimations.get(type), position, type);
      // Lo activamos ( o no)
      enemy.enabled = enabled;
      
      x+=MATCH_HEAD_RADIUS;
      enemyCount++;
      enemies.add(enemy);
    } 
  }

  // Actualizamos la posición y estado de todos los enemigos
  public void updateEnemies() {
    
    Iterator<Enemy> enemyIt = enemies.iterator();
    
    // Iteramos la lista de enemigos
    while (enemyIt.hasNext()) {
      Enemy enemy = enemyIt.next();
      if (enemy.position.x < 0) {
        // Si la posición del enemigo se sale por la izquierda de la pantalla, lo quitamos de la lista
        enemyIt.remove();
      } else {
        // Lo movemos a la velocidad actual
        enemy.position.x-=terrainSpeed;
        if (enemy.enabled) {
          // Si está activo, calculamos su colisión con el pavo
          boolean collision = collision(turkey.position.x, turkey.position.y, turkey.width, turkey.height, enemy.position.x, enemy.position.y, enemy.animation.getImage().width, enemy.animation.getImage().height, 10);
          if (collision) {
            // Evitamos colisionar con el pavo si ya ha sido golpeado
            if (lastHitTime == 0) {
              // Actualizamos la marca de tiempo del ultimo golpe para hacer al pavo invulnerable unos instantes
              lastHitTime = getTimeElapsed();
              // Quitamos los puntos de vida correspondientes a la fuerza del enemigo
              addLifePoints(-enemy.strength);
            }
          }
        }
      }
    }

    // Rellenamos la lista de enemigos con nuevos enemigos que reemplacen a los eliminados
    fillWithEnemies();
  }

  // Método para calcular la colision entre dos cajas definidas por su posición y tamaño
  // La variable padding se usa para compensar el margen entre lo que haya pintado dentro de una
  // caja y su borde, permitiendo que dos cajas puedan coincidir hasta 2 x padding pixeles antes
  // de detectar la colisión
  private boolean collision(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2, int padding) {
    return (x1 + w1 - padding) >= (x2 + padding) && (x1 + padding) <= (x2 + w2 -padding) && (y1 + h1 - padding) >= (y2 - padding) && (y1 + padding <= y2 + h2 -padding);
  }
  
  // Pintamos el pavo
  public void drawTurkey() {
    if (DEBUG) {
      // Si está activo el flag de depuración, mostramos la caja de colisión
      gfx.noFill();
      gfx.strokeWeight(1);
      gfx.stroke(0, 255, 0);
      gfx.rect(turkey.position.x, turkey.position.y, turkey.width, turkey.height);
    }
    
    if (lastHitTime > 0) {
      // Si el pavo ha sido golpeado recientemente, lo ponemos en rojo pulsante
      int tint = (int)getTimeElapsed() / 100 % 2;
      gfx.tint(255*tint, 0, 0);
      gfx.image(turkey.currentAnimation.getImage(), turkey.position.x + PERSP_X_ADJUST, turkey.position.y);
      gfx.noTint();
    } else {
      // Pintamos en la posición del pavo la animación actual
      gfx.image(turkey.currentAnimation.getImage(), turkey.position.x + PERSP_X_ADJUST, turkey.position.y);
    }
  }

  // Actualizamos la posición y estado de todos los items
  public void updateItems() {
    
    Iterator<Item> itemIt = items.iterator();
    
    // Iteramos la lista de items
    while (itemIt.hasNext()) {
      Item item = itemIt.next();
      if (item.position.x < 0) {
        // Quitamos todos los items fuera de pantalla
        itemIt.remove();
      } else {
        if (item.touch == null && item.enabled == true) {
          // Si el item no ha sido recogido y está activo, comprobamos la colisión con el pavo
          if (collision(turkey.position.x, turkey.position.y, turkey.width, turkey.height, item.position.x, item.position.y, item.animation.getImage().width, item.animation.getImage().height, 10)) {
            // Si colisionan, determinamos el tipo de item y actuamos en consecuencia
            boolean touched = false;
            
            switch(item.type) {
            case Item.COIN:
              // Monedas: reproducimos el sample asociado, actualizamos el marcador y marcamos el item como 'tocado'
              Audio.playSample(Audio.GET_COIN);
              addScore(COIN_VALUE);
              touched = true;
              break;
            case Item.HEART:
              // Corazon: reproducimos el sample asociado, actualizamos los puntos de vida y marcamos el item como 'tocado'
              // Solo se recogen corazones si le falta vida al pavo
              if (getLifePoints()<INITIAL_LIFE_POINTS) {
                Audio.playSample(Audio.GET_HEART);
                addLifePoints(item.value);
                touched = true;
              }
              break;
            case Item.FLASK:
              // Viales: reproducimos el sample asociado, actualizamos los puntos de vuelo y marcamos el item como 'tocado'
              // Solo se recogen viales si le faltan puntos de vuelo al pavo
              if (powerPoints<INITIAL_POWER_POINTS) {
                Audio.playSample(Audio.GET_FLASK);
                addPowerPoints(item.value);
                touched = true;
              }
              break;
            }

            // Si el elemento ha sido tocado (se cumplen las precondiciones), establecemos el vector touch, que marcará
            // la nueva posición desde la que desaparecerá el item
            if (touched) {
              item.position.y = item.position.y-50;
              item.touch = new Vector(item.position.x, item.position.y-10);
            }
          }
        }
      }
      
      // Desplazamos a la izquierda todos los items
      item.position.x-=terrainSpeed;
      // Si el item ha sido recogido, lo subimos hasta que desaparezca
      if (item.touch != null) {
        if (item.touch.y > 0) item.touch.y-=10;
      }
    } 

    // Añadimos nuevos items si fuera necesario
    fillWithItems();
  }

  // Actualizamos la posición y estado de todas las cerillas
  public void updateMatches() {
    
    Iterator<Match> matchIt = matches.iterator();
    
    // Iteramos todos los elementos del vector
    while (matchIt.hasNext()) {     
      Match match = matchIt.next();

      if (match.position.x < 0) {
        // Quitamos todas las cerillas fuera de pantalla
        matchIt.remove();
      } else {
        // Desplazamos a la izquierda
        match.position.x-=terrainSpeed;
        
        // Si el calor de la cerilla llega al umbral máximo, la prendemos
        if (match.heat >= Match.MAX_HEAT) {
          // Sonido de llama
          Audio.playSample(Audio.FLAME);
          match.intensity = Match.MAX_INTENSITY;
          // Quitamos la marca de calor y la marcamos como usada
          match.heat = 0;
          match.used = true;
        }

        // Si la cerilla está prendida
        if (match.used && match.intensity > 0) {
          // Reducimos la intensidad
          match.intensity-=Match.INTENSITY_DECAY;

          // Propagamos el calor a las vecinas
          // Si hay elemento a izquierda, le pasamos calor
          int matchIndex = matches.indexOf(match);
          if ( matchIndex > 0) {
            Match left = matches.get(matchIndex-1);
            // Solo transmitmos calor a las cerillas no usadas y con cabeza
            if (left.used == false && !left.noFire) left.heat+=Match.HEAT_TRANSMISION;
          }
          // Si hay elemento a izquierda, le pasamos calor
          if ( matchIndex < matches.size() - 2) {
            Match right = matches.get(matchIndex+1);
            // Solo transmitmos calor a las cerillas no usadas y con cabeza
            if (right.used == false  && !right.noFire) right.heat+=Match.HEAT_TRANSMISION;
          }

          // Calculamos la altura de la llama de cara a aplicar la colisión
          int flameHeight = getFlameHeight(match.intensity);
          // Buscamos si hay colisión entre la caja que contiene al fuego y elpavo
          if (collision(match.position.x, match.position.y-flameHeight, MATCH_HEAD_RADIUS, flameHeight, turkey.position.x, turkey.position.y, turkey.width, turkey.height, 5)) {
            // Si el pavo es vulnerable
            if (lastHitTime == 0) {
              // Actualizamos la fecha de ataque y quitamos puntos de vida proporcionales a la intensidad
              lastHitTime = getTimeElapsed();
              addLifePoints(-(int)match.intensity/10);
            }
          }
        }
      }
    }
    
    // Añadimos mas cerillas si fuera necesario
    fillWithMatches();

    // 0.1% de probabilidad de combustión espontanea del camino
    if (random(1000) < 10) {
      // Incendiamos una cerilla random con la condición de que no sea una cerilla sin cabeza
      int mid = (int)random(matches.size()-1);
      if (!matches.get(mid).noFire) matches.get(mid).heat = Match.MAX_HEAT;
    }
  }

  // Pinta el cartel de fin de juego
  public void drawGameOver() {
    gfx.textSize(150);
    int w = (int)gfx.textWidth("GAME OVER");
    int c = getFireColor((int)(getTimeElapsed()%1000), 1000);
    gfx.fill(c);
    gfx.text("GAME OVER", (this.width-w)/2, height/2 - 25);
  }

  // Pinta los puntos de vida del pavo
  public void drawLifePoints() {
    gfx.noStroke(); 
    gfx.fill(200, 0, 0);
    gfx.rect(25, 25, lifePoints*3, 15);
    gfx.fill(255, 0, 0);
    gfx.rect(25, 25, lifePoints*3, 5);

    gfx.strokeWeight(3);
    // Si hay puntos pendientes de asignar, damos indicación visual
    int borderColor = (pendingLifePoints==0)?color(55, 55, 55):color(255, 255, 255);
    gfx.stroke(borderColor);

    gfx.noFill();
    gfx.rect(25, 25, INITIAL_LIFE_POINTS*3, 15);
  }
  // Pinta los puntos de vuelo del pavo
  public void drawPowerPoints() {
    gfx.noStroke(); 
    gfx.fill(0, 0, 255);
    gfx.rect(25, 50, powerPoints*3, 15);
    gfx.fill(0, 0, 255);
    gfx.rect(25, 50, powerPoints*3, 5);

    gfx.strokeWeight(3);
    // Si hay puntos pendientes de asignar, damos indicación visual
    int borderColor = (pendingPowerPoints==0)?color(55, 55, 55):color(255, 255, 255);
    gfx.stroke(borderColor);
    gfx.noFill();
    gfx.rect(25, 50, INITIAL_POWER_POINTS*3, 15);
  }

  // Gestionamos la posición del pavo
  public void updateTurkey(Match matchOver) {
    InputHandler input = getInputHandler();
    // Estado del botón izquierdo del raton
    int button = input.getMouseButtonState(InputHandler.MOUSE_LEFT);

    
    if (button == InputHandler.MOUSE_PRESSED) {
      // Si pulsado, el pavo debe volar
      fly = true;
    } else if (button == InputHandler.MOUSE_RELEASED) {
      // Si se suelta, el pavo debe aterrizar
      fly = false;
    }

    // Si el pavo debe volar, tiene vector  y puntos necesarios, le damos empuje
    if ( fly && vy >=0 && powerPoints > 0) {
      vy = FLY_THRUST;
      addPowerPoints(-2);
      // Cambiamos a la animación de vuelo
      turkey.playAnimation("pavo_salta");
    } else {
      // Decrementamos el vector de vuelo
      vy++;
    }
    // Actualizamos la altura del pavo con el vector de vuelo
    turkey.position.y+=vy;
    // Asignamos la posición x del pavo a la del ratón
    turkey.position.x = max(min(input.getMousePosition().x,this.width-turkey.currentAnimation.getImage().width-PERSP_X_ADJUST),0);

    if (turkey.position.y<0) {
      // Evitamos que el pavo se salga por la parte vertical de la pantalla
      turkey.position.y = 0;
    } else if (turkey.position.y>=matchOver.position.y-turkey.height) {
      // Si la posición del pavo es superior a la de la cerilla que cae debajo
      // hacemos que se pose sobre la cerilla. Como esto es probable que suceda despues de
      // terminar el vuelo, cambiamos a la animación correspondiente.
      turkey.position.y = matchOver.position.y-turkey.height;
      turkey.playAnimation("pavo_corre");
    }

    // Comprobamos la invulnerabilidad y la desactivamos cuando sea preciso
    if (getTimeElapsed() - lastHitTime >  BLINK_PERIOD_AFTER_HIT) {
      lastHitTime = 0;
    }
  }
  
  // Callback de actualización y redibujado del juego
  public void onStageUpdate(long delta) {
    // Limpiamos la pantalla
    clear();

    // Si el pavo no está muerto, actualizamos los elementos del escenario
    if (!dead) {
      updateItems();
      updateMatches();
      updateEnemies();
      updateScore();
      updateLifePoints();
      updatePowerPoints();
    }

    // Iteramos por todas las cerillas y las dibujamos una a una de izquierda a derecha
    // En cada iteración, pintamos el item y enemigo que haya activo en esa posición
    // Si el pavo se encuentra en esa cerilla, lo pintamos justo despues
    for (int c=0; c<matches.size(); c++) {
      
      Match match = matches.get(c);
      
      boolean drawTurkey = false;

      // Si lo hemos hecho bien, en la posicion c de cada vector
      // habrá un elemento en la misma posición que la cerilla
      Item item = items.get(c);
      Enemy enemy = enemies.get(c);

      // Calculamos si el pavo está sobre esta cerilla comprobando
      // que su posición x es mayor que la de la cerilla en curso pero inferior
      // a la cerilla siguiente
      if (turkey.position.x>=match.position.x) {
        if (c<(matches.size()-1) && turkey.position.x < matches.get(c+1).position.x) {
          drawTurkey=true;
          // Actualizamos al pavo sabiendo la cerilla en la que se encuentra
          updateTurkey(matches.get(c));
        }
      }
      // Dibuja la cerilla
      drawMatch(match);
      // Si hay  que pintar al pavo y está vivo, lo pintamos
      if (drawTurkey && !dead) {
        drawTurkey();
      }
      
      // Pintamos solo si esta activo
      if (item.enabled) {
        drawItem(item);
      }
      // Pintamos solo si esta activo
      if (enemy.enabled) {
        drawEnemy(enemy);
      }
    }

    // Terminamos de pintar los elementos de frente
    drawScore();
    drawFire();
    drawLifePoints();
    drawPowerPoints();

    // Si el pavo ha muerto, mensaje de fin de juego
    if (dead) {
      drawGameOver();
    }

    // Actualizamos el tiempo de animación del pavo con la diferencia de tiempo de la última ejecución
    turkey.currentAnimation.nextFrame(delta);

    // Actualizamos todas las animaciones de items con la diferencia de tiempo de la última ejecución
    for (int c=0; c<itemAnimations.size(); c++) {
      itemAnimations.get(c).nextFrame(delta);
    }
    // Actualizamos todas las animaciones de enemigos con la diferencia de tiempo de la última ejecución
    for (int c=0; c<enemyAnimations.size(); c++) {
      enemyAnimations.get(c).nextFrame(delta);
    }

    // Para terminar, incrementamos la velocidad del juego una fracción cada cinco segundos, haciendo la dificultad progresiva
    terrainSpeed = SPEED + (getTimeElapsed() / 5000);
  }
}
/**
 * Vector - Encapsula dos coordenadas escalares en un único valor
 */
class Vector {
  // Coordenadas
  int x, y;
  // Constructor
  Vector() {
    x = 0; y = 0;
  }
  // Constructor con datos
  Vector(int x, int y) {
    this.x = x;
    this.y = y;
  }
  // Determina si un vector se encuentra contenido en el rectángulo definido por dos esquinas
  public boolean isInside(Vector topLeft, Vector bottomRight) {
    return x >= topLeft.x && x <= bottomRight.x &&  y >= topLeft.y && y < bottomRight.y;
  }
}

/**
 * Clock - Controla el tiempo transcurrido entre dos ejecuciones
 */
class Clock {
  
  int time_now;
  int time_old;
  int time_delta_millis;
  float time_delta_sec;

  // constructor: object initialization
  Clock() {
    time_now = 0;
    time_old = 0;
    time_delta_millis = 0;
  }

  // executed everytime inside draw to calculate elapsed time since last execution
  public void update() {
    time_now = millis();
    time_delta_millis = time_now - time_old;
    time_old = time_now;

    time_delta_sec = time_delta_millis / 1000.0f;
  }

  // return mseconds elapsed since last execution
  public int getDeltaMillis() {
    return time_delta_millis;
  }

  // return seconds elapsed since last execution
  public float getDeltaSec() {
    return time_delta_sec;
  }
}

/**
 * Timer - Cronometro
 *
 * Esta clase no está actualmente en uso, las instancias de Stage tienen su propio control de tiempo
 */

class Timer {
  // Dirección adelante / cuentra atrás
  boolean up;
  // Tiempo de inicio
  int startPoint;
  // Tiempo de fin
  int endPoint;
  // Flag de parada
  boolean counting;
  // Tiempo transcurrido
  int currentTime;
  // Inicialización
  Timer() {
    up = true;
    startPoint = 0;
    endPoint = 0;
    counting = false;
    currentTime = startPoint;
  }
  
  Timer (int sPoint, int ePoint) {
    setPoints(sPoint,ePoint);
  }

  public void reset () {
    currentTime = startPoint;
  }

  public void setPoints (int sPoint, int ePoint) {
    startPoint = sPoint;
    endPoint = ePoint;

    up = ePoint >= sPoint;
    
    reset();
  }

  public void update (int millis) {
    if(counting) {
      if(up) {
        currentTime+=millis;
      }else{
        currentTime-=millis;
      }
    }
  }

  public void start (boolean onoff) {
    counting = onoff;
  }

  public int getTimeMillis () {
    return currentTime;
  }

  public float getTimeSec () {
    return (currentTime / 1000.0f);
  }

  public boolean getFinish() {
    return counting || (up && currentTime >= endPoint) || currentTime <= startPoint;
  }
}
/**
 * Gestión de la entrada de datos
 *
 * Esta clase no es estrictamente necesaria, ya que Processing pone a disposición
 * del usuario todas las variables de estado de forma global. No obstante, y de cara
 * a la legibilidad, la gestión del teclado y del ratón se hará desde aquí.
 *
 * Gestión del ratón - Cada vez que un evento de ratón se detecta vía mousePressed o mouseReleased
 * se comunica a una instancia de esta clase para almacenar el estado (*interrupciones*). Este permanecerá así hasta
 * que un nuevo evento modifique el estado previo o se resetee el estado al final de cada ciclo
 * de ejecución. Aunque Processing suministra el evento mouseClicked, en principio no es necesario, ya que
 * la interfaz es capaz de determinar este hecho.
 *
 * Gestión del teclado - No ha sido necesario gestionar entrada por teclado, por lo que queda pendiente
 * para la siguiente iteración
 * 
 */


class InputHandler {
  // Estado de botón de ratón pulsado
  public final static int MOUSE_PRESSED = 1;
  // Estado de botón de ratón liberado
  public final static int MOUSE_RELEASED = 2;
  // Estado de tecla pulsada
  public final static int KEY_PRESSED = 1;
  // Estado de tecla liberada
  public final static int KEY_RELEASED = 2;
  
  // Índice del botón izquierdo
  public final static int MOUSE_LEFT = 0;
  // Índice del botón central
  public final static int MOUSE_MIDDLE = 1;
  // Índice del botón derecho
  public final static int MOUSE_RIGHT = 2;
  // Estado de los botones del ratón
  int[] mouseButtonState;
  // Estado del movimiento del ratón
  boolean mouseMove;
  
  Map<Integer,Integer> keyboardState;
  
  // Constructor
  InputHandler() {
    // Inicialmente los tres botones están en un estado ni pulsado, ni soltado
    mouseButtonState = new int[]{0,0,0};
    keyboardState = new HashMap<Integer,Integer>();
  }
  // Procesa un evento de pulsación de raton
  public void mousePressed(int button){
    int number = 0;
    
    // Determinamos el índice del vector de botones en función del botón pulsado
    if(button == LEFT){
      number = MOUSE_LEFT;
    }else if(button == RIGHT){
      number = MOUSE_RIGHT;
    }else{ // CENTER
      number = MOUSE_MIDDLE;
    }
    // Establecemos el estado de este botón a PULSADO
    mouseButtonState[number] = MOUSE_PRESSED;
  }
  // Procesa un evento de liberación de botón pulsado
  public void mouseReleased(int button){
    int number = 0;
      
    // Determinamos el índice del vector de botones en función del botón pulsado
    if(button == LEFT){
      number = MOUSE_LEFT;
    }else if(button == RIGHT){
      number = MOUSE_RIGHT;
    }else{ // CENTER
      number = MOUSE_MIDDLE;
    }
    // Establecemos el estado de este botón a SOLTADO
    // anulando el estado anterior
    mouseButtonState[number] = MOUSE_RELEASED;
  }
  
  public void mouseMoved(){
     mouseMove = true;
  }
  
  // Devuelve el estado en el que se encuentra el botón solicitado
  public int getMouseButtonState(int button){
    return mouseButtonState[button];
  }
  
  public void keyPressed(int keycode){
    keyboardState.put(keyCode,KEY_PRESSED); 
  }
  
  public void keyReleased(int keycode){
    keyboardState.put(keyCode,KEY_RELEASED); 
    println("released "+keycode);
  }
  
  public boolean isKeyPressed(int code){
     return keyboardState.containsKey(code) && keyboardState.get(code) == KEY_PRESSED;
  }
  
  public List<Integer> getPressedKeys(){
    List<Integer> pressedKeys = new ArrayList<Integer>();
    for(Integer i:keyboardState.keySet()){
      if(keyboardState.get(i) == KEY_PRESSED){
         pressedKeys.add(i); 
      }
    }
    
    return pressedKeys;
  }
  // Borra todos los estados de los botones del ratón. Este
  // procedimiento se invoca en cada ciclo de procesado.
  public void clearInput(){
     for(int i:mouseButtonState){
       mouseButtonState[i] = 0; 
     }
     Iterator<Integer> keybIt = keyboardState.keySet().iterator();
     while(keybIt.hasNext()){
       int code = keybIt.next(); 
       if(keyboardState.get(code) == KEY_RELEASED){
         keybIt.remove(); 
       }
     }
     mouseMove = false;
  }
  // Obtiene la información de la posición del ratón
  public Vector getMousePosition() {
    // Tomando las variables globales aquí no
    // es muy diferente a hacerlo en los consumidores
    return new Vector(mouseX, mouseY);
  }
  
  public boolean isMouseMoving(){
    return mouseMove; 
  }
}


///////////////////
//
// ETAPAS DE JUEGO
//
///////////////////
//
// El flujo de ejecución del juego se ha separado en entidades aisladas denominadas
// "Stages". Cada Stage o etapa tiene su propia superficie de dibujado, y acceso al
// manejador de entrada de usuario. Se pueden entender como pequeños subprogramas cuyo
// ciclo de vida es gestionado desde un "coordinador" a traves de sus estados, con un 
// guión maestro que le dicta cómo deben hacerse las transiciones entre las diferentes etapas.

// Diferentes estados de una etapa
enum StageState {
  // Estado no inicializado
  Undefined, 
    // Estado comenzado
    Started, 
    // Estado pausa (actualmente no en uso)
    Paused, 
    // Estado finalizado
    Ended
}

/**
 * Stage - Unidad lógica del juego
 *
 */
abstract class Stage {
  // Estado en que se encuentra la etapa
  private StageState state;
  // Tiempo transcurrido desde que se inició
  private long timeElapsed;
  // Cada etapa tiene su propia superficie de redibujado
  // de manera que se puedan ejecutar técnicas de double buffering
  // o blending entre escenas
  private PGraphics graphics;
  // Cada etapa tiene acceso al controlador de la entrada
  private InputHandler input;
  // Ancho y alto de la etapa. Puede parecer que tiene poco sentido
  // establecer estos valores aquí, pero las etapas son cada una
  // de las escenas del juego, y tienen que ser conscientes del tamaño
  // de la pantalla. La solución pasa por crear variables globales por
  // todas partes, o singletons con los valores por defecto, o propagar 
  // los valores como aquí.
  protected int width, height;
  // Referencia al applet principal, necesario para inicialización de audio/ui
  protected final PApplet applet;

  Stage(PApplet applet) {
    this.applet = applet;
    // Estado inicial indefinido
    state = StageState.Undefined;
    // Inicialización del tiempo transcurrido
    timeElapsed = 0;
  }

  // Establece el tamaño de la etapa
  public void setSize(int width, int height) {
    this.width = width;
    this.height = height;
  }
  // Establece el controlador de la entrada
  public void setInputHandler(InputHandler input) {
    this.input = input;
  }
  // Obtiene el controlador de la entrada
  public InputHandler getInputHandler() {
    return this.input;
  }
  // Obtiene el estado actual de la etapa
  public StageState getState() {
    return state;
  }
  // Obtiene el número de milisegundos transcurridos desde el inicio
  final public long getTimeElapsed() {
    return timeElapsed;
  }
  // Obtiene la superficie gráfica de esta etapa
  final public PGraphics getGraphics() {
    return graphics;
  }
  /*
   * Punto de entrada de la etapa. Cuando el coordinador
   * de etapas determine que esta etapa debe entrar en escena
   * invocará este método. Las clases hijas son responsables
   * de sobreescribir el método onStateBegin() para inyectar
   * su funcionalidad
   */
  public final void begin() {
    // Cambio de estado a "empezada"
    this.state = StageState.Started;
    // Creamos una nueva superficie gráfica con el tamaño apropiad
    this.graphics = createGraphics(width, height);

    println("Stage "+this.getClass().getName()+" starts");
    // Delegamos la funcionalidad
    this.onStageBegin();
  }
  /*
   * Este método debe ser invocado para que la etapa 
   * cambie de estado, bien sea desde el coordinador 
   * para forzar la salida, o desde una clase hija para
   * informar que ha terminado su trabajo
   */
  public final void end() {
    // Cambio de estado a "terminada"
    this.state = StageState.Ended ;
    // Delegamos la funcionalidad en el hijo
    this.onStageEnd();
    // Informamos del tiempo de ejecución
    println(this.getClass().getName()+" alive for "+timeElapsed+" millis");
  }

  /*
   * Este método se encarga de efectuar el siguiente
   * ciclo de actualización de la etapa, alimentando a esta
   * con la diferencia de tiempo desde la última ejecución.
   */
  public final void update(long delta) {
    // No hay nada que actualizar si no ha sido inicializada
    if (this.state == StageState.Started) {
      // Actualizamos el tiempo pasado
      this.timeElapsed += delta;
      // https://processing.org/reference/PGraphics_beginDraw_.html
      /*
       * Fuera de la función principal draw() del script base, parece
       * que hay que invocar estas dos funciones para notificar al contexto
       * gráfico que ya estamos dispuestos para pintar, y que finalmente hemos
       * terminado. Para evitar hacer esto en cada clase hija, prohibimos
       * la reescritura de este método y delegamos la funcionalidad en
       * onStageUpdate(), debidamente envuelto por beginDraw() y endDraw()
       */
      graphics.beginDraw();
      this.onStageUpdate(delta);
      graphics.endDraw();
    }
  }
  /*
   * Callback de inicialización, invocado al pasar a Started 
   */
  public void onStageBegin() {
    // Este método puede ser sobreescrito por las clases hijas para capturar
    // el comienzo de la etapa
    println(this.getClass().getName()+" stage begin");
  }
  /*
   * Callback de finalización, invocado al pasar a Ended
   */
  public void onStageEnd() {
    // Este método puede ser sobreescrito por las clases hijas para capturar
    // el fin de la etapa
    println(this.getClass().getName()+" stage end");
  }

  /*
   * Los callback de estado pueden (o no) ser capturados por las clases hijas, pero
   * onStageUpdate() debe ser implementado a la fuerza. Para esto, marcamos la clase
   * y el método como abstractos.
   */
  public abstract void onStageUpdate(long delta);
}

/**
 * GreetingStage - Etapa inicial, presentación
 *
 * Esta clase presenta el mensaje de bienvenida al usuario
 */
class GreetingStage extends Stage {
  // Duración de la presentación
  long duration;
  PImage image;
  // Constructor con parámetros
  GreetingStage(PApplet applet, long duration) {
    super(applet);
    this.duration = duration;
  }
  // Callback de inicialización
  public void onStageBegin() {
    image = loadImage(ASSETS_PATH+"match.jpg");
  }
  // Callback de actualizacion
  public void onStageUpdate(long delta) {
    // Referencia a la superfici de dibujo
    PGraphics gfx = this.getGraphics();
    gfx.image(image, 0, 0);

    // Etiquetas que se visualizarán
    String label1 = "Hell's Matches les desea feliz navidad";
    String label2 = "Prendiendo el asunto desde 1820";

    // Establecemos el tamaño de letra
    gfx.textSize(55);
    // Pintamos la primera etiqueta en rojo
    gfx.fill(255, 0, 0);
    gfx.text(label1, (2*this.width/3-gfx.textWidth(label1))/2, height/2 - 100);
    // Pintamos la primera etiqueta en amarillo
    gfx.fill(255, 255, 0);    
    gfx.text(label2, (2*this.width/3-gfx.textWidth(label2))/2, height/2 + 100);



    // Verificamos la condición de salida e invocamos la finalización
    if (this.getTimeElapsed() >= duration) {
      this.end();
    }
  }
}

/**
 * MenuStage - Menú de opciones con interacción con el usuario
 *
 * Este menú presenta dos sets de interfaz (principal y configuración), y 
 * alterna entre ellos en función de la entrada del usuario
 */
class MenuStage extends Stage {
  // El usuario no ha seleccionado nada
  public final static int NONE = 0;
  // El usuario ha seleccionado RUN
  public final static int RUN = 1;
  // El usuario ha seleccionado EXIT
  public final static int EXIT = 2;
  // Elección dle usuario
  private int playerChoice;

  // Fuente grande para las etiqutas
  ControlFont bigFont;
  // Fuente mediana para las etiqutas
  ControlFont regularFont;
  // Capa de interfaz principal
  ControlP5 menuMain;
  // Capa de interfaz de configuración
  ControlP5 menuConfig;
  // Determina el menú activo en cada situación
  ControlP5 menuActive;
  // Referencia al control con el ratón encima
  Object controlWithMouseOver;

  MenuStage(PApplet applet) {
    super(applet);
  }

  public void onStageBegin() {
    // Precargamos las imágenes asociadas a los botones que vamos a incluir
    loadButtonImages(new String[]{"btn-config", "btn-run", "btn-exit", "btn-music1", "btn-music2", "btn-music3", "btn-image1", "btn-image2", "btn-image3", "btn-back"});
    // Tamaño por defecto de todos los botones
    int buttonWidth = 150;
    
    // Inicializamos las fuentes para etiquetas y botones
    bigFont = new ControlFont(createFont(ASSETS_PATH+"BebasNeue Bold.ttf", 40));
    regularFont = new ControlFont(createFont(ASSETS_PATH+"BebasNeue Bold.ttf", 25));

    // Creamos la capa de interfaz del menu principal
    menuMain = new ControlP5(applet);
    // Desactivamos el menu temporalmente
    enableMenu(menuMain, false);
    // Añadimos los tres botones del menú principal y los vinculamos a los métodos onConfigClick, onrunClick y onExitClick
    createImageButton(this, menuMain, "onConfigClick", "btn-config", (this.width-buttonWidth)/2, 100).setValue(0);
    createImageButton(this, menuMain, "onRunClick", "btn-run", (this.width-buttonWidth)/2, 175).setValue(0);
    createImageButton(this, menuMain, "onExitClick", "btn-exit", (this.width-buttonWidth)/2, 250).setValue(0);

    // Creamos la capa de interfaz del menu de configuracion
    menuConfig = new ControlP5(applet);
    // Desactivamos el menu temporalmente
    enableMenu(menuConfig, false);
    // Añadimos la etiqueta de cambio de música
    menuConfig.addLabel("Cambiar música", 25, 50).setFont(bigFont);
    // Creamos los botones del panel de configuracion
    createImageButton(this, menuConfig, "onMusicButtonClick1", "btn-music1", 75, 100);
    createImageButton(this, menuConfig, "onMusicButtonClick2", "btn-music2", 235, 100);
    createImageButton(this, menuConfig, "onMusicButtonClick3", "btn-music3", 395, 100);
    // Control de volumen
    CColor sliderColor = new CColor(color(190, 34, 0), color(0, 0, 0, 100), color(254, 180, 38), color(255, 255, 255), color(255, 255, 255));
    menuConfig.addSlider(this, "onVolumeChange").setPosition(new float[]{75, 175}).setSize(400, 50).setLabel("Volumen").setFont(regularFont).setColor(sliderColor).setValue(50);
    // Sección de imagen de fondo
    menuConfig.addLabel("Cambiar imagen", 25, 275).setFont(bigFont);
    createImageButton(this, menuConfig, "onImageButtonClick1", "btn-image1", 75, 325);
    createImageButton(this, menuConfig, "onImageButtonClick2", "btn-image2", 235, 325);
    createImageButton(this, menuConfig, "onImageButtonClick3", "btn-image3", 395, 325);

    // Botón para cambiar entre UIs
    createImageButton(this, menuConfig, "onBackClick", "btn-back", 25, 500).setValue(0);

    // Establecemos por defecto el menú principal como capa de interfaz activa
    enableMenu(menuMain, true);

    // Establecemos NADA como selección del usuario
    playerChoice = NONE;
  }

  // Habilita o deshabilita un conjunto de controles, así como la propagación de sus eventos
  private void enableMenu(ControlP5 menu, boolean enabled) {
    menu.setVisible(enabled);
    menu.setBroadcast(enabled);
    if (enabled) {
      menuActive = menu;
    }
  }
  
  // Callback del control deslizador
  public void onVolumeChange(float v) {
    Audio.setVolume(v);
  }
  
  // Callback del botón CONFIG
  public void onConfigClick(int theValue) {
    enableMenu(menuMain, false);
    enableMenu(menuConfig, true);
  }

  // Callback del botón RUN
  public void onRunClick(int v) {
    playerChoice = RUN;
    end();
  }
  
  // Callback del botón EXIT
  public void onExitClick(int v) {
    playerChoice = EXIT; 
    end();
  }

  // Callback del botón VOLVER
  public void onBackClick(int v) {
    enableMenu(menuMain, true);
    enableMenu(menuConfig, false);
  }

  // Callback de los botones de cambio de imagen de fondo
  public void onImageButtonClick1() { 
    appCfgBackgroundImage = "a";
  }
  public void onImageButtonClick2() { 
    appCfgBackgroundImage = "b";
  }
  public void onImageButtonClick3() { 
    appCfgBackgroundImage = "c";
  }
  // Callback de los botones de cambio de hilo musical
  public void onMusicButtonClick1() { 
    Audio.playTheme(Audio.THEME_1);
  }
  public void onMusicButtonClick2() { 
    Audio.playTheme(Audio.THEME_2);
  }
  public void onMusicButtonClick3() { 
    Audio.playTheme(Audio.THEME_3);
  }

  // Este metodo se invoca siempre que un control emite un evento 
  public void controlEvent(ControlEvent event) {
    // En cada evento reproducimos el sample CLICK, funciona también en el cambio de valor del slider
    Audio.playSample(Audio.BUTTON_CLICK);
  }
  
  // Callback de actualizacion
  public void onStageUpdate(long delta) {
    // Si el ratón está sobre algún control y antes no lo estaba, reproducimos un sample para el evento OVER
    // Despues, nos quedamos con una referencia a dicho control para no ejecutar el sonido mas de una vez
    if (menuActive.getMouseOverList().size() > 0 && controlWithMouseOver == null) {
      controlWithMouseOver = menuActive.getMouseOverList().get(0);
      Audio.playSample(Audio.BUTTON_OVER);
    } else if (menuActive.getMouseOverList().size() == 0) {
      controlWithMouseOver = null;
    }

    PGraphics gfx = getGraphics();
    PImage backgroundImage = getBackgroundImage(appCfgBackgroundImage);

    gfx.image(backgroundImage, (this.width-backgroundImage.width)/2, (this.height-backgroundImage.height)/2);
  }

  public void onStageEnd() {
    // Deshabilitamos ambos menus antes de salir
    enableMenu(menuMain, false);
    enableMenu(menuConfig, false);
  }
  
  // Obtiene la elección del usuario
  public int getPlayerChoice() {
    return playerChoice;
  }
}

/**
 * TransitionStage - Metaetapa de transición entre dos etapas
 *
 * Esta etapa sirve como paso intermedio entre dos etapas cualesquiera, creando
 * un efecto de fundido a negro entre una y otra. Para que el efecto funcione
 * la etapa previa y la siguiente deben tener sus superficies de dibujado pintadas
 * para que el efecto de transición sea apreciable.
 */
class TransitionStage extends Stage {
  // Modo de transición "fundido a negro"
  final static int FADE_BLACK = 0;
  // Flag de transición "fundido a blanco"
  final static int FADE_WHITE = 1;
  // Flag d etransición "mezcla"
  final static int FADE_BLEND = 2;
  // Referencias a las etapas anterior y siguiente
  Stage next, prev;
  // Tipo de transición que se aplicará
  int transitionType;
  long duration;
  // Constructor: Recibe una escena terminada y una pendiente de inicializar para crear
  // una transición entre ambas definida por transitionType
  TransitionStage(PApplet applet, Stage prev, Stage next, int transitionType, long duration) {
    super(applet);
    // Establecemos cuanto durará la transición, ajustando así la velocidad del fundido
    this.duration = duration;
    // Guardamos las referencias a las etapas
    this.next = next;
    this.prev = prev;
    // Establecemos el tipo de transición
    this.transitionType = transitionType;
  }
  /* Callback de inicialización
   * Esta metaetapa es responsable de inicializar la etapa siguiente
   * si no lo estuviera, obteniendo así una superficie de dibujo 
   * pintada e inicializada
   */
  public void onStageBegin() {
    if (next.getState() != StageState.Started) {
      next.setSize(this.width, this.height);
      next.setInputHandler(this.getInputHandler());
      next.begin();
    }
  }
  // Callback de actualización
  public void onStageUpdate(long delta) {
    // Obtenemos una referencia ala superficie de dibujo
    PGraphics gfx = this.getGraphics();
    // Obtenemos el tiempo transcurrido
    long elapsed = getTimeElapsed();
    // Empleamos el 50% de la duración para el fundido a negro
    long fadeoutDuration = duration / 2;
    // El 50% restante para el fundido desde negro
    long fadeinDuration = duration / 2;
    // Usaremos un velo negro con opacidad variable para controlar el fundido
    int opacity = 0;

    // Usando el tiempo transcurrido como referencia, empleamos el 50%
    // de la duración en hacer el fundido a negro
    if (elapsed <= fadeoutDuration) {
      // La primera escena ya ha terminado, solo necesitamos tomar su último fotograma para hacer la interpolación

      // Reducimos progresivamente la opacidad de 256 (max) a 0
      opacity = (int) ((256 * elapsed) / fadeoutDuration);

      // Empezamos dibujando la superficie de dibujo de la etapa previa en la superficie de esta etapa
      gfx.image(prev.getGraphics(), 0, 0);
      // Pintamos un velo negro con la opacidad establecida
      gfx.fill(0, 0, 0, opacity);
      gfx.rect(0, 0, this.width, this.height);
    } else if ( (elapsed-fadeoutDuration) < fadeinDuration) {
      // La segunda mitad de la duración, hacemos el proceso inverso

      // Calculamos la opacidad para que vaya desde 256 hasta 0
      opacity = 256 - (int) ((256 * (elapsed-fadeoutDuration) / fadeinDuration));

      // Esta linea es opcional, si no se ejecuta la etapa quedará congelada en el tiempo
      // mientras dura la transicion. Si se invoca, la etapa siguiente será procesada con 
      // normalidad.
      next.update(delta);

      // Pintamos la superficie de dibujo de la etapa siguiente en la superficie de esta capa
      gfx.image(next.getGraphics(), 0, 0);
      // Y terminamos pintando el velo a la opacidad elegida
      gfx.fill(0, 0, 0, opacity );
      gfx.rect(0, 0, this.width, this.height);
    } else {
      // Una vez superada la duración, invocamos la finalización de la etapa
      end();
    }
  }
  // Obtiene la referencia a la etapa siguiente 
  public Stage getNextStage() {
    return next;
  }
  // Obtiene la referencia a la etapa anterior
  public Stage getPrevStage() {
    return prev;
  }
}

/**
 * EndStage - Felicitación de fin de programa
 *
 * Esta clase contiene la misma funcionalidad que
 * GreetingStage. Solo existe como clase separada
 * porque la forma en la que el controlador de etapas
 * funciona requiere que haya solo una clase por cada
 * etapa, ya que usa este valor para discriminarlas. Se
 * podría haber hecho suministrando un identificador 
 * en el constructor, pero en general sólo va a haber una
 * etapa de cada tipo. En caso contrario, habría que cambiar
 * la operativa.
 */
class EndStage extends GreetingStage {
  // Invocamos el constructor del padre
  EndStage(PApplet applet, long duration) {
    super(applet, duration);
  }
}

/**
 * StageController - Controla el ciclo de vida de las etapas y orquesta la navegación
 *
 * Esta clase se encarga de gestionar el orden en eque las diferentes escenas deben activarse,
 * así como ejecutar sus métodos de servicio para actualizar gráficos y leer la entrada de
 * usuario.
 */
class StageController {
  // Instancia del controlador de entrada
  InputHandler input;
  // Etapa en ejecución, solo puede haber una etapa en ejecución
  Stage stage;
  // Etapa siguiente, estará en preparación para
  // ejecutar en el siguiente ciclo mientas termina
  // la actual
  Stage prevStage;
  // Tiempo transcurrido, actualmente sin uso
  long elapsed;
  // Tamaño de la ventana, necesario para instanciar las etapas
  int screenWidth, screenHeight;
  // Referencia al applet principal
  PApplet applet;

  // Constructor
  StageController(PApplet applet, int screenWidth, int screenHeight) {
    this.applet = applet;

    this.screenWidth = screenWidth;
    this.screenHeight = screenHeight;
    // Creamos una nueva instancia del gestor de e/s
    this.input = new InputHandler();
    // Inicialmente no hay ninguna etapa asignada
    this.stage = null;
    this.prevStage = null;
  }
  // Obtiene la instancia del gestor de e/s
  public InputHandler getInputHandler() {
    return input;
  }

  // Este método se encarga de invocar al método de actualización
  // de la etapa en curso, alimentándolo con la delta de tiempo
  // transcurrido desde la última ejecución
  public void procesStageLifecycle(long delta) {
    // Actualmente elapsed no se usa, lo guardamos para el futuro
    elapsed+=delta;
    // En el primer ciclo de ejecución no hay ninguna etapa cargada,
    // evitamos un NPE
    if (stage != null) {
      // Actualizamos la etapa
      stage.update(delta);
    }
  }
  /*
   * Este método se encarga de gestionar cómo las etapas 
   * se suceden entre sí. En general, crea una etapa inicial
   * si no hay ninguna programada y a partir de ahí, comprueba
   * en cada iteración si la etapa en curso ha terminado. De ser así
   * comprueba la clase a la que pertenece y establece la siguiente 
   * etapa a ejecutar. La gestion de todas las etapas es mas o menos
   * secuencial salvo en el menú. En esta etapa valoraremos qué decisión
   * tomar en base a la decisión (botón) que tomó el usuario (RUN o EXIT)
   */
  public void processStageSequence() {
    if (stage == null) {
      // 1.- Si no se ha establecido una escena, comenzamos con el mensaje de bienvenida
      setNextStage(new GreetingStage(applet, 5000));
      //setNextStage(new GameStage(applet));
    } else if (stage.getState() == StageState.Ended) {
      // El cambio de una escena a otra se hace solo cuando el controlador
      // comprueba que la etapa en curso ha terminado

      if (stage.getClass() == GreetingStage.class) {
        // Cambio GREETING->MENU: Si la escena cargada es la pantalla de bienvenida, pasamos a la pantalla de menú
        setNextStage(new TransitionStage(applet, stage, new MenuStage(applet), TransitionStage.FADE_BLACK, 1000));
      } else if (stage.getClass() == TransitionStage.class) {
        // Cambio PREV -(fade)-> NEXT
        // Si la escena es una transición, pasamos a la escena que tenga configurada como siguiente
        // Para acceder a los métodos propios de la transición, es necesario hacer un cast a su tipo específico
        TransitionStage transition = (TransitionStage) stage;
        setNextStage( transition.getNextStage() );
      } else if (stage.getClass() == GameStage.class) {
        // Cambio GAME -> MENU
        // Si la etapa cargada es la de juego, volvemos a cargar la pantalla de menú
        setNextStage(new TransitionStage(applet, stage, new MenuStage(applet), TransitionStage.FADE_BLACK, 1000));
      } else if (stage.getClass() == EndStage.class) {
        // Cambio GREETING->fin
        // EndStage es el punto de salida, cerramos la aplicacion cuando haya pasado el tiempo estipulado
        exit();
      } else if (stage.getClass() == MenuStage.class) {
        // Al terminar la etapa de menú, la elección del jugador queda disponible, la evaluamos
        MenuStage menu = (MenuStage)stage;
        switch(menu.getPlayerChoice()) {
        case MenuStage.RUN:
          // RUN: El jugador quiere empezar a jugar
          // Creamos una transición de 1sg de fundido a negro que de paso a la etapa de juego
          setNextStage(new TransitionStage(applet, stage, new GameStage(applet), TransitionStage.FADE_BLACK, 1000));
          break;
        case MenuStage.EXIT:
          // EXIT: El jugador quiere salir de la aplicación
          // Creamos una transición de 1sg de fundido a negro que de paso a la tarjeta de despedida
          setNextStage(new TransitionStage(applet, stage, new EndStage(applet, 5000), TransitionStage.FADE_BLACK, 1000));
          break;
        }
      }
    }
  }

  /* Este método establece e inicializa la siguiente
   * etapa que sucederá a la actual que acaba de 
   * terminar.
   */
  private void setNextStage(Stage next) {
    // Le pasamos el control de e/s a la nueva etapa
    next.setInputHandler(this.input);
    // Configuramos su tamaño
    next.setSize(screenWidth, screenHeight);
    // Le indicamos que comience para que se inicialice
    if (next.getState() == StageState.Undefined) next.begin();
    // Actualizamos la referencia de la etapa en curso
    // a la nueva etapa, dejando que la antigua
    // pase al recolector de basura.
    stage = next;
  }

  // Obtiene la referencia a la etapa en curso
  public Stage getStage() {
    return stage;
  }
}
//
// INTERFAZ DE USUARIO
//


// Ancho de los botones
private final static int BTN_IMAGE_WIDTH = 150;
// Alto de los botones
private final static int BTN_IMAGE_HEIGHT = 50;
// Lista de imagenes asociadas a cada estado, por ID de botón
private static final Map<String, List<PImage>> buttonImages = new HashMap<String, List<PImage>>();
// Lista de imagenes de fondo, por nombre
private static final Map<String, PImage> backgroundImages = new HashMap<String,PImage>();

// Método de servicio que busca todas las imágenes de fondo (background_*) y las carga en memoria
public void loadBackgroundImages() {
  File dir = new File(sketchPath(ASSETS_PATH));
  for(String file:dir.list()){
    if(file.startsWith("background")){
      String bgId = file.substring("background-".length(),file.length()-4);
      backgroundImages.put(bgId,loadImage(ASSETS_PATH+file));
      println("Background "+ASSETS_PATH+file+" loaded");
    }
  }
}

// Devuelve una imagen de fondo dado su identificador
public PImage getBackgroundImage(String bgId){
  return backgroundImages.get(bgId); 
}

// Precarga en memoria todas las imágenes de estado de un botón
public void loadButtonImages(String[] btnNames) {
  // Este método espera que haya tres imágenes (una por estado) para cada nombre de botón
  for (String btnName : btnNames) {
    if (!buttonImages.containsKey(btnName)) {
      List<PImage> btnImages = new ArrayList<PImage>();
      btnImages.add(loadImage(ASSETS_PATH+btnName+"_normal.png"));
      btnImages.add(loadImage(ASSETS_PATH+btnName+"_over.png"));
      btnImages.add(loadImage(ASSETS_PATH+btnName+"_highlight.png"));
      buttonImages.put(btnName, btnImages);
    }
  }
}

// Devuelve un botón con imágenes de fondo
public static Button createImageButton(Object listener, ControlP5 control, String handler, String btn, int x, int y) {
  // Localizamos las imagenes precargadas
  List<PImage> images = buttonImages.get(btn);
    
    return control
      .addButton(listener, handler)
      .setImages(images.get(0), images.get(1), images.get(2))
      .setSize(BTN_IMAGE_WIDTH, BTN_IMAGE_HEIGHT)
      .setPosition(x, y);
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Main" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
