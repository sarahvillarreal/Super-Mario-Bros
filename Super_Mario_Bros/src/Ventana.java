import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

public class Ventana extends JFrame {

    private static final long serialVersionUID = 1L;
    static Estado estado;
    private static final int ANCHO = 1200;
    private static final int ALTO = 700;

    private Niveles nivel;
    private Jugador jugador;
    private Camara camara;

    private static Image icono = new ImageIcon("src/Recursos/logo3.png").getImage();
    private static Image logo = new ImageIcon("src/Recursos/logo3.png").getImage();
    private static ImageIcon gifFondo = new ImageIcon("src/Recursos/background_menu2.gif");
    private static ImageIcon gifFondo2 = new ImageIcon("src/Recursos/background_menu.gif");
    private Image fondo1 = new ImageIcon("src/Recursos/fondo_nivel1.png").getImage();
    private Image fondo4 = new ImageIcon("src/Recursos/fondo_nivel4.png").getImage();
    private Image fondo5 = new ImageIcon("src/Recursos/fondo_nivel5.png").getImage();
    private Image vida = new ImageIcon("src/Texturas/vida.png").getImage();
    private Image moneda = new ImageIcon("src/Texturas/frame-1.png").getImage();
    Image moneda2 = moneda.getScaledInstance(32, 32, Image.SCALE_SMOOTH);

    public Ventana(Jugador jugador, Niveles nivel) {
        this.jugador = jugador;
        this.nivel = nivel;

        setIconImage(icono);

        setTitle("Super Mario Bros");
        setSize(ANCHO, ALTO);
        setBounds(80, 40, ANCHO, ALTO);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());


        camara = new Camara(0, 0, ANCHO, ALTO);
        camara.actualizar(jugador);

