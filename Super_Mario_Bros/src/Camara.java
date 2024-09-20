public class Camara {
    private float x, y;
    private final float anchoVentana, altoVentana;

    public Camara(float x, float y, float anchoVentana, float altoVentana) {
        this.x = x;
        this.y = y;
        this.anchoVentana = anchoVentana;
        this.altoVentana = altoVentana;
    }

    // Actualiza la posición de la cámara basada en la posición del jugador
    public void actualizar(Jugador jugador) {
        x = jugador.getX() - anchoVentana / 2 + jugador.getAncho() / 2;
        y = jugador.getY() - altoVentana / 2 - 100 + jugador.getAlto() / 2;
        // Ajusta la posición de la cámara para centrar al jugador en la pantalla
    }

    // Métodos para obtener la posición de la cámara
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
