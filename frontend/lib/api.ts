import { consultorActual } from './sesion';

/**
 * Error que devuelve la API. El backend siempre responde los errores con la
 * forma {"errores": [...]}, con todos los errores juntos.
 */
export class ErrorApi extends Error {
  errores: string[];
  estado: number;

  constructor(errores: string[], estado: number) {
    super(errores.join(' | '));
    this.errores = errores;
    this.estado = estado;
  }
}

function encabezados(conCuerpo: boolean): HeadersInit {
  const headers: Record<string, string> = {};
  if (conCuerpo) {
    headers['Content-Type'] = 'application/json';
  }
  const usuario = consultorActual();
  if (usuario) {
    headers['X-Usuario'] = usuario;
  }
  return headers;
}

async function pedir<T>(ruta: string, metodo: string, cuerpo?: unknown): Promise<T> {
  let respuesta: Response;
  try {
    respuesta = await fetch(`/api${ruta}`, {
      method: metodo,
      headers: encabezados(cuerpo !== undefined),
      body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
    });
  } catch {
    throw new ErrorApi(['No se pudo conectar con el servidor'], 0);
  }

  if (!respuesta.ok) {
    let errores = [`Error ${respuesta.status} al llamar al servidor`];
    try {
      const datos = await respuesta.json();
      if (Array.isArray(datos?.errores) && datos.errores.length > 0) {
        errores = datos.errores;
      }
    } catch {
      // la respuesta no trae el cuerpo esperado: queda el mensaje genérico
    }
    throw new ErrorApi(errores, respuesta.status);
  }

  if (respuesta.status === 204) {
    return undefined as T;
  }
  const texto = await respuesta.text();
  return (texto ? JSON.parse(texto) : undefined) as T;
}

export const api = {
  get: <T,>(ruta: string) => pedir<T>(ruta, 'GET'),
  post: <T,>(ruta: string, cuerpo?: unknown) => pedir<T>(ruta, 'POST', cuerpo ?? {}),
  put: <T,>(ruta: string, cuerpo?: unknown) => pedir<T>(ruta, 'PUT', cuerpo ?? {}),
  delete: <T,>(ruta: string) => pedir<T>(ruta, 'DELETE'),

  /** Pide un archivo al backend y lo descarga con el navegador. */
  descargar: async (ruta: string, cuerpo?: unknown) => {
    const respuesta = await fetch(`/api${ruta}`, {
      method: 'POST',
      headers: encabezados(true),
      body: JSON.stringify(cuerpo ?? {}),
    });
    if (!respuesta.ok) {
      let errores = ['No se pudo generar el archivo'];
      try {
        const datos = await respuesta.json();
        if (Array.isArray(datos?.errores) && datos.errores.length > 0) {
          errores = datos.errores;
        }
      } catch {
        // sin cuerpo de error
      }
      throw new ErrorApi(errores, respuesta.status);
    }
    const disposicion = respuesta.headers.get('Content-Disposition') ?? '';
    const coincidencia = /filename="?([^"]+)"?/.exec(disposicion);
    const nombre = coincidencia ? coincidencia[1] : 'archivo';
    const blob = await respuesta.blob();
    const url = URL.createObjectURL(blob);
    const enlace = document.createElement('a');
    enlace.href = url;
    enlace.download = nombre;
    document.body.appendChild(enlace);
    enlace.click();
    enlace.remove();
    URL.revokeObjectURL(url);
    return nombre;
  },
};

/** Mensajes de error de cualquier excepción, para mostrarlos todos juntos. */
export function mensajesDeError(e: unknown): string[] {
  if (e instanceof ErrorApi) {
    return e.errores;
  }
  return [e instanceof Error ? e.message : 'Ocurrió un error inesperado'];
}
