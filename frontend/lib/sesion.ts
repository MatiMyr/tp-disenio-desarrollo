/**
 * Sesion del navegador. No hay seguridad real en esta entrega: del consultor se
 * guarda el nombre de usuario (que viaja en el header X-Usuario) y del
 * candidato, el numero de cuestionario que devuelve CU001.
 */

const CLAVE_CONSULTOR = 'evalpro.consultor';
const CLAVE_CUESTIONARIO = 'evalpro.idCuestionario';

function leer(clave: string): string | null {
  if (typeof window === 'undefined') {
    return null;
  }
  try {
    return window.sessionStorage.getItem(clave);
  } catch {
    return null;
  }
}

function escribir(clave: string, valor: string | null) {
  if (typeof window === 'undefined') {
    return;
  }
  try {
    if (valor === null) {
      window.sessionStorage.removeItem(clave);
    } else {
      window.sessionStorage.setItem(clave, valor);
    }
  } catch {
    // el navegador puede tener el almacenamiento bloqueado
  }
}

export function consultorActual(): string | null {
  return leer(CLAVE_CONSULTOR);
}

export function guardarConsultor(nombreUsuario: string) {
  escribir(CLAVE_CONSULTOR, nombreUsuario);
}

export function cuestionarioActual(): number | null {
  const valor = leer(CLAVE_CUESTIONARIO);
  return valor === null ? null : Number(valor);
}

export function guardarCuestionario(idCuestionario: number) {
  escribir(CLAVE_CUESTIONARIO, String(idCuestionario));
}

export function cerrarSesion() {
  escribir(CLAVE_CONSULTOR, null);
  escribir(CLAVE_CUESTIONARIO, null);
}

export function iniciales(nombre: string): string {
  const partes = nombre.trim().split(/\s+/);
  if (partes.length === 1) {
    return partes[0].slice(0, 2).toUpperCase();
  }
  return (partes[0][0] + partes[1][0]).toUpperCase();
}
