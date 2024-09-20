import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Niveles {
    private List<Bloque> bloques = new ArrayList<>();
    private List<Bloque> bloquesSinColision = new ArrayList<>();
    private List<Bloque> killBlocks = new ArrayList<>();
    private List<Bloque> areasVictoria = new ArrayList<>();
    private List<Enemigo> enemigos = new ArrayList<>();
    private List<Hongo> hongos = new ArrayList<>();
    private List<Moneda> monedas = new ArrayList<>();
    private Map<String, ImageIcon> texturas;
    private List<String> texturasSinColision;
    private int nivel = 1;
    private boolean victoria = false;
    private Boss boss;
    private List<Enemigo> enemigosOriginales = new ArrayList<>();
    private Bloque spawnBlock;

    public Niveles(String nombreArchivo) {
        texturasSinColision = List.of("agua1.png", "agua2.png", "1.png", "2.png", "3.png", "4.png", "5.png", "6.png", "7.png", "8.png", "9.png", "10.png", "11.png", "12.png", "13.png", "14.png", "15.png", "16.png", "17.png", "18.png", "castillo1.png", "castillo3.png", "castillo4.png", "castillo5.png", "castillo6.png");
        texturas = cargarTexturas("src/Texturas");
        cargarNivel(nombreArchivo);
        guardarPosicionesOriginales();
    }

    public void cambiarNivel(String nuevoNombreArchivo) {
        bloques.clear();
        bloquesSinColision.clear();
        killBlocks.clear();
        enemigos.clear();
        hongos.clear();
        System.out.println("Cambiando de nivel...");
        cargarNivel(nuevoNombreArchivo);
        guardarPosicionesOriginales();
    }

    private Map<String, ImageIcon> cargarTexturas(String rutaCarpeta) {
        Map<String, ImageIcon> texturas = new HashMap<>();
        try {
            Files.walk(Paths.get(rutaCarpeta)).forEach(rutaArchivo -> {
                if (Files.isRegularFile(rutaArchivo)) {
                    try {
                        String nombreTextura = rutaArchivo.getFileName().toString().toLowerCase();
                        BufferedImage imagenTextura = ImageIO.read(rutaArchivo.toFile());
                        if (imagenTextura != null) {
                            ImageIcon iconoImagen = new ImageIcon(imagenTextura.getScaledInstance(48, 48, Image.SCALE_SMOOTH));
                            texturas.put(nombreTextura, iconoImagen);
                        } else {
                            System.err.println("Error al cargar la imagen: " + rutaArchivo.toString());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return texturas;
    }

    private void cargarNivel(String nombreArchivo) {
        try (BufferedReader reader = new BufferedReader(new FileReader(nombreArchivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(",");
                float x = Float.parseFloat(partes[0]);
                float y = Float.parseFloat(partes[1]);
                String textura = partes[2].toLowerCase();

                Bloque bloque = new Bloque(x, y, textura);

                if (textura.startsWith("spawnblock")) {
                    spawnBlock = bloque;
                } else if (textura.startsWith("killblock")) {
                    killBlocks.add(bloque);
                } else if (textura.startsWith("spawnenemigo")) {
                    String tipoEnemigo = textura.substring("spawnenemigo".length()).toLowerCase();
                    tipoEnemigo = tipoEnemigo.replace(".png", "");
                    Enemigo enemigo = new Enemigo((int) (x * 48), (int) (y * 48), tipoEnemigo, texturas);
                    enemigos.add(enemigo);
                } else if (textura.equals("moneda.png")) { 
                    Moneda moneda = new Moneda(x, y, textura, texturas);
                    monedas.add(moneda);
                } else if (textura.startsWith("area-victoria")) {
                    areasVictoria.add(bloque);
                } else if (textura.startsWith("spawnboss")) {
                    boss = new Boss((x * 48), (y * 48), texturas);
                } else {
                    if (texturasSinColision.contains(textura)) {
                        bloquesSinColision.add(bloque);
                    } else {
                        bloques.add(bloque);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void guardarPosicionesOriginales() {
        enemigosOriginales.clear();


        for (Enemigo enemigo : enemigos) {
            Enemigo enemigoOriginal = new Enemigo(enemigo.getX(), enemigo.getY(), enemigo.getTipo(), texturas);
            enemigosOriginales.add(enemigoOriginal);
        }
    }

    public void reiniciarElementos() {
    	hongos.clear();
        enemigos.clear();
        for (Enemigo enemigoOriginal : enemigosOriginales) {
            Enemigo enemigo = new Enemigo(enemigoOriginal.getX(), enemigoOriginal.getY(), enemigoOriginal.getTipo(), texturas);
            enemigos.add(enemigo);
        }
    }

    public void actualizarElementos(Jugador jugador) {
        List<Bloque> bloquesParaEliminar = new ArrayList<>(); 

        for (Bloque bloque : bloques) {
            if (bloque.Textura.startsWith("lucky_block")) {
                Rectangle2D.Float bloqueRect = new Rectangle2D.Float(bloque.x * 48, bloque.y * 48, 48, 48);
                Rectangle2D.Float jugadorRect = new Rectangle2D.Float(jugador.getX(), jugador.getY() - 1, jugador.getAncho(), jugador.getAlto()); 

                if (bloqueRect.intersects(jugadorRect)) {
                    String tipo = bloque.Textura;
                    System.out.println("Colisión detectada con bloque: " + tipo);

                    if (tipo.equals("lucky_block_vida.png") || tipo.equals("lucky_block_boost.png")) {
                        if (!bloque.Textura.equals("bloque_abierto.png")) {
                            bloque.Textura = "bloque_abierto.png";
                            Hongo hongo = new Hongo(bloque.x * 48, (bloque.y - 1) * 48, tipo, texturas);
                            hongos.add(hongo);

                            ImageIcon icono = texturas.get("block_abierto.png");
                            if (icono != null) {
                                texturas.put(bloque.Textura, icono);
                            } else {
                                System.err.println("Error: No se ha cargado la textura bloque_abierto.png");
                            }
                        }
                    } else if (tipo.equals("lucky_block_moneda.png")) {
                        if (!bloque.Textura.equals("bloque_abierto.png")) {
                            ImageIcon icono = texturas.get("block_abierto.png");
                            if (icono != null) {
                                texturas.put("bloque_abierto.png", icono); 
                            } else {
                                System.err.println("Error: No se ha cargado la textura block_abierto.png");
                            }
                            jugador.incrementarMonedas(); 
                            bloque.Textura = "bloque_abierto.png"; 
                        }
                    } else if (tipo.equals("lucky_block.png")) {
                        if (!bloque.Textura.equals("bloque_abierto.png")) {
                            ImageIcon icono = texturas.get("block_abierto.png");
                            if (icono != null) {
                                texturas.put("bloque_abierto.png", icono); 
                            } else {
                                System.err.println("Error: No se ha cargado la textura block_abierto.png");
                            }
                            bloque.Textura = "bloque_abierto.png"; 
                        }
                    }
                    break; 
                }
            }
        }

        for (Bloque bloque : areasVictoria) {
            Rectangle2D.Float bloqueRect = new Rectangle2D.Float(bloque.x * 48, bloque.y * 48, 48, 48);
            Rectangle2D.Float jugadorRect = new Rectangle2D.Float(jugador.getX(), jugador.getY(), jugador.getAncho(), jugador.getAlto());

            if (bloqueRect.intersects(jugadorRect)) {
                reiniciarElementos();  
                if (nivel == 1) {
                    cambiarNivel("nivel_2.txt"); //nivel 2
                    nivel++;
                } else if (nivel == 2) {
                    cambiarNivel("nivel_3.txt"); //nivel 3
                    nivel++;
                } else if (nivel == 3) {
                    cambiarNivel("nivel_4.txt"); //nivel 4
                    nivel++;
                } else if (nivel == 4) {
                    cambiarNivel("nivel_5.txt"); //nivel 5
                    nivel++;
                } else if (nivel == 5) {
                	 Ventana.estado = Estado.VICTORIA;
                }
                break;
            }
        }

        if (boss != null) {
            boss.actualizar(bloques, killBlocks, enemigos, jugador); 
            if (boss.estaEliminado()) {
                boss = null;  
            }
        }

        List<Moneda> monedasRecogidas = new ArrayList<>();
        for (Moneda moneda : monedas) {
            if (moneda.colisionaConJugador(jugador)) {
                jugador.incrementarMonedas(); 
                moneda.recoger();  
                monedasRecogidas.add(moneda); 
            }
        }
        monedas.removeAll(monedasRecogidas);
    }






    public void dibujar(Graphics g, Camara camara) {
        for (Bloque b : bloques) {
            ImageIcon icono = texturas.get(b.Textura);
            if (icono != null) {
                g.drawImage(icono.getImage(), (int)((b.x * 48) - camara.getX()), (int)((b.y * 48) - camara.getY()), null);
            }
        }

        if (boss != null) {
            boss.dibujar(g, camara);
        }

        for (Moneda moneda : monedas) {
            moneda.dibujar(g, camara);
        }

        for (Bloque b : bloquesSinColision) {
            ImageIcon icono = texturas.get(b.Textura);
            if (icono != null) {
                g.drawImage(icono.getImage(), (int)((b.x * 48) - camara.getX()), (int)((b.y * 48) - camara.getY()), null);
            }
        }

        for (Enemigo enemigo : enemigos) {
            enemigo.dibujar(g, camara);
        }

       
        
        for (Hongo hongo : hongos) {
            hongo.dibujar(g, camara);
        }

       

        for (Bloque area : areasVictoria) {
            ImageIcon icono = texturas.get(area.Textura);
            if (icono != null) {
                g.drawImage(icono.getImage(), (int)((area.x * 48) - camara.getX()), (int)((area.y * 48) - camara.getY()), null);
            }
        }
    }


    public List<Bloque> getBloques() {
        return bloques;
    }

    public List<Bloque> getBloquesSinColision() {
        return bloquesSinColision;
    }

    public List<Bloque> getKillBlocks() {
        return killBlocks;
    }

    public List<Enemigo> getEnemigos() {
        return enemigos;
    }

    public List<Hongo> getHongos() {
        return hongos;
    }
    



	public Boss getBoss() {

		return boss;
	}
	public Bloque getSpawnBlock() {
        return spawnBlock;
    }
    
}
