package fr.bnia.declarations.application;

import fr.bnia.declarations.application.ConsulterStocksDeclares.LigneStockListee;
import java.time.LocalDate;
import java.util.List;

/** L'export des stocks, capturé : ce qui lui est passé. */
final class EditionsStocksCapturees implements EditionsStocks {

    List<LigneStockListee> lignes;
    LocalDate etabliLe;

    @Override
    public byte[] excel(List<LigneStockListee> lignes, LocalDate etabliLe) {
        this.lignes = lignes;
        this.etabliLe = etabliLe;
        return new byte[] {1};
    }
}
