package starter.application.ports;

public interface AuditLogRepositoryPort {
    void logAction(String action, String details);
}
