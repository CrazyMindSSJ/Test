# Productos Service

API REST para registrar usuarios, crear solicitudes de retiro de residuos y asignarlas a los camiones de la flota de su comuna.

## Ejecución

```powershell
.\mvnw.cmd spring-boot:run
```

La API queda disponible en `http://localhost:8080`. Las solicitudes que alcanzan la capa MVC bajo `/api/**` se registran en la consola del servidor. La consola interactiva antigua solo se activa con el perfil `console`.

Para abrir la interfaz de inicio de sesión en la consola y mantener disponible la API REST, ejecuta:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=login-console"
```

La interfaz permite iniciar sesión con cualquier cuenta registrada, muestra su nombre y rol, y permite cerrar la sesión o salir. En una terminal compatible, la contraseña no se muestra al escribirla.

La base H2 de esta versión se crea en `data/productos-v2.mv.db`; la base anterior `data/productos.mv.db` no se modifica. Los datos de muestra se agregan solo cuando la base nueva no tiene usuarios.

## Cuentas de muestra

Contraseña para cada cuenta de muestra: `Cliente1234` para clientes y `Conductor1234` para conductores.

| Tipo | Usuario |
| --- | --- |
| Cliente | `cliente1`, `cliente2`, `cliente3` |
| Conductor | `conductor1`, `conductor2`, `conductor3` |
| Administración | `admin` — contraseña `Admin1234` |

Estas credenciales son exclusivamente para desarrollo local. No deben usarse en producción.

El inicializador carga tres clientes, tres conductores con licencia vigente, tres flotas (una por comuna), tres camiones y tres solicitudes pendientes de ejemplo.

## Flujo de solicitudes

1. Autenticarse en `POST /api/auth/login` y usar el token de respuesta como `Authorization: Bearer <token>`.
2. Un cliente registra `POST /api/solicitudes` indicando tipo y peso. El nombre, dirección y comuna se toman del perfil autenticado; la comuna determina la flota, y la solicitud inicia como `PENDIENTE`.
3. Un conductor consulta `GET /api/solicitudes/disponibles`. Solo recibe solicitudes pendientes de la flota asignada a su camión y con licencia vigente.
4. Acepta con `POST /api/solicitudes/{id}/aceptar`. Se valida que siga disponible y que el peso acumulado no exceda `peso_carga` del camión.
5. Consulta su lista con `GET /api/solicitudes/asignadas` y marca el retiro completado con `POST /api/solicitudes/{id}/completar`.

Clientes pueden consultar sus solicitudes mediante `GET /api/solicitudes/mias`; usuarios autenticados pueden consultar las flotas con `GET /api/flotas`.

`POST /api/auth/register` crea una cuenta de cliente y requiere `username`, `password`, `rut`, `nombre`, `apellido`, `edad`, `comuna`, `calle` y `numCalle`.
