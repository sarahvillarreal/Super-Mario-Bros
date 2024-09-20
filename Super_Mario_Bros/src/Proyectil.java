import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Proyectil {
    private float x, y;
    private float velocidadX, velocidadY;
    private final float velocidad = 5.0f;
    private final float ancho = 30.0f;
    private final float alto = 20.0f;
    private boolean eliminado = false;
    private Image imagen; 

    public Proyectil(float x, float y, float direccionX, float direccionY) {
        this.x = x;
        this.y = y;
        this.velocidadX = direccionX * velocidad;
        this.velocidadY = direccionY * velocidad;
        cargarImagen();
    }

    private void cargarImagen() {
        String rutaImagen = "/Texturas/fuego.png";
        try {
            BufferedImage img = ImageIO.read(getClass().getResource(rutaImagen));
            this.imagen = img.getScaledInstance((int) ancho, (int) alto, Image.SCALE_SMOOTH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void actualizar() {
        x += velocidadX;
        y += velocidadY;
    }

    public void dibujar(Graphics g, Camara camara) {
        if (!eliminado) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(imagen, (int) (x - camara.getX()), (int) (y - camara.getY()), null);
        }
    }

    public Rectangle2D.Float getBounds() {
        return new Rectangle2D.Float(x, y, ancho, alto);
    }

    public void eliminar() {
        eliminado = true;
    }

    public boolean estaEliminado() {
        return eliminado;
    }
}
