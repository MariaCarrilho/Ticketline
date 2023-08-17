package pd.ticketline.utils;

public class Search {
    private String criterio;
    private String pesquisa;

    public Search(String criterio, String pesquisa) {
        this.criterio = criterio;
        this.pesquisa = pesquisa;
    }

    public String getCriterio() {
        return criterio;
    }

    public String getPesquisa() {
        return pesquisa;
    }
}
