package fr.bnia.boot;

import com.openhtmltopdf.util.Diagnostic;
import com.openhtmltopdf.util.XRLog;
import com.openhtmltopdf.util.XRLogger;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * Journal d'OpenHTMLtoPDF (ADR-020) : par défaut, la bibliothèque écrit sur la console, hors de la configuration des
 * logs, une ligne d'information par PDF. Elle passe ici par SLF4J, sous ses propres noms ({@code com.openhtmltopdf.*}),
 * dont le niveau se règle dans {@code application.yml} (avertissements et erreurs seulement).
 */
@Configuration(proxyBeanMethods = false)
class JournalPdfConfiguration {

    JournalPdfConfiguration() {
        XRLog.setLoggerImpl(new VersSlf4j());
    }

    /** Les niveaux de {@code java.util.logging} traduits en ceux de SLF4J. */
    static final class VersSlf4j implements XRLogger {

        @Override
        public void log(String ou, Level niveau, String message) {
            log(ou, niveau, message, null);
        }

        @Override
        public void log(String ou, Level niveau, String message, Throwable erreur) {
            Logger logger = LoggerFactory.getLogger(ou);
            int n = niveau.intValue();
            if (n >= Level.SEVERE.intValue()) {
                logger.error(message, erreur);
            } else if (n >= Level.WARNING.intValue()) {
                logger.warn(message, erreur);
            } else if (n >= Level.INFO.intValue()) {
                logger.info(message, erreur);
            } else if (n >= Level.FINE.intValue()) {
                logger.debug(message, erreur);
            } else {
                logger.trace(message, erreur);
            }
        }

        /** Le niveau se règle dans la configuration des logs, pas par la bibliothèque. */
        @Override
        public void setLevel(String ou, Level niveau) {
        }

        @Override
        public boolean isLogLevelEnabled(Diagnostic diagnostic) {
            Logger logger = LoggerFactory.getLogger("com.openhtmltopdf");
            int n = diagnostic.getLevel().intValue();
            return n >= Level.SEVERE.intValue() ? logger.isErrorEnabled()
                    : n >= Level.WARNING.intValue() ? logger.isWarnEnabled()
                    : n >= Level.INFO.intValue() ? logger.isInfoEnabled()
                    : n >= Level.FINE.intValue() ? logger.isDebugEnabled() : logger.isTraceEnabled();
        }
    }
}
