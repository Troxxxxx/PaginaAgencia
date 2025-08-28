Agencia de Ventas de Autos — Spring Boot + JWT + Google OAuth + PayPal (Docker)

Aplicación web para gestión y venta de vehículos:
- Catálogo de autos con filtros y detalle.
- Autenticación tradicional (email/clave) y Google OAuth.
- Carrito de compras con control de disponibilidad.
- Pagos con PayPal (sandbox) y generación de factura (OrdenPago).
- Módulo de favoritos por usuario.
- Panel básico de administración (usuarios, marcas, modelos, tipos, imágenes).
- Docker Compose con MySQL y phpMyAdmin.

> Proyecto dockerizado y listo para correr localmente.


 Demo / Screenshots
> (Súbelas a `/docs/img/` y referencia aquí)
- Home / Catálogo
- Detalle de carro
- Carrito + Checkout (PayPal)
- Login / Google OAuth
- Admin de usuarios / imágenes

---

 🛠️ Stack Técnico
- Backend: Java 21, Spring Boot 3 (Web, Security, Data JPA, Thymeleaf, Actuator)
- Auth: JWT (jjwt 0.11.5), Google Identity (OAuth2 / token verification)
- Base de datos: MySQL 8, phpMyAdmin
- Build: Maven
- Frontend templating: Thymeleaf + Bootstrap 5 + FontAwesome
- Infra: Docker / Docker Compose

---

 ✨ Features Clave
- Registro/login tradicional + login con Google (`/api/auth/google`).
- Emisión y almacenamiento de JWT en cookie `JWT_TOKEN`.
- CRUD de entidades: Carro, Marca, Modelo, Tipo, Imagen.
- Carrito y checkout con verificación de disponibilidad y marcar vendido.
- PayPal sandbox: creación y captura de orden, persistencia en `OrdenPago`.
- Favoritos por usuario (agregar/listar/eliminar).

---

## 📂 Estructura (extracto relevante)
