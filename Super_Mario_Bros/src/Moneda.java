import java.awt.Graphics;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import javax.swing.ImageIcon;

public class Moneda {
    private float x, y;
    private String textura;
    private ImageIcon[] frames;
    private int frameIndex;
    private boolean recogida;
    private long lastUpdateTime;
    private long frameDuration; 

    public Moneda(float x, float y, String textura, Map<String, ImageIcon> texturas) {
        this.x = x;
        this.y = y;
        this.textura = textura;
        this.recogida = false;


        int numFrames = 4; 
        frames = new ImageIcon[numFrames];
        for (int i = 1; i <= numFrames; i++) {
            frames[i - 1] = texturas.get("frame-" + i + ".png"); 
        }

        frameIndex = 0;
        frameDuration = 100; 
        lastUpdateTime = System.currentTimeMillis();
    }

    private void updateFrame() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime >= frameDuration) {
            frameIndex = (frameIndex + 1) % frames.length;
            lastUpdateTime = currentTime;
        }
    }

    public void dibujar(Graphics g, Camara camara) {
        if (!recogida && frames[frameIndex] != null) {
            updateFrame(); 
            g.drawImage(frames[frameIndex].getImage(), (int) ((x * 48) - camara.getX()), (int) ((y * 48) - camara.getY()), null);
        }
    }

    public boolean colisionaConJugador(Jugador jugador) {
        if (!recogida) {
            Rectangle2D.Float monedaRect = new Rectangle2D.Float(x * 48, y * 48, 48, 48);
            Rectangle2D.Float jugadorRect = new Rectangle2D.Float(jugador.getX(), jugador.getY(), jugador.getAncho(), jugador.getAlto());
            
            return monedaRect.intersects(jugadorRect);
        }
        return false;
    }

    public void recoger() {
        this.recogida = true;
    }

    public boolean isRecogida() {
        return recogida;
    }
}
