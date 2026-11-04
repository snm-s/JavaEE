package fr.malinassur.exception;

/**
 * Erreur "métier" : une règle n'est pas respectée (ex : résilier un contrat déjà résilié).
 * La servlet l'attrape et affiche son message à l'utilisateur.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
