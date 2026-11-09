package starter.adapters.demo;

import starter.application.ports.AuditLogRepositoryPort;

public class ConsoleAuditLogAdapter implements AuditLogRepositoryPort {
    @Override
    public void logAction(String action, String details) {
        System.out.printf("%s: %s%n", action, details);
    }
}
