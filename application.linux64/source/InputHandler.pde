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
import java.util.Iterator;

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
  void mousePressed(int button){
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
  void mouseReleased(int button){
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
  
  void mouseMoved(){
     mouseMove = true;
  }
  
  // Devuelve el estado en el que se encuentra el botón solicitado
  int getMouseButtonState(int button){
    return mouseButtonState[button];
  }
  
  void keyPressed(int keycode){
    keyboardState.put(keyCode,KEY_PRESSED); 
  }
  
  void keyReleased(int keycode){
    keyboardState.put(keyCode,KEY_RELEASED); 
    println("released "+keycode);
  }
  
  boolean isKeyPressed(int code){
     return keyboardState.containsKey(code) && keyboardState.get(code) == KEY_PRESSED;
  }
  
  List<Integer> getPressedKeys(){
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
  void clearInput(){
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
  Vector getMousePosition() {
    // Tomando las variables globales aquí no
    // es muy diferente a hacerlo en los consumidores
    return new Vector(mouseX, mouseY);
  }
  
  boolean isMouseMoving(){
    return mouseMove; 
  }
}
