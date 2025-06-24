package com.controlescolar.config;

import org.mindrot.jbcrypt.BCrypt;
import java.util.regex.Pattern;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Configuración de seguridad para el sistema de control escolar
 * Maneja encriptación de contraseñas, validaciones y sesiones
 */
public class SecurityConfig {

    // Configuración de BCrypt
    private static final int BCRYPT_ROUNDS = 12;

    // Patrones de validación
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    );
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[0-9]{10,15}$"
    );

    // Configuración de sesiones
    private static final int SESSION_TIMEOUT_MINUTES = 30;
    private static final Map<String, SessionInfo> activeSessions = new HashMap<>();

    // Configuración de intentos de login
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;
    private static final Map<String, LoginAttempt> loginAttempts = new HashMap<>();

    /**
     * Encripta una contraseña usando BCrypt
     * @param password Contraseña en texto plano
     * @return Contraseña encriptada
     */
    public static String hashPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Verifica si una contraseña coincide con su hash
     * @param password Contraseña en texto plano
     * @param hashedPassword Contraseña encriptada
     * @return true si coinciden, false en caso contrario
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        if (password == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(password, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Valida que una contraseña cumpla con los requisitos de seguridad
     * @param password Contraseña a validar
     * @return true si es válida, false en caso contrario
     */
    public static boolean isValidPassword(String password) {
        if (password == null) return false;
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Valida formato de email
     * @param email Email a validar
     * @return true si es válido, false en caso contrario
     */
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Valida formato de teléfono
     * @param phone Teléfono a validar
     * @return true si es válido, false en caso contrario
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        return PHONE_PATTERN.matcher(phone.replaceAll("\\s+", "")).matches();
    }

    /**
     * Crea una nueva sesión de usuario
     * @param userId ID del usuario
     * @param username Nombre de usuario
     * @param role Rol del usuario
     * @return Token de sesión
     */
    public static String createSession(String userId, String username, String role) {
        String sessionToken = UUID.randomUUID().toString();
        SessionInfo sessionInfo = new SessionInfo(userId, username, role, LocalDateTime.now());
        activeSessions.put(sessionToken, sessionInfo);

        // Limpiar sesiones expiradas
        cleanExpiredSessions();

        return sessionToken;
    }

    /**
     * Valida si una sesión es válida y no ha expirado
     * @param sessionToken Token de sesión
     * @return SessionInfo si es válida, null en caso contrario
     */
    public static SessionInfo validateSession(String sessionToken) {
        if (sessionToken == null) return null;

        SessionInfo sessionInfo = activeSessions.get(sessionToken);
        if (sessionInfo == null) return null;

        // Verificar si la sesión ha expirado
        LocalDateTime now = LocalDateTime.now();
        long minutesElapsed = ChronoUnit.MINUTES.between(sessionInfo.getLastActivity(), now);

        if (minutesElapsed > SESSION_TIMEOUT_MINUTES) {
            activeSessions.remove(sessionToken);
            return null;
        }

        // Actualizar última actividad
        sessionInfo.updateLastActivity();
        return sessionInfo;
    }

    /**
     * Cierra una sesión específica
     * @param sessionToken Token de sesión a cerrar
     */
    public static void closeSession(String sessionToken) {
        if (sessionToken != null) {
            activeSessions.remove(sessionToken);
        }
    }

    /**
     * Registra un intento de login fallido
     * @param identifier Identificador del usuario (email o username)
     * @return true si el usuario está bloqueado, false en caso contrario
     */
    public static boolean recordFailedLogin(String identifier) {
        if (identifier == null) return false;

        LoginAttempt attempt = loginAttempts.get(identifier);
        if (attempt == null) {
            attempt = new LoginAttempt();
            loginAttempts.put(identifier, attempt);
        }

        attempt.incrementAttempts();

        // Si excede el máximo de intentos, bloquear
        if (attempt.getAttempts() >= MAX_LOGIN_ATTEMPTS) {
            attempt.setLockoutTime(LocalDateTime.now());
            return true;
        }

        return false;
    }

    /**
     * Verifica si un usuario está bloqueado por intentos fallidos
     * @param identifier Identificador del usuario
     * @return true si está bloqueado, false en caso contrario
     */
    public static boolean isUserLocked(String identifier) {
        if (identifier == null) return false;

        LoginAttempt attempt = loginAttempts.get(identifier);
        if (attempt == null || attempt.getLockoutTime() == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        long minutesElapsed = ChronoUnit.MINUTES.between(attempt.getLockoutTime(), now);

        if (minutesElapsed >= LOCKOUT_DURATION_MINUTES) {
            // El bloqueo ha expirado
            loginAttempts.remove(identifier);
            return false;
        }

        return true;
    }

    /**
     * Limpia los intentos de login después de un login exitoso
     * @param identifier Identificador del usuario
     */
    public static void clearLoginAttempts(String identifier) {
        if (identifier != null) {
            loginAttempts.remove(identifier);
        }
    }

    /**
     * Limpia sesiones expiradas del mapa de sesiones activas
     */
    private static void cleanExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        activeSessions.entrySet().removeIf(entry -> {
            long minutesElapsed = ChronoUnit.MINUTES.between(
                    entry.getValue().getLastActivity(), now
            );
            return minutesElapsed > SESSION_TIMEOUT_MINUTES;
        });
    }

    /**
     * Obtiene el número de minutos restantes hasta que expire el bloqueo
     * @param identifier Identificador del usuario
     * @return Minutos restantes de bloqueo, 0 si no está bloqueado
     */
    public static long getRemainingLockoutMinutes(String identifier) {
        if (identifier == null) return 0;

        LoginAttempt attempt = loginAttempts.get(identifier);
        if (attempt == null || attempt.getLockoutTime() == null) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        long minutesElapsed = ChronoUnit.MINUTES.between(attempt.getLockoutTime(), now);
        long remaining = LOCKOUT_DURATION_MINUTES - minutesElapsed;

        return Math.max(0, remaining);
    }

    /**
     * Obtiene mensaje de validación de contraseña
     * @return String con los requisitos de contraseña
     */
    public static String getPasswordRequirements() {
        return "La contraseña debe tener al menos 8 caracteres y contener: " +
                "una mayúscula, una minúscula, un número y un carácter especial (@#$%^&+=)";
    }

    /**
     * Clase interna para manejar información de sesión
     */
    public static class SessionInfo {
        private final String userId;
        private final String username;
        private final String role;
        private final LocalDateTime createdAt;
        private LocalDateTime lastActivity;

        public SessionInfo(String userId, String username, String role, LocalDateTime createdAt) {
            this.userId = userId;
            this.username = username;
            this.role = role;
            this.createdAt = createdAt;
            this.lastActivity = createdAt;
        }

        public void updateLastActivity() {
            this.lastActivity = LocalDateTime.now();
        }

        // Getters
        public String getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getLastActivity() { return lastActivity; }
    }

    /**
     * Clase interna para manejar intentos de login
     */
    private static class LoginAttempt {
        private int attempts = 0;
        private LocalDateTime lockoutTime;

        public void incrementAttempts() {
            this.attempts++;
        }

        public int getAttempts() { return attempts; }
        public LocalDateTime getLockoutTime() { return lockoutTime; }
        public void setLockoutTime(LocalDateTime lockoutTime) { this.lockoutTime = lockoutTime; }
    }
}