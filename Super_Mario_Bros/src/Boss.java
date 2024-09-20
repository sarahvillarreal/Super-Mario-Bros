import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.ImageIcon;

public class Boss {
    private float x, y;
    private float velocidadX = -2.0f;
    private float velocidadY = 0.0f;
    private final float ancho = 48.0f;
    private final float alto = 48.0f;
    private boolean eliminado = false;
    private int vida = 6;
    private long tiempoCambioDireccion = System.currentTimeMillis();
    private final long intervaloCambioDireccion = 2000;
    private final Random random = new Random();
    private List<Proyectil> proyectiles = new ArrayList<>();
    private long tiempoDisparo = System.currentTimeMillis();
    private final long intervaloDisparo = 3000;
    private BufferedImage[] caminarSprites;
    private int frameActual = 0;
    private long tiempoFrame = System.currentTimeMillis();
    private final long intervaloFrame = 100;


    private boolean enEmbestida = false;
    private long tiempoEmbestida = System.currentTimeMillis();
    private final long intervaloEmbestida = 5000;
    private final long duracionEmbestida = 1000;
    private final float velocidadEmbestida = 8.0f;

    private boolean mirandoDerecha = true; 

    public Boss(float x, float y, Map<String, ImageIcon> texturas) {
        this.x = x;
        this.y = y;
        this.caminarSprites = cargarSprites(texturas);
    }

    private BufferedImage[] cargarSprites(Map<String, ImageIcon> texturas) {
        BufferedImage[] sprites = new BufferedImage[3];
        for (int i = 0; i < sprites.length; i++) {
            ImageIcon icono = texturas.get("bowser" + i + ".png");
            if (icono != null) {
                Image imagen = icono.getImage();
                BufferedImage bufferedImage = new BufferedImage(
                    imagen.getWidth(null), imagen.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                Graphics g = bufferedImage.createGraphics();
                g.drawImage(imagen, 0, 0, null);
                g.dispose();
                sprites[i] = bufferedImage;
            } else {
                System.err.println("No se pudo cargar el sprite: bowser" + i + ".png");
            }
        }
        return sprites;
    }

    public void actualizar(List<Bloque> bloques, List<Bloque> killBlocks, List<Enemigo> enemigos, Jugador jugador) {
        if (!eliminado) {
            manejarEmbestida(jugador);

            if (!enEmbestida) {
                cambiarDireccionAleatoria();
                disparar(jugador);
            }

            velocidadY += 1.0f;
            x += velocidadX;
            manejarColisiones(bloques, true);
            y += velocidadY;
            manejarColisiones(bloques, false);
            verificarKillBlocks(killBlocks);

            for (Proyectil proyectil : proyectiles) {
                proyectil.actualizar();
            }

            verificarColisionesConJugador(jugador);
            actualizarFrame();
        }
    }

    private void manejarEmbestida(Jugador jugador) {
        long tiempoActual = System.currentTimeMillis();

        if (enEmbestida) {
            if (tiempoActual - tiempoEmbestida > duracionEmbestida) {
                enEmbestida = false;
                velocidadX = random.nextFloat() * 4 - 2;
                velocidadY = 0;
            } else {
                float direccionX = jugador.getX() - x;
                float direccionY = jugador.getY() - y;
                float longitud = (float) Math.sqrt(direccionX * direccionX + direccionY * direccionY);
                direccionX /= longitud;
                direccionY /= longitud;

                velocidadX = direccionX * velocidadEmbestida;
                mirandoDerecha = velocidadX > 0; 
            }
        } else {
            if (tiempoActual - tiempoEmbestida > intervaloEmbestida) {
                enEmbestida = true;
                tiempoEmbestida = tiempoActual;
            }
        }
    }

    private void actualizarFrame() {
        long tiempoActual = System.currentTimeMillis();
        if (tiempoActual - tiempoFrame > intervaloFrame) {
            frameActual = (frameActual + 1) % caminarSprites.length;
            tiempoFrame = tiempoActual;
        }
    }

    private void verificarColisionesConJugador(Jugador jugador) {
        for (Proyectil proyectil : proyectiles) {
            if (proyectil.getBounds().intersects(jugador.getBounds())) {
                jugador.perderVida();
                proyectil.eliminar();
            }
        }

        proyectiles.removeIf(Proyectil::estaEliminado);
    }

    private void disparar(Jugador jugador) {
        long tiempoActual = System.currentTimeMillis();
        if (tiempoActual - tiempoDisparo > intervaloDisparo) {
            float direccionX = jugador.getX() - (x + ancho / 2);
            float direccionY = jugador.getY() - (y + alto / 2);
            float longitud = (float) Math.sqrt(direccionX * direccionX + direccionY * direccionY);

            if (longitud != 0) {
                direccionX /= longitud;
                direccionY /= longitud;

                proyectiles.add(new Proyectil(x + ancho / 2, y + alto / 2, direccionX, direccionY));
            }

            tiempoDisparo = tiempoActual;
        }
    }

    private void cambiarDireccionAleatoria() {
        long tiempoActual = System.currentTimeMillis();
        if (tiempoActual - tiempoCambioDireccion > intervaloCambioDireccion) {
            if (random.nextBoolean()) {
                velocidadX = random.nextFloat() * 4 - 2;
                mirandoDerecha = velocidadX > 0; 
            } else {
                velocidadY = random.nextFloat() * 4 - 2;
            }
            tiempoCambioDireccion = tiempoActual;
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
                    mirandoDerecha = velocidadX > 0; 
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

    public void recibirDanio(int danio) {
        if (!eliminado) {
            vida -= danio;
            if (vida <= 0) {
                eliminar();
                Ventana.estado = Estado.VICTORIA; // Cambia el estado a VICTORIA
            }
        }
    }

    public void eliminar() {
        if (!eliminado) {
            eliminado = true;
        }
    }

    public void dibujar(Graphics g, Camara camara) {
        if (!eliminado) {
            BufferedImage spriteActual = caminarSprites[frameActual];
            if (spriteActual != null) {
                Graphics2D g2d = (Graphics2D) g.create();
                if (!mirandoDerecha) {

                    g2d.drawImage(spriteActual, (int)(x - camara.getX() + ancho), (int)(y - camara.getY()), 
                                  -(int)ancho, (int)alto, null);
                } else {
                    g2d.drawImage(spriteActual, (int)(x - camara.getX()), (int)(y - camara.getY()), null);
                }
                g2d.dispose();
            } else {
                System.err.println("El sprite actual es null");
                g.setColor(Color.RED);
                g.fillRect((int)(x - camara.getX()), (int)(y - camara.getY()), (int)ancho, (int)alto);
            }
        }

        for (Proyectil proyectil : proyectiles) {
            proyectil.dibujar(g, camara);
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

    public int getVida() {
        return vida;
    }
}
