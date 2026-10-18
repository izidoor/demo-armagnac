package fr.bnia.commun;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.application.FormatDocument;
import fr.bnia.commun.application.StockageDocuments;
import fr.bnia.commun.domain.DocumentDepose;
import fr.bnia.commun.domain.DocumentRefuse;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.support.AbstractIT;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Stockage commun des documents (ADR-018) : dépôt, lecture, suppression et garde-fous de l'adapter. */
class StockageDocumentsIT extends AbstractIT {

    private static final byte[] PDF = "%PDF-1.4\n%%EOF\n".getBytes(StandardCharsets.US_ASCII);

    @Autowired StockageDocuments stockage;

    @Test
    void depose_lit_et_supprime_un_pdf() {
        DocumentDepose depose = stockage.deposerPdf("facture.pdf", PDF);

        assertThat(depose.type()).isEqualTo("application/pdf");
        assertThat(depose.taille()).isEqualTo(PDF.length);
        StockageDocuments.DocumentLu lu = stockage.lire(depose.cle());
        assertThat(lu.document()).isEqualTo(depose);
        assertThat(lu.contenu()).isEqualTo(PDF);

        DocumentDepose autre = stockage.deposerPdf("dae.pdf", PDF);
        stockage.supprimer(depose.cle());
        stockage.supprimer(List.of(autre.cle()));
        assertThatThrownBy(() -> stockage.lire(depose.cle())).isInstanceOf(ElementIntrouvable.class);
        assertThatThrownBy(() -> stockage.lire(autre.cle())).isInstanceOf(ElementIntrouvable.class);
    }

    @Test
    void refuse_un_fichier_vide_trop_gros_ou_qui_n_est_pas_un_pdf() {
        assertThatThrownBy(() -> stockage.deposerPdf("vide.pdf", new byte[0]))
                .isInstanceOf(DocumentRefuse.class).hasMessage("Le fichier « vide.pdf » est vide.");
        assertThatThrownBy(() -> stockage.deposerPdf("photo.pdf", "GIF89a".getBytes(StandardCharsets.US_ASCII)))
                .isInstanceOf(DocumentRefuse.class).hasMessage("Le fichier « photo.pdf » n'est pas un PDF.");
        byte[] gros = Arrays.copyOf(PDF, (int) StockageDocuments.TAILLE_MAXIMALE + 1);
        assertThatThrownBy(() -> stockage.deposerPdf("gros.pdf", gros))
                .isInstanceOf(DocumentRefuse.class).hasMessage("Le fichier « gros.pdf » dépasse 10 Mo.");
    }

    @Test
    void reconnait_les_formats_admis_par_leur_signature() {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0};
        byte[] xlsx = {'P', 'K', 0x03, 0x04, 0};
        java.util.Set<FormatDocument> tous = java.util.EnumSet.allOf(FormatDocument.class);
        assertThat(stockage.deposer("ecran.png", png, tous).type()).isEqualTo("image/png");
        assertThat(stockage.deposer("matrice.xlsx", xlsx, tous).type())
                .isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        assertThat(stockage.deposer("attestation.pdf", PDF, tous).type()).isEqualTo("application/pdf");
        assertThatThrownBy(() -> stockage.deposer("outil.exe", new byte[] {'M', 'Z'}, tous))
                .isInstanceOf(DocumentRefuse.class).hasMessage("Le fichier « outil.exe » n'est pas un PDF, ni une image"
                        + " PNG, ni une image JPEG, ni un classeur Excel (.xlsx).");
        assertThatThrownBy(() -> stockage.deposerPdf("ecran.pdf", png)).isInstanceOf(DocumentRefuse.class)
                .hasMessage("Le fichier « ecran.pdf » n'est pas un PDF.");
    }
}
