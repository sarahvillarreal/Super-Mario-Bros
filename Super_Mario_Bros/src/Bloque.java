public class Bloque {
    float x, y;
    String Textura;

    public Bloque(float x, float y, String Textura) {
        this.x = x;
        this.y = y;
        this.Textura = Textura;
    }

	public Object getX() {
		return this.x;
	}

	public Object getY() {
		return this.y;
	}
}
