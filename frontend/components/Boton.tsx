'use client';

type Props = {
  children: React.ReactNode;
  onClick?: () => void;
  tipo?: 'primary' | 'secondary' | 'danger' | 'success' | 'warn';
  chico?: boolean;
  deshabilitado?: boolean;
  estilo?: React.CSSProperties;
  titulo?: string;
};

export default function Boton({
  children,
  onClick,
  tipo = 'secondary',
  chico,
  deshabilitado,
  estilo,
  titulo,
}: Props) {
  return (
    <button
      className={`btn ${tipo}${chico ? ' sm' : ''}`}
      onClick={onClick}
      disabled={deshabilitado}
      style={estilo}
      title={titulo}
      type="button"
    >
      {children}
    </button>
  );
}
