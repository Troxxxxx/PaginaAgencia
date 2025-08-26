document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.delete-btn').forEach(button => {
        button.addEventListener('click', function (event) {
            event.preventDefault(); // Previene el envío inmediato del formulario

            Swal.fire({
                title: "¿Estás seguro?",
                text: "¡No podrás revertir esta acción!",
                icon: "warning",
                showCancelButton: true,
                confirmButtonColor: "#c6303e",
                cancelButtonColor: "#616970",
                confirmButtonText: "Sí, eliminar",
                cancelButtonText: "Cancelar",
                draggable: true
            }).then((result) => {
                if (result.isConfirmed) {
                    const form = this.closest('form');
                    const url = form.action;
                    const method = form.method;

                    const formData = new URLSearchParams(new FormData(form)); // Captura todos los datos del form

                    fetch(url, {
                        method: method,
                        headers: {
                            'Content-Type': 'application/x-www-form-urlencoded',
                        },
                        body: formData
                    })
                    .then(response => response.json())
                    .then(data => {
                        if (data.success) {
                            Swal.fire({
                                title: "¡Eliminado!",
                                text: "El registro ha sido eliminado.",
                                icon: "success",
                                confirmButtonColor: "#ffc60a",
                                draggable: true
                            }).then(() => {
                                window.location.reload();
                            });
                        } else {
                            Swal.fire({
                                title: "Error",
                                text: data.message || "No se pudo eliminar el registro.",
                                icon: "error",
                                confirmButtonColor: "#ffc60a",
                                draggable: true
                            });
                        }
                    })
                    .catch(error => {
                        Swal.fire({
                            title: "Error",
                            text: "Ocurrió un error al intentar eliminar el registro.",
                            icon: "error",
                            confirmButtonColor: "#ffc60a",
                            draggable: true
                        });
                    });
                }
            });
        });
    });
});