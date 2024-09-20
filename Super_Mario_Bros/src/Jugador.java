import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class Jugador {
    public float x, y;
    private float velocidadX = 0;
    private float velocidadY = 0;
    private float ancho = 48;
    private float alto = 48;
    private boolean pisando = false;
    private static int vidas = 3;  
    private boolean izquierdaPresionada = false;
    private boolean derechaPresionada = false;
    private Niveles niveles;  
    private static int monedas = 0;
    private BufferedImage idleSprite;
    private BufferedImage[] caminarSprites;
    private int animacionCaminarIndex = 0;
    private int contadorFrames = 0; 
    private final int framesPorSprite = 6;
    private boolean mirandoIzquierda = false;
    private boolean modo_boost = false;
    
    public Jugador(float x, float y, Niveles niveles) {
        this.x = x;
        this.y = y;
        this.niveles = niveles;  
        
        cargarSprites();
    }
    
    private void cargarSprites() {
        try {
            
            idleSprite = ImageIO.read(getClass().getResource("/texturas/mario1.png"));
            
            caminarSprites = new BufferedImage[3]; //cantidad de sprites al caminar
            caminarSprites[0] = ImageIO.read(getClass().getResource("/texturas/mario2.png"));
            caminarSprites[1] = ImageIO.read(getClass().getResource("/texturas/mario1.png"));
            caminarSprites[2] = ImageIO.read(getClass().getResource("/texturas/mario3.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void actualizar(List<Bloque> bloques, List<Enemigo> enemigos, List<Bloque> killBlocks, List<Hongo> hongos, Boss boss) {
    	if (izquierdaPresionada) {
    	    velocidadX = -7; // velocidad hacia un lado
    	    mirandoIzquierda = true;
    	} else if (derechaPresionada) {
    	    velocidadX = 7; // el otro
    	    mirandoIzquierda = false; 
    	} else {
    	    velocidadX = 0;
    	}

        velocidadY += 1;

        x += velocidadX;
        manejarColisiones(bloques, true); // si colisiona

        y += velocidadY;
        manejarColisiones(bloques, false);

        verificarColisionConEnemigos(enemigos);
        verificarColisionConKillBlocks(killBlocks);
        verificarColisionConHongos(hongos);
        verificarColisionConBoss(boss); 
        
        actualizarAnimacion();
    }


    private void manejarColisiones(List<Bloque> bloques, boolean esHorizontal) {
        Rectangle2D.Float jugadorRect = new Rectangle2D.Float(x, y, ancho, alto);
        pisando = false;

        for (Bloque bloque : bloques) {
            Rectangle2D.Float bloqueRect = new Rectangle2D.Float(bloque.x * 48, bloque.y * 48, 48, 48);

            if (jugadorRect.intersects(bloqueRect)) {
                if (esHorizontal) {

                    if (velocidadX > 0) { 
                        x = bloqueRect.x - ancho;
                    } else if (velocidadX < 0) { 
                        x = bloqueRect.x + bloqueRect.width;
                    }
                } else {

                    if (velocidadY > 0) {
                        y = bloqueRect.y - alto;
                        velocidadY = 0;
                        pisando = true; 
                    } else if (velocidadY < 0) { 
                        y = bloqueRect.y + bloqueRect.height;
                        velocidadY = 0;
                    }
                }
                jugadorRect.setRect(x, y, ancho, alto);
            }
        }
    }
    
    public void incrementarMonedas() {
        this.monedas++;
        System.out.println("Monedas: " + this.monedas);
    }
    
    private void actualizarAnimacion() {
        contadorFrames++;
        if (contadorFrames >= framesPorSprite) {
            contadorFrames = 0;
            animacionCaminarIndex++;
            if (animacionCaminarIndex >= caminarSprites.length) {
                animacionCaminarIndex = 0;
            }
        }
    }
    
    private void verificarColisionConEnemigos(List<Enemigo> enemigos) {
        Rectangle2D.Float jugadorRect = new Rectangle2D.Float(x, y, ancho, alto);


        List<Enemigo> copiaEnemigos = new ArrayList<>(enemigos);

        for (Enemigo enemigo : copiaEnemigos) {
            if (!enemigo.estaEliminado()) {
                Rectangle2D.Float enemigoRect = new Rectangle2D.Float(enemigo.getX(), enemigo.getY(), enemigo.getAncho(), enemigo.getAlto());

                if (jugadorRect.intersects(enemigoRect)) {

                    if (y + alto <= enemigo.getY() + enemigo.getAlto() / 2) {
                        enemigos.remove(enemigo); 
                    } else {
                    	if(modo_boost == true)
                    	{
                    		enemigos.remove(enemigo); 
                    		modo_boost = false;
                    		alto = 48;
                    	}
                    	else
                    	{
                        perderVida(); 
                    	}
                    }
                }
            }
        }
    }


    private void verificarColisionConBoss(Boss boss) {
        if (boss != null) {
            Rectangle2D.Float jugadorRect = new Rectangle2D.Float(x, y, ancho, alto);
            Rectangle2D.Float bossRect = new Rectangle2D.Float(boss.getX(), boss.getY(), boss.getAncho(), boss.getAlto());

            if (jugadorRect.intersects(bossRect)) {
             
                if (y + alto <= boss.getY() + boss.getAlto() / 2 && velocidadY > 0) {
                    boss.recibirDanio(1);
                    velocidadY = -15; 
                    pisando = true; 
                } else {
                    perderVida(); 
                }
            }
        }
    }



    private void verificarColisionConHongos(List<Hongo> hongos) { // aca todo lo de los hongos
        Rectangle2D.Float jugadorRect = new Rectangle2D.Float(x, y, ancho, alto);

        List<Hongo> copiaHongos = new ArrayList<>(hongos); // una copia para evitar bugs al reiniciar nivel

        for (Hongo hongo : copiaHongos) {
            if (!hongo.estaEliminado()) {
                Rectangle2D.Float hongoRect = new Rectangle2D.Float(hongo.getX(), hongo.getY(), hongo.getAncho(), hongo.getAlto());

                if (jugadorRect.intersects(hongoRect)) {
                    if (hongo.getTipo().equals("lucky_block_vida.png")) { //si es de vida te aumenta una
                        vidas++;
                        hongos.remove(hongo); 
                    } else if (hongo.getTipo().equals("lucky_block_boost.png")) {
                        alto = 64; 
                        modo_boost = true;
                        hongos.remove(hongo); 
                    }
                }
            }
        }
    }





    private void verificarColisionConKillBlocks(List<Bloque> killBlocks) {
        Rectangle2D.Float jugadorRect = new Rectangle2D.Float(x, y, ancho, alto);

        for (Bloque killblock : killBlocks) {
            Rectangle2D.Float killblockRect = new Rectangle2D.Float(killblock.x * 48, killblock.y * 48, 48, 48);

            if (jugadorRect.intersects(killblockRect)) {
                perderVida();
            }
        }
    }

    public void perderVida() {
        vidas--;
        if (vidas <= 0) {
            System.out.println("Game Over");
            Ventana.estado = Estado.GAME_OVER; // Cambia el estado a GAME_OVER
        } else {
            niveles.reiniciarElementos();  
            

            Bloque spawnBlock = niveles.getSpawnBlock();
            if (spawnBlock != null) {
                x = spawnBlock.x * 48;  
                y = spawnBlock.y * 48;
            }
            else
            {
            	System.err.print("No se encontro el spawnblock");
            	x = 50;
            	y = 50;
            }
            velocidadY = 0;  
        }
    }


    public void dibujar(Graphics g, Camara camara) {
        Graphics2D g2d = (Graphics2D) g;
        BufferedImage spriteActual;


        if (velocidadX == 0) {
            spriteActual = idleSprite;  
        } else {
            spriteActual = caminarSprites[animacionCaminarIndex];  
        }

        g2d.translate((int)(x - camara.getX()), (int)(y - camara.getY()));

        if (mirandoIzquierda) {
            g2d.scale(-1, 1);  
            g2d.drawImage(spriteActual, -(int) ancho, 0, (int) ancho, (int) alto, null); 
        } else {
            g2d.drawImage(spriteActual, 0, 0, (int) ancho, (int) alto, null);
        }


        g2d.setTransform(new AffineTransform()); 
    }



    public KeyAdapter getControladorTeclado() {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT, KeyEvent.VK_A -> izquierdaPresionada = true;
                    case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> derechaPresionada = true;
                    case KeyEvent.VK_SPACE, KeyEvent.VK_W -> {
                        if (pisando) {
                            velocidadY = -21;  
                        }
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT, KeyEvent.VK_A -> izquierdaPresionada = false;
                    case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> derechaPresionada = false;
                }
            }
        };
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

    public static int getVidas() {
        return vidas;
    }
    
    public static int getMonedas() {
        return monedas;
    }
    
    public Rectangle2D.Float getBounds() {
        return new Rectangle2D.Float(x, y, ancho, alto);
    }

	public void setY(float y2) {
		y = y2;
		
	}

	public void setX(float x2) {
		x = x2;
	}
}
