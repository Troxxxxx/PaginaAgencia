// ✅ Obtener token JWT desde cookies
function getJwtToken() {
    const cookieValue = document.cookie.split('; ').find(row => row.startsWith('JWT_TOKEN='));
    return cookieValue ? cookieValue.split('=')[1] : null;
}

// ✅ Mostrar perfil del usuario
window.addEventListener('DOMContentLoaded', async () => {
    const token = getJwtToken();
    const loginButton = document.getElementById('loginButton');
    const userProfile = document.getElementById('userProfile');
    const userAvatar = document.getElementById('userAvatar');
    const userName = document.getElementById('userName');
    const userEmail = document.getElementById('userEmail');

    if (token) {
        try {
            const res = await fetch('/api/auth/profile', {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (res.ok) {
                const user = await res.json();
                userName.textContent = user.nombre || "Usuario";
                userAvatar.src = user.ruta_imagen_usuario || "/assets/img/FotoPerfil/usuario2.jpg";
                userEmail.textContent = user.correo || "Correo no disponible";

                loginButton.classList.add('d-none');
                userProfile.classList.remove('d-none');

                const adminButton = document.querySelector('button[onclick*="/admin"]');
                if (user.tipo_usuario !== 1 && adminButton) {
                    adminButton.classList.add('d-none');
                }
            } else {
                console.warn("⚠ Token inválido o expirado.");
            }
        } catch (err) {
            console.error("❌ Error al cargar el perfil:", err);
        }
    }
});

// ✅ Cargar y renderizar el carrito
async function renderizarCarrito() {
    const cartContent = document.getElementById('cartContent');
    const cartTotal = document.getElementById('cartTotal');
    const ordenIdInput = document.getElementById('ordenId');

    const token = getJwtToken();
    if (!token) {
        window.location.href = '/home';
        return;
    }

    try {
        const response = await fetch('/cart/view', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) {
            cartContent.innerHTML = '<p class="text-danger text-center">Error al cargar el carrito.</p>';
            cartTotal.textContent = '0.00';
            return;
        }

        const order = await response.json();
        if (!order || !order.detalles || order.detalles.length === 0) {
            cartContent.innerHTML = '<p class="text-muted text-center">Tu carrito está vacío.</p>';
            cartTotal.textContent = '0.00';
            return;
        }

        let html = '<div class="list-group">';
        let total = 0;

        order.detalles.forEach(detalle => {
            const carro = detalle.carro;
            if (!carro || typeof carro.precioCarro !== 'number') return;

            const imagenSrc = carro.rutaImagen ? `/${carro.rutaImagen}` : '/assets/img/default-car.jpg';
            const marca = carro.marca?.nombreMarca || 'Sin Marca';
            const modelo = carro.modelo?.nombreModelo || 'Sin Modelo';
            const precio = carro.precioCarro.toFixed(2);
            const ano = carro.ano || 'N/A';

            total += carro.precioCarro;

            html += `
                <div class="list-group-item d-flex align-items-center mb-3 justify-content-between" style="border-radius: 8px;">
                    <div class="d-flex align-items-center">
                        <img src="${imagenSrc}" alt="${marca} ${modelo}" style="width: 100px; height: 100px; object-fit: cover; border-radius: 8px; margin-right: 15px;">
                        <div>
                            <h5 class="mb-1">${marca} ${modelo}</h5>
                            <p class="mb-1 text-muted">Año: ${ano} | Precio: $${precio}</p>
                        </div>
                    </div>
                    <button class="btn btn-sm btn-outline-danger btn-eliminar-detalle" data-id-detalle="${detalle.idDetalle}">
                        <i class="fas fa-trash-alt"></i>
                    </button>
                </div>
            `;
        });

        html += '</div>';
        cartContent.innerHTML = html;
        cartTotal.textContent = total.toFixed(2);
        ordenIdInput.value = order.idOrden;

    } catch (err) {
        console.error("❌ Error al cargar el carrito:", err);
        cartContent.innerHTML = '<p class="text-danger text-center">Error al cargar el carrito.</p>';
        cartTotal.textContent = '0.00';
    }
}


// ✅ Evento: abrir el modal del carrito
const cartModalElement = document.getElementById('cartModal');
if (cartModalElement) {
    cartModalElement.addEventListener('show.bs.modal', renderizarCarrito);
}

// ✅ Agregar carro al carrito
document.addEventListener('click', async (event) => {
    const btn = event.target.closest('.btn-agregar-carrito');
    if (!btn) return;

    const token = getJwtToken();
    if (!token) {
        window.location.href = '/home';
        return;
    }

    const carroId = btn.getAttribute('data-id-carro');
    if (!carroId) {
        console.error("❌ ID de carro no encontrado");
        return;
    }

    try {
        const response = await fetch(`/cart/add?carroId=${carroId}`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        const text = await response.text();
        let result = text ? JSON.parse(text) : null;

        if (response.ok) {
            alert(result?.mensaje || "🚗 Carro agregado al carrito");
        } else {
            alert(result?.error || "❌ Error al agregar al carrito");
        }
    } catch (err) {
        console.error("❌ Error de red al agregar al carrito:", err);
        alert("⚠ Error de red al intentar agregar al carrito");
    }
});

// ✅ Eliminar carro del carrito
document.addEventListener('click', async (event) => {
    const btn = event.target.closest('.btn-eliminar-detalle');
    if (!btn) return;

    const detalleId = btn.getAttribute('data-id-detalle');
    const token = getJwtToken();

    if (!token) {
        window.location.href = '/home';
        return;
    }

    if (!confirm("¿Estás seguro de que deseas eliminar este producto del carrito?")) return;

    try {
        const response = await fetch(`/cart/remove?detalleId=${detalleId}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        const text = await response.text();
        const result = text ? JSON.parse(text) : null;

        if (response.ok) {
            alert(result?.mensaje || "Producto eliminado del carrito");
            await renderizarCarrito();
        } else {
            alert(result?.error || "❌ Error al eliminar el producto");
        }
    } catch (err) {
        console.error("❌ Error al eliminar producto del carrito:", err);
        alert("⚠ Error de red al eliminar el producto");
    }
});
// ✅ Simular pago y mostrar factura
async function procesarPago() {
    const token = getJwtToken();
    try {
        const res = await fetch('/cart/checkout', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        const data = await res.json();

        if (res.ok) {
            alert(data.mensaje || "Pago realizado con éxito");
            await renderizarCarrito();
            intentarMostrarFactura(data.ordenId);
        } else {
            alert(data.error || "❌ Error en el proceso de pago");
        }
    } catch (err) {
        alert("❌ Error de red en el pago");
        console.error(err);
    }
}


// ✅ Mostrar factura con loader y retries
async function intentarMostrarFactura(facturaId, intentos = 5) {
    if (!facturaId) {
      console.error("❌ facturaId inválido para mostrar factura:", facturaId);
      return;
    }
  
    const token = getJwtToken();
    const loader = document.getElementById("facturaLoader");
    const content = document.getElementById("facturaContent");
  
    loader.style.display = "block";
    content.style.display = "none";
  
    const modal = new bootstrap.Modal(document.getElementById("invoiceModal"));
    modal.show();
  
    for (let i = 0; i < intentos; i++) {
      try {
        const res = await fetch(`/factura/by-id/${facturaId}`, {
          headers: { 'Authorization': `Bearer ${token}` }
        });
  
        if (res.ok) {
          const factura = await res.json();
  
          document.getElementById("facturaMonto").textContent = factura.monto.toFixed(2);
          document.getElementById("facturaFecha").textContent = new Date(factura.fechaPago).toLocaleString();
          document.getElementById("facturaEstado").textContent = factura.estado;
          document.getElementById("facturaPaypalId").textContent = factura.paypalOrderId;
  
          loader.style.display = "none";
          content.style.display = "block";
          return;
        }
      } catch (err) {
        console.warn(`⚠️ Intento ${i + 1} fallido al obtener factura`, err);
      }
  
      await new Promise(resolve => setTimeout(resolve, 500));
    }
  
    loader.style.display = "none";
    alert("❌ Error al cargar la factura. Intenta recargar la página.");
  }
  
  // ✅ Renderiza PayPal solo al abrir el modal de carrito
  document.getElementById('cartModal').addEventListener('shown.bs.modal', () => {
    const paypalContainer = document.getElementById("paypal-button-container");
    const cartTotalElement = document.getElementById("cartTotal");
    const ordenIdInput = document.getElementById("ordenId");
  
    if (!paypalContainer || !cartTotalElement || !ordenIdInput) {
      console.error("❌ No se encontraron los elementos necesarios para PayPal.");
      return;
    }
  
    // 🧹 Limpiar contenedor anterior antes de renderizar
    paypalContainer.innerHTML = "";
  
    paypal.Buttons({
      createOrder: function (data, actions) {
        const total = parseFloat(cartTotalElement.innerText);
        const ordenId = ordenIdInput.value;
  
        if (isNaN(total) || !ordenId) {
          alert("❌ No se puede iniciar el pago: Total u orden no válida.");
          throw new Error("Datos inválidos para el pago");
        }
  
        return fetch("/api/paypal/create-order", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ total, ordenId })
        })
          .then(res => res.json())
          .then(data => {
            if (!data.id) throw new Error("No se recibió un ID válido de PayPal");
            return data.id;
          });
      },
  
      onApprove: function (data, actions) {
        const ordenId = ordenIdInput.value;
  
        return fetch(`/api/paypal/capturar?orderID=${data.orderID}&ordenId=${ordenId}`, {
          method: "POST"
        })
          .then(res => {
            if (!res.ok) throw new Error("Error al capturar la orden en el backend");
            return res.json();
          })
          .then(data => {
            alert("✅ ¡Pago realizado con éxito!");
            console.log("🧾 Factura capturada:", data);
            renderizarCarrito?.(); // actualiza carrito si existe esa función
            intentarMostrarFactura(data.facturaId);
          })
          .catch(err => {
            console.error("❌ Error en onApprove:", err);
            alert("❌ Error al procesar el pago.");
          });
      },
  
      onCancel: () => alert("❌ Pago cancelado por el usuario."),
  
      onError: err => {
        console.error("❌ Error general de PayPal:", err);
        alert("❌ Ocurrió un error inesperado con el botón de pago.");
      }
    }).render("#paypal-button-container");
  });


// ✅ Logout global
function logout() {
    document.cookie = 'JWT_TOKEN=; Max-Age=0; path=/; SameSite=Strict';
    window.location.href = '/home';
}

// Lógica para mostrar el botón de administrador si el usuario es admin
window.addEventListener('DOMContentLoaded', async () => {
    const token = getJwtToken();
    const adminButton = document.getElementById('adminButton');

    // Ocultamos siempre el botón al inicio, por seguridad
    if (adminButton) {
        adminButton.classList.add('d-none');
    }

    if (!token) {
        console.warn("⚠ JWT_TOKEN no encontrado en cookies.");
        return;
    }

    try {
        const res = await fetch('/api/auth/profile', {
            headers: {
                'Authorization': 'Bearer ' + token
            }
        });

        if (res.ok) {
            const user = await res.json();

            // Mostrar el botón si es tipo_usuario == 1
            if (adminButton && user.tipo_usuario === 1) {
                adminButton.classList.remove('d-none');
            }

        } else {
            console.warn("⚠ Token inválido o expirado.");
        }
    } catch (err) {
        console.error("❌ Error al cargar el perfil:", err);
    }
});
// 💣 Parche definitivo para silenciar cualquier intento de usar cargarCarrito
window.cargarCarrito = function () {
    // No hace nada. Se deja definida para evitar errores.
};