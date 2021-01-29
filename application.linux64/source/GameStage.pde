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
  void addIntensity(float amount) {
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
  void addAnimation(String name, int frames) {
    animations.put(name, new Animation(name, frames));
  }
  // Reproduce una animación dado su nombre
  void playAnimation(String name) {
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
  void onStageBegin() {
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
  void initializeEnemyAnimations() {
    enemyAnimations = new ArrayList<Animation>();

    for (int c=0; c<2; c++) {
      enemyAnimations.add(new Animation("enemy_"+c, 6));
    }
  }
  // Carga las animaciones de los tres tipos de items
  void initializeItemAnimations() {
    itemAnimations = new ArrayList<Animation>();

    for (int c=0; c<3; c++) {
      itemAnimations.add(new Animation("item_"+c, 6));
    }
  }
  // Construye la paleta de colores del fuego.
  // Basándose en 6 colores básicos, crea un degradado de 384 colores usando 
  // interpolaciones de los elementos de dos en dos
  void initializeFirePalette() {
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
  void onStageEnd() {
    ui.hide();
  }

  // Callback del botón de volver, termina la etapa al hacer click
  void onClick() {
    end();
  }

  // Borra la pantalla en cada iteración para volver a dibujar
  void clear() {
    // En lugar de pintar la pantalla en negro, usamos la imagen de fondo establecida en la configuración
    PImage background = backgroundImages.get(appCfgBackgroundImage);
    gfx.image(background, (this.width-background.width)/2, (this.height-background.height)/2);
  }
  
  // Dibuja una única cerilla en la pantalla
  void drawMatch(Match match) {
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
  void drawEnemy(Enemy enemy) {
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
  int getFireColor(int value, int max) {
    int index = (int)(value * 1f * firePalette.size() / max);
    if (index >= firePalette.size()) {
      return firePalette.get(firePalette.size()-1);
    } else {
      return firePalette.get(index);
    }
  }
  // Dibuja un único item
  void drawItem(Item item) {
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
  int getFlameHeight(float intensity) {
    // El valor es un poco random, ha sido ajustado hasta encontrar un equilibrio aceptable
    return (int)(MATCH_HEAD_RADIUS * 8 * intensity / Match.MAX_INTENSITY);
  }

  // Dibuja las llamas de las cerillas
  // A diferencia del resto de métodos de dibujo, aquí se pintan todas a la vez. Esto se debe
  // a que mientras que el resto de elementos dependen de la cerilla sobre la que descansan 
  // para ser pintados antes o despues, las llamas ocultan todo el escenario, por lo que el orden
  // no importa.
  void drawFire() {
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
  void drawScore() {
    String scoreText = String.valueOf(score);
    gfx.textSize(32);
    gfx.fill(255, 255, 255);
    int textWidth = (int)gfx.textWidth(scoreText);
    // Lo pintamos arriba a la derecha, con 25px de padding
    gfx.text(scoreText, width-textWidth-25, 50);
  }

  // Crea la animación del marcador de puntos
  void updateScore() {
    // Si quedan puntos pendientes de ser asignados, los asignamos y quitamos de la lista de pendientes
    if (pendingScore>0) {
      int scoreTaken = min(pendingScore, 5);
      pendingScore-=scoreTaken;
      score+=scoreTaken;
    }
  }
  // Crea la animación de la barra de vida y determina si el pavo vive o muere 
  void updateLifePoints() {
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
  void updatePowerPoints() {
    // Si quedan puntos de vuelo pendientes de asignar los vamos descontando poco a poco
    if (pendingPowerPoints != 0) {
      int pointsTaken = pendingPowerPoints > 0 ? 1 : -1;
      pendingPowerPoints-=pointsTaken;
      powerPoints+=pointsTaken;
    }
  }
  
  // Añade puntos a la lista de puntos pendientes
  void addScore(int amount) {
    pendingScore+=amount;
  }

  // Añade puntos de vida a la lista de puntos pendientes
  void addLifePoints(int amount) {
    
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
  int getLifePoints() {
    return pendingLifePoints + lifePoints;
  }
  // Determina cuantos puntos de vuelo tiene el pavo, teniendo en cuenta el valor pendiente de asignar
  int getPowerPoints() {
    return pendingPowerPoints + powerPoints;
  }
  
  // Añade puntos de vuelo a la lista de puntos pendientes
  void addPowerPoints(int amount) {
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
  void updateEnemies() {
    
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
  void drawTurkey() {
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
  void updateItems() {
    
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
  void updateMatches() {
    
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
  void drawGameOver() {
    gfx.textSize(150);
    int w = (int)gfx.textWidth("GAME OVER");
    int c = getFireColor((int)(getTimeElapsed()%1000), 1000);
    gfx.fill(c);
    gfx.text("GAME OVER", (this.width-w)/2, height/2 - 25);
  }

  // Pinta los puntos de vida del pavo
  void drawLifePoints() {
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
  void drawPowerPoints() {
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
  void updateTurkey(Match matchOver) {
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
  void onStageUpdate(long delta) {
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
