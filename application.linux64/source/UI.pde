//
// INTERFAZ DE USUARIO
//
import java.util.List;

// Ancho de los botones
private final static int BTN_IMAGE_WIDTH = 150;
// Alto de los botones
private final static int BTN_IMAGE_HEIGHT = 50;
// Lista de imagenes asociadas a cada estado, por ID de botón
private static final Map<String, List<PImage>> buttonImages = new HashMap<String, List<PImage>>();
// Lista de imagenes de fondo, por nombre
private static final Map<String, PImage> backgroundImages = new HashMap<String,PImage>();

// Método de servicio que busca todas las imágenes de fondo (background_*) y las carga en memoria
void loadBackgroundImages() {
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
PImage getBackgroundImage(String bgId){
  return backgroundImages.get(bgId); 
}

// Precarga en memoria todas las imágenes de estado de un botón
void loadButtonImages(String[] btnNames) {
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
