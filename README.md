# ms-debitcard

Microservicio de gestión de tarjetas de débito asociadas a cuentas bancarias.

## Funcionalidad

- CRUD completo de tarjetas de débito.
- Asociación a una cuenta principal por `primaryAccountId`.
- Pago directo con cargo a la cuenta principal asociada.
- Publicación de eventos de movimientos con Kafka.

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/debit-cards` | Crear tarjeta de débito |
| GET | `/debit-cards/{id}` | Buscar tarjeta |
| GET | `/debit-cards` | Listar tarjetas |
| PUT | `/debit-cards/{id}` | Actualizar tarjeta |
| DELETE | `/debit-cards/{id}` | Eliminar tarjeta |
| POST | `/debit-cards/{id}/payments` | Registrar pago con la tarjeta |

## Eventos

| Topic | Trigger |
|-------|---------|
| `bank.movement.recorded` | Al registrar un pago con débito |
