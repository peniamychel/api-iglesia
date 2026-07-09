# Reglas y Estándares del Proyecto (RBAC y Seguridad)

Este archivo contiene las directrices de seguridad, roles, y mapeo de servicios de la aplicación para guiar a futuros agentes de desarrollo.

---

## 1. Modelo de Roles y Servicios (RBAC)

La aplicación tiene dos niveles principales de acceso:
* **Administrador Supremo (ADMIN):** Tiene acceso absoluto a todos los endpoints del backend (`hasRole('ADMIN')`) y todas las rutas en el frontend.
* **Roles Locales (PASTOR, ENCARGADO_IGLESIA, LIDER_JOVENES, DIACONO, TESORERO):** Tienen acceso filtrado a los datos de la iglesia activa asignada a su cargo actual. Sus acciones granulares (Crear, Editar, Ver, Eliminar, Imprimir) son validadas a través del sistema de privilegios en el token de autenticación.

---

## 2. Inicialización de Datos Semilla (DB)

Cualquier cambio o adición de roles, servicios o acciones debe realizarse en la clase de semilla `SecuritySeedDataInitializer.java`. 
* **Regla Crítica:** Nunca inyectes o fuerces privilegios globales a roles locales de forma directa en el backend sin considerar las tablas de especificaciones de roles.
* El inicializador sincroniza automáticamente la lista de acciones válidas llamando a `syncRolePrivileges(nombreRol, nombreCargo, listaAcciones)`.

---

## 3. Autorización y Seguridad en Controladores

Todos los controladores REST deben validar la autoridad correspondiente usando la estructura:
```java
@PreAuthorize("hasAuthority('SERVICIO:ACCION') OR hasRole('ADMIN')")
```
* **Bypass de Admin:** La cláusula `OR hasRole('ADMIN')` es requerida en todos los métodos para garantizar que el Administrador Supremo nunca quede bloqueado por falta de privilegios individuales.
