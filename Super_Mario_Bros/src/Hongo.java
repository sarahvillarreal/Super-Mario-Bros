import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;

public class Hongo {
    private float x, y;
    private float velocidadX = +2.0f;
    private float velocidadY = 0.0f;
    private final float ancho = 48.0f;
    private final float alto = 48.0f;
    private boolean eliminado = false;
    private String tipoHongo;
    private Map<String, ImageIcon> texturas;
    private String texturaClave;

    public Hongo(float x, float y, String tipoHongo, Map<String, ImageIcon> texturas) {
        this.x = x;
        this.y = y;
        this.tipoHongo = tipoHongo;
        this.texturas = texturas;

        if (tipoHongo.equals("lucky_block_vida.png")) {
            this.texturaClave = "hongo_vida.png";
        } else {
            this.texturaClave = "hongo_boost.png";
        }
    }

    public void actualizar(List<Bloque> bloques, List<Bloque> killBlocks) {
        if (!eliminado) {
            velocidadY += 1.0f;  

  
            x += velocidadX;
            manejarColisiones(bloques, true);  


            y += velocidadY;
            manejarColisiones(bloques, false); 

    
            verificarKillBlocks(killBlocks);
        }
    }



    private void manejarColisiones(List<Bloque> bloques, boolean esHorizontal) {
        Rectangle enemigoRect = new Rectangle((int)x, (int)y, (int)ancho, (int)alto);
        for (Bloque bloque : bloques) {
            Rectangle bloqueRect = new Rectangle((int)(bloque.x * 48), (int)(bloque.y * 48), 48, 48);
            if (enemigoRect.intersects(bloqueRect)) {
                if (esHorizontal) {
                    velocidadX = -velocidadX;
                    x += velocidadX; 
                } else {
                    if (velocidadY > 0) {  
                        y = bloqueRect.y - alto;
                        velocidadY = 0;
                    } else if (velocidadY < 0) {  
                        y = bloqueRect.y + bloqueRect.height;
                        velocidadY = 0;
                    }
                }
            }
        }
    }


    private void verificarKillBlocks(List<Bloque> killBlocks) {
        Rectangle enemigoRect = new Rectangle((int)x, (int)y, (int)ancho, (int)alto);
        for (Bloque killblock : killBlocks) {
            Rectangle killblockRect = new Rectangle((int)(killblock.x * 48), (int)(killblock.y * 48), 48, 48);
            if (enemigoRect.intersects(killblockRect)) {
                eliminar();
            }
        }
    }

    public void eliminar() {
        eliminado = true;
    }

    public void dibujar(Graphics g, Camara camara) {
        if (!eliminado) {
            ImageIcon icono = texturas.get(texturaClave);
            if (icono != null) {
                Image imagen = icono.getImage();
                if (velocidadX < 0) {
                    g.drawImage(imagen, (int)(x - camara.getX()), (int)(y - camara.getY()), null);
                } else {
                    AffineTransform at = AffineTransform.getTranslateInstance(x - camara.getX(), y - camara.getY());
                    at.scale(-1, 1);
                    at.translate(-imagen.getWidth(null), 0);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.drawImage(imagen, at, null);
                    g2d.dispose();
                }
            } else {
                g.setColor(Color.RED); 
                g.fillRect((int)(x - camara.getX()), (int)(y - camara.getY()), (int)ancho, (int)alto);
            }
        }
    }



    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getAncho() {
        return ancho;
    }

    public float getAlto() {
        return alto;
    }

    public boolean estaEliminado() {
        return eliminado;
    }

    public String getTipo() {
        return tipoHongo;
    }
}
