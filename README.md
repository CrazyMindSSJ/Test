# ruta limpia

API REST para registrar usuarios, crear solicitudes de retiro de residuos y asignarlas a los camiones de la flota de su comuna.

## Ejecución

```powershell
.\mvnw.cmd spring-boot:run
```

Al iniciar, el menú de inicio de sesión aparece en la terminal y la API queda disponible en `http://localhost:8080`. Las solicitudes que alcanzan la capa MVC bajo `/api/**` se registran en la consola del servidor. El menú interactivo utiliza el perfil `login-console` de forma predeterminada.

En VS Code, selecciona la configuración **ProductosServiceApplication** y ejecútala con **F5** para responder al menú desde la terminal integrada. También puedes abrir una terminal integrada y ejecutar:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=login-console"
```

El menú permite iniciar sesión con cualquier cuenta registrada, muestra su nombre y rol, y permite cerrar la sesión o salir. En una terminal compatible, la contraseña no se muestra al escribirla. Para iniciar la API sin el menú interactivo, ejecuta `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=default"`.

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

Al iniciar sesión como cliente, el menú permite consultar sus solicitudes y crear una nueva mediante un formulario en la terminal. Después de mostrar una lista, resultado o mensaje informativo, la aplicación espera a que se presione Enter antes de volver al menú. El formulario solicita el tipo de residuo y el peso en kilogramos; el nombre, la dirección y la comuna se obtienen del perfil del cliente, y la solicitud se asigna a la flota de esa comuna. La base de demostración incluye tres clientes (`cliente1` a `cliente3`), tres flotas para `LA_FLORIDA`, `PUENTE_ALTO` y `LA_PINTANA`, y un camión asignado a cada flota.

Al iniciar sesión como conductor, el menú permite ver las solicitudes pendientes de la flota de su camión, aceptarlas, rechazarlas, consultar las asignadas y finalizar las que ya tomó. Un rechazo queda registrado para ese conductor: la solicitud conserva el estado `PENDIENTE` y puede ser aceptada por otro conductor de la misma flota, pero no vuelve a aparecer en la lista del conductor que la rechazó.

## Flujo de solicitudes

1. Autenticarse en `POST /api/auth/login` y usar el token de respuesta como `Authorization: Bearer <token>`.
2. Un cliente registra `POST /api/solicitudes` indicando tipo y peso. El nombre, dirección y comuna se toman del perfil autenticado; la comuna determina la flota, y la solicitud inicia como `PENDIENTE`.
3. Un conductor consulta `GET /api/solicitudes/disponibles`. Solo recibe solicitudes pendientes de la flota asignada a su camión y con licencia vigente.
4. Acepta con `POST /api/solicitudes/{id}/aceptar`. Se valida que siga disponible y que el peso acumulado no exceda `peso_carga` del camión.
5. Puede rechazar con `POST /api/solicitudes/{id}/rechazar`; la solicitud continúa pendiente para otro conductor de esa flota y deja de aparecer para quien la rechazó.
6. Consulta su lista con `GET /api/solicitudes/asignadas` y marca el retiro completado con `POST /api/solicitudes/{id}/completar`.

Clientes pueden consultar sus solicitudes mediante `GET /api/solicitudes/mias`; usuarios autenticados pueden consultar las flotas con `GET /api/flotas`.

`POST /api/auth/register` crea una cuenta de cliente y requiere `username`, `password`, `rut`, `nombre`, `apellido`, `edad`, `comuna`, `calle` y `numCalle`.