        JPanel panelJuego = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                if (estado == Estado.MAIN_MENU) {
                    g.drawImage(gifFondo.getImage(), 0, 0, ANCHO, ALTO, this);
                    graficoMainMenu(g);
                } else if (estado == Estado.NIVEL_UNO || estado == Estado.NIVEL_DOS || estado == Estado.NIVEL_TRES) {
                    g.drawImage(fondo1, 0, 0, getWidth(), getHeight(), this);
                    dibujarInterfazJuego(g);
                    nivel.dibujar(g, camara);
                    jugador.dibujar(g, camara);
                } else if (estado == Estado.NIVEL_CUATRO) {
                    g.drawImage(fondo4, 0, 0, getWidth(), getHeight(), this);
                    dibujarInterfazJuego(g);
                    nivel.dibujar(g, camara);
                    jugador.dibujar(g, camara);
                } else if (estado == Estado.NIVEL_CINCO) {
                    g.drawImage(fondo5, 0, 0, getWidth(), getHeight(), this);
                    dibujarInterfazJuego(g);
                    nivel.dibujar(g, camara);
                    jugador.dibujar(g, camara);
                } else if (estado == Estado.GAME_OVER) {
                    g.drawImage(gifFondo2.getImage(), 0, 0, ANCHO, ALTO, this);
                    graficoGameOver(g);
                } else if (estado == Estado.VICTORIA) {
                    g.drawImage(gifFondo.getImage(), 0, 0, ANCHO, ALTO, this);
                    graficoVictoria(g);
                }
            }
        };

        add(panelJuego, BorderLayout.CENTER);
        addKeyListener(jugador.getControladorTeclado());

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((estado == Estado.GAME_OVER || estado == Estado.VICTORIA) && e.getKeyCode() == KeyEvent.VK_SPACE) {
                    volverAlMenu();
                }
            }
        });

        Timer timer = new Timer(16, e -> {
            if (estado == Estado.NIVEL_UNO || estado == Estado.NIVEL_DOS || estado == Estado.NIVEL_TRES || estado == Estado.NIVEL_CUATRO || estado == Estado.NIVEL_CINCO) {
                jugador.actualizar(nivel.getBloques(), nivel.getEnemigos(), nivel.getKillBlocks(), nivel.getHongos(), nivel.getBoss());
                for (Enemigo enemigo : nivel.getEnemigos()) {
                    enemigo.actualizar(nivel.getBloques(), nivel.getKillBlocks());
                }
                for (Hongo hongo : nivel.getHongos()) {
                    hongo.actualizar(nivel.getBloques(), nivel.getKillBlocks());
                }
                camara.actualizar(jugador);
                nivel.actualizarElementos(jugador);
                panelJuego.repaint();
            }
        });
        timer.start();
    }

    private void dibujarInterfazJuego(Graphics g) {

        g.drawImage(vida, 5, 5, this);
 
        g.drawImage(moneda2, 1149, 5, this);

        try {
            Font marioFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/Recursos/PressStart2P-Regular.ttf")).deriveFont(18f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(marioFont);

            g.setColor(Color.WHITE);
            g.setFont(marioFont);
            g.drawString("" + Jugador.getVidas(), 54, 32);
            g.drawString("" + Jugador.getMonedas(), 1100, 32);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
    }

    private void volverAlMenu() {
        estado = Estado.MAIN_MENU;
        dispose(); 
        SwingUtilities.invokeLater(() -> {
            JFrame menuFrame = new JFrame("Super Mario Bros");
            menuFrame.setSize(ANCHO, ALTO);
            menuFrame.setBounds(80, 40, ANCHO, ALTO);
            menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            menuFrame.setLayout(new BorderLayout());
            menuFrame.setIconImage(icono);

            JPanel menuPanel = new JPanel() {
                private static final long serialVersionUID = 1L;

                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.drawImage(gifFondo.getImage(), 0, 0, ANCHO, ALTO, this);
                    graficoMainMenu(g);
                }
            };

            menuFrame.add(menuPanel, BorderLayout.CENTER);

            menuFrame.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() >= KeyEvent.VK_1 && e.getKeyCode() <= KeyEvent.VK_5) {
                        estado = Estado.values()[Estado.NIVEL_UNO.ordinal() + (e.getKeyCode() - KeyEvent.VK_1)];
                        menuFrame.dispose();
                        iniciarJuego();
                    }
                }
            });

            menuFrame.setVisible(true);
        });
    }

    public static void main(String[] args) {
        estado = Estado.MAIN_MENU;

        JFrame menuFrame = new JFrame("Super Mario Bros");
        menuFrame.setSize(ANCHO, ALTO);
        menuFrame.setBounds(80, 40, ANCHO, ALTO);
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setLayout(new BorderLayout());
        menuFrame.setIconImage(icono);

        JPanel menuPanel = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (estado == Estado.MAIN_MENU) {
                    g.drawImage(gifFondo.getImage(), 0, 0, ANCHO, ALTO, this);
                    graficoMainMenu(g);
                }
            }
        };

        menuFrame.add(menuPanel, BorderLayout.CENTER);

        menuFrame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() >= KeyEvent.VK_1 && e.getKeyCode() <= KeyEvent.VK_5) {
                    estado = Estado.values()[Estado.NIVEL_UNO.ordinal() + (e.getKeyCode() - KeyEvent.VK_1)];
                    menuFrame.dispose();
                    iniciarJuego();
                }
            }
        });

        menuFrame.setVisible(true);
    }

    private static void iniciarJuego() {
        Niveles nivel;
        String[] nivelArchivos = {"nivel_1.txt", "nivel_2.txt", "nivel_3.txt", "nivel_4.txt", "nivel_5.txt"};
        int nivelIndex = estado.ordinal() - Estado.NIVEL_UNO.ordinal();
        nivel = new Niveles(nivelArchivos[nivelIndex]);

        Jugador jugador = new Jugador(50, 50, nivel);
        Bloque spawnBlock = nivel.getSpawnBlock();
        if (spawnBlock != null) {
            jugador.x = spawnBlock.x * 48;
            jugador.y = spawnBlock.y * 48;
        }

        SwingUtilities.invokeLater(() -> {
            Ventana ventana = new Ventana(jugador, nivel);
            ventana.setVisible(true);
        });
    }

    private static void graficoMainMenu(Graphics g) {
   
        g.drawImage(logo, 220, 30, logo.getWidth(null) / 2, logo.getHeight(null) / 2, null);

        try {
            Font marioFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/Recursos/PressStart2P-Regular.ttf")).deriveFont(18f);
            Font marioFont2 = marioFont.deriveFont(14f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(marioFont);
            ge.registerFont(marioFont2);


            g.setColor(Color.BLACK);
            g.setFont(marioFont);
            g.drawString("---- Jugar ----", 445, 470);

            g.setFont(marioFont2);
            g.drawString("[1] Nivel 1", 500, 510);
            g.drawString("[2] Nivel 2", 500, 540);
            g.drawString("[3] Nivel 3", 500, 570);
            g.drawString("[4] Nivel 4", 500, 600);
            g.drawString("[5] Nivel 5", 500, 630);

        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
    }

    private static void graficoGameOver(Graphics g) {
    	
    	try {
            Font marioFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/Recursos/PressStart2P-Regular.ttf")).deriveFont(50f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(marioFont);

            g.setColor(Color.YELLOW);
            g.setFont(marioFont);
            g.drawString("GAME OVER", 380, 250);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
    	
    	try {
            Font marioFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/Recursos/PressStart2P-Regular.ttf")).deriveFont(20f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(marioFont);

            g.setColor(Color.WHITE);
            g.setFont(marioFont);
            g.drawString("Presione [Espacio] para regresar al menú", 180, 450);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
        
    }

    private static void graficoVictoria(Graphics g) {
        
        try {
            Font marioFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/Recursos/PressStart2P-Regular.ttf")).deriveFont(50f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(marioFont);

            g.setColor(Color.BLACK);
            g.setFont(marioFont);
            g.drawString("VICTORIA", 395, 250);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
        
        try {
            Font marioFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/Recursos/PressStart2P-Regular.ttf")).deriveFont(40f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(marioFont);

            g.setColor(Color.BLACK);
            g.setFont(marioFont);
            g.drawString("¡Has ganado!", 360, 300);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }

        try {
            Font marioFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/Recursos/PressStart2P-Regular.ttf")).deriveFont(20f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(marioFont);

            g.setColor(Color.WHITE);
            g.setFont(marioFont);
            g.drawString("Presione [Espacio] para regresar al menú", 180, 450);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
    }
}