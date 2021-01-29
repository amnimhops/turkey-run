import controlP5.*;

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
void settings() {
  // Establecemos el tamaño de la pantalla al ancho y alto definidos
  size(SCREEN_WIDTH, SCREEN_HEIGHT);
}

// El callback settings() se invoca antes de que se cree la ventana, por lo que
// tenemos que establecer aquí el número de fotogramas por segundo
void setup() {
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
void draw() {
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


void mousePressed(){
  // Informamos al controlador del ratón de que se ha presionado un botón
  controller.getInputHandler().mousePressed(mouseButton);
}

void mouseReleased(){
  // Informamos al controlador del ratón de que se ha soltado un botón
  controller.getInputHandler().mouseReleased(mouseButton);
}

void mouseMoved(){
  controller.getInputHandler().mouseMoved();
}

void keyPressed(){
  controller.getInputHandler().keyPressed(keyCode);
}
void keyReleased(){
  controller.getInputHandler().keyReleased(keyCode);
}
