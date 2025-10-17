package Dominio;
public class Bloqueo {
private String pedro;
private String pablo;

    public Bloqueo(String pedro, String pablo) {
        this.pedro = pedro;
        this.pablo = pablo;
    }

    public String getPedro() {
        return pedro;
    }

    public void setPedro(String pedro) {
        this.pedro = pedro;
    }

    public String getPablo() {
        return pablo;
    }

    public void setPablo(String pablo) {
        this.pablo = pablo;
    }

    @Override
    public String toString() {
        return "Bloqueo{" + "Pedro = " + pedro + ", Pablo = " + pablo + '}';
    }
    
}
