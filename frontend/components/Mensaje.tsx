'use client';

type Tipo = 'error' | 'success' | 'info' | 'warn';

const ICONOS: Record<Tipo, string> = {
  error: '⚠',
  success: '✔',
  info: 'ℹ',
  warn: '⚠',
};

type Props = {
  tipo: Tipo;
  children: React.ReactNode;
  onCerrar?: () => void;
  estilo?: React.CSSProperties;
};

export default function Mensaje({ tipo, children, onCerrar, estilo }: Props) {
  return (
    <div className={`msg ${tipo}`} style={estilo}>
      <span className="msg-icon">{ICONOS[tipo]}</span>
      <span style={{ flex: 1 }}>{children}</span>
      {onCerrar && (
        <button className="msg-close" onClick={onCerrar} title="Cerrar">
          ✕
        </button>
      )}
    </div>
  );
}

/**
 * Muestra TODOS los errores juntos, como pide la consigna, sin tapar los campos
 * ni los botones.
 */
export function ListaErrores({
  errores,
  titulo,
  onCerrar,
}: {
  errores: string[];
  titulo?: string;
  onCerrar?: () => void;
}) {
  if (errores.length === 0) {
    return null;
  }
  if (errores.length === 1 && !titulo) {
    return (
      <Mensaje tipo="error" onCerrar={onCerrar}>
        {errores[0]}
      </Mensaje>
    );
  }
  return (
    <Mensaje tipo="error" onCerrar={onCerrar}>
      <strong>{titulo ?? 'Se encontraron los siguientes errores:'}</strong>
      <ul>
        {errores.map((error, i) => (
          <li key={i}>{error}</li>
        ))}
      </ul>
    </Mensaje>
  );
}
