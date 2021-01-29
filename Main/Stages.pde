import processing.core.PApplet; //<>//

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
  final void begin() {
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
  final void end() {
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
  final void update(long delta) {
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
  void onStageBegin() {
    // Este método puede ser sobreescrito por las clases hijas para capturar
    // el comienzo de la etapa
    println(this.getClass().getName()+" stage begin");
  }
  /*
   * Callback de finalización, invocado al pasar a Ended
   */
  void onStageEnd() {
    // Este método puede ser sobreescrito por las clases hijas para capturar
    // el fin de la etapa
    println(this.getClass().getName()+" stage end");
  }

  /*
   * Los callback de estado pueden (o no) ser capturados por las clases hijas, pero
   * onStageUpdate() debe ser implementado a la fuerza. Para esto, marcamos la clase
   * y el método como abstractos.
   */
  abstract void onStageUpdate(long delta);
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
  void onStageBegin() {
    image = loadImage(ASSETS_PATH+"match.jpg");
  }
  // Callback de actualizacion
  void onStageUpdate(long delta) {
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

  void onStageBegin() {
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
  void onRunClick(int v) {
    playerChoice = RUN;
    end();
  }
  
  // Callback del botón EXIT
  void onExitClick(int v) {
    playerChoice = EXIT; 
    end();
  }

  // Callback del botón VOLVER
  void onBackClick(int v) {
    enableMenu(menuMain, true);
    enableMenu(menuConfig, false);
  }

  // Callback de los botones de cambio de imagen de fondo
  void onImageButtonClick1() { 
    appCfgBackgroundImage = "a";
  }
  void onImageButtonClick2() { 
    appCfgBackgroundImage = "b";
  }
  void onImageButtonClick3() { 
    appCfgBackgroundImage = "c";
  }
  // Callback de los botones de cambio de hilo musical
  void onMusicButtonClick1() { 
    Audio.playTheme(Audio.THEME_1);
  }
  void onMusicButtonClick2() { 
    Audio.playTheme(Audio.THEME_2);
  }
  void onMusicButtonClick3() { 
    Audio.playTheme(Audio.THEME_3);
  }

  // Este metodo se invoca siempre que un control emite un evento 
  void controlEvent(ControlEvent event) {
    // En cada evento reproducimos el sample CLICK, funciona también en el cambio de valor del slider
    Audio.playSample(Audio.BUTTON_CLICK);
  }
  
  // Callback de actualizacion
  void onStageUpdate(long delta) {
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

  void onStageEnd() {
    // Deshabilitamos ambos menus antes de salir
    enableMenu(menuMain, false);
    enableMenu(menuConfig, false);
  }
  
  // Obtiene la elección del usuario
  int getPlayerChoice() {
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
  void onStageBegin() {
    if (next.getState() != StageState.Started) {
      next.setSize(this.width, this.height);
      next.setInputHandler(this.getInputHandler());
      next.begin();
    }
  }
  // Callback de actualización
  void onStageUpdate(long delta) {
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
  InputHandler getInputHandler() {
    return input;
  }

  // Este método se encarga de invocar al método de actualización
  // de la etapa en curso, alimentándolo con la delta de tiempo
  // transcurrido desde la última ejecución
  void procesStageLifecycle(long delta) {
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
  void processStageSequence() {
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
  Stage getStage() {
    return stage;
  }
}
