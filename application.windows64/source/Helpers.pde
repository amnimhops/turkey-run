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
  boolean isInside(Vector topLeft, Vector bottomRight) {
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
  void update() {
    time_now = millis();
    time_delta_millis = time_now - time_old;
    time_old = time_now;

    time_delta_sec = time_delta_millis / 1000.0;
  }

  // return mseconds elapsed since last execution
  int getDeltaMillis() {
    return time_delta_millis;
  }

  // return seconds elapsed since last execution
  float getDeltaSec() {
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

  void reset () {
    currentTime = startPoint;
  }

  void setPoints (int sPoint, int ePoint) {
    startPoint = sPoint;
    endPoint = ePoint;

    up = ePoint >= sPoint;
    
    reset();
  }

  void update (int millis) {
    if(counting) {
      if(up) {
        currentTime+=millis;
      }else{
        currentTime-=millis;
      }
    }
  }

  void start (boolean onoff) {
    counting = onoff;
  }

  int getTimeMillis () {
    return currentTime;
  }

  float getTimeSec () {
    return (currentTime / 1000.0);
  }

  boolean getFinish() {
    return counting || (up && currentTime >= endPoint) || currentTime <= startPoint;
  }
}
