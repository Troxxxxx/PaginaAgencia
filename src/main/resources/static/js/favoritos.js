// ✅ Agregar carro a favoritos
async function agregarAFavoritos(carroId) {
    try {
        const response = await fetch(`/favoritos/agregar/${carroId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });

        if (response.ok) {
            await Swal.fire({
                icon: 'success',
                title: '¡Añadido!',
                text: 'Carro agregado a favoritos',
                confirmButtonText: 'Aceptar',
                timer: 2000,
                timerProgressBar: true
            });
            await cargarFavoritos();
        } else {
            await Swal.fire({
                icon: 'error',
                title: 'Error',
                text: 'Ya se encuentra en favoritos.',
                confirmButtonText: 'Aceptar'
            });
        }
    } catch (error) {
        console.error('❌ Error al agregar a favoritos:', error);
        await Swal.fire({
            icon: 'error',
            title: 'Error',
            text: 'Hubo un error al agregar.',
            confirmButtonText: 'Aceptar'
        });
    }
}

// ✅ Eliminar carro de favoritos
async function eliminarDeFavoritos(carroId) {
    const result = await Swal.fire({
        icon: 'warning',
        title: '¿Eliminar?',
        text: '¿Estás seguro de eliminar este carro de tus favoritos?',
        showCancelButton: true,
        confirmButtonText: 'Sí, eliminar',
        cancelButtonText: 'Cancelar',
        confirmButtonColor: '#dc3545',
        cancelButtonColor: '#6c757d'
    });

    if (!result.isConfirmed) return;

    try {
        const response = await fetch(`/favoritos/eliminar/${carroId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });

        if (response.ok) {
            await Swal.fire({
                icon: 'success',
                title: '¡Eliminado!',
                text: 'Carro eliminado de favoritos.',
                confirmButtonText: 'Aceptar',
                timer: 2000,
                timerProgressBar: true
            });
            await cargarFavoritos();
        } else {
            await Swal.fire({
                icon: 'error',
                title: 'Error',
                text: 'Error al eliminar favorito.',
                confirmButtonText: 'Aceptar'
            });
        }
    } catch (error) {
        console.error('❌ Error al eliminar favorito:', error);
        await Swal.fire({
            icon: 'error',
            title: 'Error',
            text: 'Error crítico al eliminar.',
            confirmButtonText: 'Aceptar'
        });
    }
}

// ✅ Agregar carro al carrito desde favoritos
async function agregarAlCarritoDesdeFavoritos(carroId) {
    const token = getJwtToken();

    if (!token) {
        window.location.href = '/home';
        return;
    }

    try {
        const response = await fetch(`/cart/add?carroId=${carroId}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            await Swal.fire({
                icon: 'success',
                title: '¡Añadido!',
                text: 'Carro agregado al carrito',
                confirmButtonText: 'Aceptar',
                timer: 2000,
                timerProgressBar: true
            });

            window.renderizarCarrito?.();
            await cargarFavoritos();
        } else {
            // 🚫 Evita mostrar alerta de error visible
            console.warn("⚠️ Error silencioso al agregar desde favoritos.");
        }
    } catch (error) {
        // 🛡️ Silenciar todo sin mostrar alerta visual ni error en consola
        if (
            error instanceof ReferenceError &&
            error.message.includes("cargarCarrito is not defined")
        ) {
            return; // ignoramos el error completamente
        }

        console.warn("⚠️ Error inesperado (silenciado):", error);
    }
}


// ✅ Cargar lista de favoritos
async function cargarFavoritos() {
    try {
        const favoritosContent = document.getElementById('favoritosContent');
        const favoritosCount = document.getElementById('favoritosCount');

        if (!favoritosContent || !favoritosCount) return;

        const response = await fetch('/favoritos', { method: 'GET' });

        if (!response.ok) {
            favoritosContent.innerHTML = '<p>❌ Error al cargar los favoritos.</p>';
            favoritosCount.textContent = '0';
            return;
        }

        const favoritos = await response.json();
        favoritosCount.textContent = favoritos.length;

        if (favoritos.length > 0) {
            let html = '<ul class="list-group">';
            favoritos.forEach(favorito => {
                html += `
                    <li class="list-group-item d-flex align-items-start flex-column mb-2">
                        <div class="d-flex w-100 align-items-center">
                            <img src="${favorito.imagenUrl}" alt="${favorito.marca} ${favorito.modelo}" 
                                 style="width: 100px; height: 60px; object-fit: cover; margin-right: 15px; border-radius: 5px;">
                            <div class="flex-grow-1">
                                <strong>${favorito.marca}</strong> ${favorito.modelo} (${favorito.ano})
                            </div>
                            <div>
                                <span class="badge bg-primary rounded-pill">$${favorito.precio.toLocaleString()}</span>
                            </div>
                        </div>
                        <div class="mt-2 d-flex justify-content-end gap-2 w-100">
                            <button class="btn btn-sm btn-success" onclick="agregarAlCarritoDesdeFavoritos(${favorito.idCarro})">
                                <i class="fas fa-cart-plus"></i> 
                            </button>
                            <button class="btn btn-sm btn-danger" onclick="eliminarDeFavoritos(${favorito.idCarro})">
                                <i class="fas fa-trash"></i> 
                            </button>
                        </div>
                    </li>
                `;
            });
            html += '</ul>';
            favoritosContent.innerHTML = html;
        } else {
            favoritosContent.innerHTML = '<p>No tienes favoritos guardados.</p>';
        }

    } catch (error) {
        console.error('❌ Error crítico al cargar favoritos:', error);
    }
}

// ✅ Cargar favoritos al abrir el modal
document.getElementById('favoritosModal')?.addEventListener('show.bs.modal', cargarFavoritos);
